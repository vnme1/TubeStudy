# 🚀 TubeStudy 배포 가이드 (Deployment Guide)

TubeStudy를 프로덕션 환경에서 배포하기 위한 완벽한 가이드입니다.

---

## 📋 목차 (Table of Contents)

1. [개발 환경 → 배포 환경 전환](#개발-환경--배포-환경-전환)
2. [서버 배포 방법](#서버-배포-방법)
3. [Chrome 확장 배포](#chrome-확장-배포)
4. [데이터베이스 설정](#데이터베이스-설정)
5. [보안 설정](#보안-설정)
6. [모니터링 및 로깅](#모니터링-및-로깅)
7. [트러블슈팅](#트러블슈팅)

---

## 🔄 개발 환경 → 배포 환경 전환

### **개발 환경 (Development)**
```properties
# application.properties
spring.datasource.url=jdbc:h2:file:./data/tubestudy_db
spring.h2.console.enabled=true
server.port=18085
logging.level.root=DEBUG
```

### **배포 환경 (Production)**
```properties
# application-prod.properties
spring.datasource.url=jdbc:h2:file:/var/lib/tubestudy/tubestudy_db
spring.h2.console.enabled=false
server.port=8080
logging.level.root=INFO
server.servlet.context-path=/api
```

---

## 🖥️ 서버 배포 방법

### **옵션 1: JAR 파일 배포 (권장)**

#### **Step 1: 프로덕션 빌드**
```bash
mvn clean package -DskipTests -Pprod
```

#### **Step 2: 배포 서버에 업로드**
```bash
scp target/tracker-0.0.1-SNAPSHOT.jar user@server:/opt/tubestudy/
```

#### **Step 3: 서버에서 실행**
```bash
# 백그라운드 실행
nohup java -jar /opt/tubestudy/tracker-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  > /var/log/tubestudy/app.log 2>&1 &
```

#### **Step 4: 프로세스 확인**
```bash
# 실행 상태 확인
ps aux | grep tracker

# 포트 확인
netstat -tlnp | grep 8080

# 로그 확인
tail -f /var/log/tubestudy/app.log
```

---

### **옵션 2: Docker 배포**

#### **Dockerfile 생성** (`tracker/Dockerfile`)
```dockerfile
FROM openjdk:17-jdk-slim

# 작업 디렉토리
WORKDIR /app

# JAR 파일 복사
COPY target/tracker-0.0.1-SNAPSHOT.jar app.jar

# 데이터 디렉토리 생성
RUN mkdir -p /app/data

# 환경변수 설정
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### **docker-compose.yml 생성**
```yaml
version: '3.8'

services:
  tubestudy-backend:
    build: .
    container_name: tubestudy-backend
    ports:
      - "8080:8080"
    volumes:
      - ./data:/app/data
      - ./logs:/app/logs
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SERVER_PORT=8080
    restart: unless-stopped
```

#### **빌드 및 실행**
```bash
# 이미지 빌드
docker build -t tubestudy:latest .

# 컨테이너 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f tubestudy-backend
```

---

### **옵션 3: Kubernetes 배포** (고급)

#### **deployment.yaml**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: tubestudy-backend
  labels:
    app: tubestudy

spec:
  replicas: 2
  selector:
    matchLabels:
      app: tubestudy
  template:
    metadata:
      labels:
        app: tubestudy
    spec:
      containers:
      - name: tubestudy
        image: tubestudy:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
```

#### **배포**
```bash
kubectl apply -f deployment.yaml
kubectl get pods
kubectl logs -f deployment/tubestudy-backend
```

---

## 🎛️ Chrome 확장 배포

### **옵션 1: 개인 사용 (ZIP 파일)**

1. **ZIP 파일 준비**
   ```bash
   # 이미 생성됨: tube-study-extension.zip
   ```

2. **사용자 설치 방법**
   - Chrome 주소창: `chrome://extensions`
   - 개발자 모드 ON
   - "압축 해제된 확장 프로그램 로드" 클릭
   - 다운로드한 ZIP 파일 선택

---

### **옵션 2: Chrome 웹 스토어 등록 (공식 배포)**

#### **준비 사항**
- Google 개발자 계정 ($5 수수료)
- 확장 프로그램 아이콘 (128x128px)
- 스크린샷 (1280x800px)
- 개인정보 보호정책

#### **단계**
1. [Chrome 웹 스토어 개발자 대시보드](https://chrome.google.com/webstore/devconsole) 접속
2. **새 항목 추가** → ZIP 업로드
3. **상세 정보 입력**
   - 제목: "TubeStudy"
   - 설명: "YouTube 학습 진도 추적 및 딴짓 방지"
   - 카테고리: 생산성
4. **스크린샷/아이콘** 업로드
5. **검수 제출** (3-5일)
6. ✅ 승인 후 공개

---

## 📊 데이터베이스 설정

### **H2 파일 기반 (현재 설정)**
- ✅ 설정 없음 (자동)
- ✅ 경량
- ⚠️ 동시 다중 사용자 제한

### **MySQL 전환** (권장)

#### **Step 1: MySQL 설치**
```bash
# Ubuntu/Debian
sudo apt-get install mysql-server

# macOS (Homebrew)
brew install mysql
```

#### **Step 2: 데이터베이스 생성**
```sql
CREATE DATABASE tubestudy DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'tubestudy'@'localhost' IDENTIFIED BY 'secure_password';
GRANT ALL PRIVILEGES ON tubestudy.* TO 'tubestudy'@'localhost';
FLUSH PRIVILEGES;
```

#### **Step 3: application-prod.properties 수정**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/tubestudy
spring.datasource.username=tubestudy
spring.datasource.password=secure_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=validate
```

#### **Step 4: pom.xml에 MySQL 드라이버 추가**
```xml
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
</dependency>
```

---

## 🔐 보안 설정

### **1. HTTPS 설정**

#### **자체 서명 인증서 생성** (테스트용)
```bash
keytool -genkey -alias tubestudy -keyalg RSA -keystore keystore.jks -validity 365
```

#### **application-prod.properties**
```properties
server.ssl.key-store=keystore.jks
server.ssl.key-store-password=password
server.ssl.key-store-type=JKS
server.ssl.key-alias=tubestudy
```

### **2. CORS 설정**

#### **SecurityConfig.java** 생성
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/api/**").permitAll()
                .anyRequest().authenticated()
            .and()
            .cors()
                .configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(Arrays.asList(
                        "https://www.youtube.com",
                        "https://youtu.be"
                    ));
                    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
                    config.setAllowedHeaders(Arrays.asList("*"));
                    config.setAllowCredentials(true);
                    return config;
                });
        return http.build();
    }
}
```

### **3. Rate Limiting** (선택)

```java
@Component
public class RateLimitingFilter extends OncePerRequestFilter {
    private final LoadingCache<String, Integer> requestCounters = 
        CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build(new CacheLoader<String, Integer>() {
                public Integer load(String key) { return 0; }
            });

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain chain) 
            throws ServletException, IOException {
        String ip = request.getRemoteAddr();
        int count = requestCounters.getUnchecked(ip);
        
        if (count >= 100) { // 분당 100 요청 제한
            response.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
            return;
        }
        
        requestCounters.put(ip, count + 1);
        chain.doFilter(request, response);
    }
}
```

---

## 📝 모니터링 및 로깅

### **1. 애플리케이션 로깅**

#### **logback-spring.xml**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>/var/log/tubestudy/app.log</file>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>/var/log/tubestudy/app-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxFileSize>100MB</maxFileSize>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
    </appender>

    <root level="INFO">
        <appender-ref ref="FILE" />
    </root>
</configuration>
```

### **2. 헬스체크 엔드포인트**

```java
@RestController
@RequestMapping("/api/health")
public class HealthController {
    @GetMapping
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "timestamp", Instant.now().toString()
        ));
    }
}
```

#### **확인 방법**
```bash
curl http://localhost:8080/api/health
```

### **3. 모니터링 도구** (선택)

#### **Prometheus + Grafana**
```yaml
# docker-compose.yml 추가
prometheus:
  image: prom/prometheus:latest
  volumes:
    - ./prometheus.yml:/etc/prometheus/prometheus.yml
  ports:
    - "9090:9090"

grafana:
  image: grafana/grafana:latest
  ports:
    - "3000:3000"
```

---

## 🔧 트러블슈팅

### **1. 포트 이미 사용 중**
```bash
# 포트 사용 프로세스 확인
lsof -i :8080

# 프로세스 종료
kill -9 <PID>
```

### **2. 메모리 부족**
```bash
# JVM 메모리 설정
java -Xmx1024m -Xms512m -jar tracker-0.0.1-SNAPSHOT.jar
```

### **3. 데이터베이스 연결 실패**
```bash
# MySQL 서버 상태 확인
sudo systemctl status mysql

# MySQL 로그 확인
tail -f /var/log/mysql/error.log
```

### **4. Chrome 확장이 서버에 연결 못함**
```javascript
// content.js에서 확인
const SERVER_URL = 'http://your-domain.com:8080'; // 포트 확인
console.log('Sending data to:', SERVER_URL);
```

---

## 📅 배포 체크리스트

- [ ] JAR 파일 빌드 완료 (`mvn clean package`)
- [ ] application-prod.properties 설정 확인
- [ ] 데이터베이스 마이그레이션 완료
- [ ] SSL/HTTPS 인증서 준비
- [ ] CORS 설정 확인
- [ ] 로깅 시스템 설정
- [ ] 백업 전략 수립
- [ ] 모니터링 도구 설정
- [ ] 보안 감사 완료
- [ ] 성능 테스트 완료 (`ab`, `wrk` 등)
- [ ] 운영 문서 작성
- [ ] 팀 훈련 완료

---

## 🌐 배포 환경별 권장사항

### **AWS**
- EC2 인스턴스 (t3.medium 이상)
- RDS MySQL
- Application Load Balancer
- CloudWatch 로깅

### **Google Cloud**
- Cloud Run (Serverless)
- Cloud SQL
- Cloud Load Balancing

### **Azure**
- App Service
- Azure Database for MySQL
- Application Gateway

---

**마지막 업데이트**: 2025-11-23
