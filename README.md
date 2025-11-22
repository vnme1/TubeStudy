# TubeStudy: 유튜브 학습 진도 추적 및 딴짓 방지 대시보드

**TubeStudy**는 유튜브 기반 온라인 학습자들을 위한 **Chrome Extension + Spring Boot 백엔드 트래커**입니다.  
유튜브 시청 정보를 5초 간격으로 수집·분석하여 **학습 진도 관리**, **딴짓 방지**, **대시보드 통계 분석**을 제공합니다.

---

## 🎯 주요 기능 (Key Features)

### 1️⃣ **실시간 진도 추적**
- 유튜브 강의의 시청 시간, 진도율, 마지막 시청 위치를 **5초 간격으로 서버 동기화**
- 브라우저 종료 후에도 **마지막 시청 지점 복구**
- Portfolio 강조: Client-Server 실시간 통신, 데이터 영속성 관리

### 2️⃣ **딴짓 방지 & 키워드 관리**
- 영상 제목에서 **사용자 정의 키워드 감지** (게임, 먹방, Vlog 등)
- 감지 시 **Chrome 알림 + 음성 알림** (한글 지원, 음성 속도 조절 가능)
- **CRUD 인터페이스**로 키워드 동적 추가/삭제/토글
- 기본 6개 키워드 자동 초기화

### 3️⃣ **대시보드 통계 분석**
- **총 학습 시간**, **주간 목표 달성률**, **과목별 분포** 시각화
- **기간별 필터** (오늘 / 이번주 / 이번달 / 전체)
- Stream API 기반 통계 계산, 실시간 업데이트

### 4️⃣ **UI/UX 개선**
- 🌙 **다크/라이트 모드 토글** - localStorage 기반 자동 복구
- ✨ **CSS 애니메이션** (fadeIn, slideIn, cardHover, pulse, spin)
- 🔊 **음성 알림** - Web Speech API, 한글 완벽 지원
- 📊 **반응형 대시보드** - Tailwind CSS 기반

### 5️⃣ **데이터 관리**
- 📥 **CSV 내보내기** - UTF-8 BOM 인코딩으로 한글 정상 처리
- 🗑️ **전체 데이터 삭제** - 한 번에 모든 기록 제거 기능
- 🔄 **설정 동기화** - 여러 탭에서 실시간 동기화

---

## 🛠️ 기술 스택 (Tech Stack)

### **Backend**
- **Framework**: Spring Boot 3.5.9-SNAPSHOT
- **Language**: Java 17
- **Data**: Spring Data JPA, Hibernate
- **Database**: H2 (파일 기반, 개발/배포 공용)
- **Build**: Maven

### **Frontend & Extension**
- **Extension**: Chrome Manifest V3
- **Frontend**: Vanilla JavaScript, HTML5, CSS3
- **Styling**: Tailwind CSS
- **API Communication**: Fetch API (비동기)
- **Storage**: localStorage (설정 저장)
- **Audio**: Web Speech API (한글 음성)

### **Architecture**
```
┌─────────────────────────────────────────────────┐
│          YouTube.com                            │
│  (Chrome Extension - content.js 주입)           │
│  5초 간격 데이터 수집                            │
└────────────────┬────────────────────────────────┘
                 │ Fetch API (JSON)
                 ▼
┌─────────────────────────────────────────────────┐
│  Spring Boot Backend (localhost:18085)          │
│  ┌──────────────────────────────────────────┐  │
│  │ Controller (TrackerController)           │  │
│  │ - POST /api/tracker/sync (실시간 동기화) │  │
│  │ - GET /api/dashboard/stats (통계)       │  │
│  │ - GET /api/tracker/export/csv (내보내기)│  │
│  └──────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────┐  │
│  │ Service Layer                            │  │
│  │ - TrackerService (진도 추적 로직)        │  │
│  │ - SettingsService (설정 관리)            │  │
│  │ - CsvExportService (CSV 인코딩)         │  │
│  └──────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────┐  │
│  │ JPA Repository & H2 Database             │  │
│  │ - VideoProgress (학습 기록)              │  │
│  │ - Settings (설정값)                      │  │
│  │ - DistractionKeyword (커스텀 키워드)    │  │
│  └──────────────────────────────────────────┘  │
└─────────────────────────────────────────────────┘
```

---

## 💻 설치 및 실행 가이드 (Installation & Run Guide)

### **사전 요구사항**
- Java 17 이상
- Maven 3.8+
- Chrome 브라우저 (Manifest V3 지원)
- 포트 18085 사용 가능

### **1️⃣ Backend 서버 실행**

#### **옵션 A: IDE에서 실행 (추천)**
1. 프로젝트를 IDE(IntelliJ/VS Code)에서 열기
2. `src/main/java/com/tubestudy/tracker/TrackerApplication.java` 우클릭
3. **Run** 선택

#### **옵션 B: 터미널에서 실행**
```bash
# 프로젝트 디렉토리에서
mvn spring-boot:run
```

#### **옵션 C: JAR 파일로 배포**
```bash
# 패키징
mvn clean package -DskipTests

# 실행
java -jar target/tracker-0.0.1-SNAPSHOT.jar
```

**✅ 서버 정상 시작 확인:**
- 콘솔 로그에 `Started TrackerApplication in X.XXX seconds` 메시지 표시
- 데이터베이스: `./data/tubestudy_db` 파일 생성됨

---

### **2️⃣ Chrome 확장프로그램 설치**

1. **Chrome 브라우저 열기**
2. 주소창에 **`chrome://extensions`** 입력 후 이동
3. **우측 상단** → **"개발자 모드"** 활성화 (토글 ON)
4. **"압축 해제된 확장 프로그램 로드"** 버튼 클릭
5. 프로젝트 폴더 내 **`tube-study-extension`** 디렉토리 선택
6. 설치 완료! 🎉 Chrome 우측 상단에 TubeStudy 아이콘 표시됨

**🔧 확장프로그램 설정:**
- Chrome 우측 상단 TubeStudy 아이콘 우클릭
- **"옵션"** → 서버 주소 확인 (기본값: `http://localhost:18085`)
- 필요시 수정 후 저장

---

### **3️⃣ 대시보드 접속**

브라우저에서 아래 주소로 접속:
```
http://localhost:18085
```

**📊 대시보드 페이지:**
- **study.html** - 학습 현황 대시보드 (기간별 통계)
- **settings.html** - 설정 패널 (다크모드, 음성알림, 키워드 관리 등)

---

## 🎬 사용 방법 (How to Use)

### **1단계: YouTube에서 강의 시청**
1. YouTube에서 학습 영상 시작
2. 자동으로 5초마다 서버에 시청 정보 전송
3. 딴짓 키워드 감지 시 Chrome 알림 표시

### **2단계: 시청 데이터 확인**
- `http://localhost:18085/study.html` 접속
- 📈 **오늘/이번주/이번달별** 학습 시간 확인
- 🎯 **주간 목표** 달성률 보기
- 🏆 **연속 시청 스트릭** 확인

### **3단계: 설정 관리**
- `http://localhost:18085/settings.html` 접속
- **다크/라이트 모드** 토글
- **음성 알림** 활성화 (선택)
- **애니메이션** 효과 토글
- **커스텀 키워드** 추가/삭제/토글
- **목표 시간** 설정

### **4단계: 데이터 내보내기**
- 설정 페이지에서 **"학습 기록 CSV로 내보내기"** 클릭
- `study_records_YYYY-MM-DD.csv` 파일 자동 다운로드
- Excel/Google Sheets에서 열어서 분석 가능

---

## 📁 프로젝트 구조 (Project Structure)

```
tracker/
├── src/
│   ├── main/
│   │   ├── java/com/tubestudy/tracker/
│   │   │   ├── TrackerApplication.java          # 메인 진입점
│   │   │   ├── controller/
│   │   │   │   ├── TrackerController.java       # 진도 추적 API
│   │   │   │   ├── DashboardController.java     # 통계 API
│   │   │   │   └── SettingsController.java      # 설정 API
│   │   │   ├── service/
│   │   │   │   ├── TrackerService.java          # 핵심 로직
│   │   │   │   ├── SettingsService.java         # 설정 관리
│   │   │   │   └── CsvExportService.java        # CSV 내보내기
│   │   │   ├── repository/
│   │   │   │   ├── VideoProgressRepository.java
│   │   │   │   ├── SettingsRepository.java
│   │   │   │   └── DistractionKeywordRepository.java
│   │   │   ├── entity/
│   │   │   │   ├── VideoProgress.java           # 학습 기록
│   │   │   │   ├── Settings.java                # 사용자 설정
│   │   │   │   └── DistractionKeyword.java      # 커스텀 키워드
│   │   │   └── dto/                             # Data Transfer Objects
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── study.html                   # 대시보드
│   │       │   └── settings.html                # 설정 페이지
│   │       └── application.properties
│   └── test/
│       └── java/.../TrackerApplicationTests.java
│
├── tube-study-extension/
│   ├── manifest.json                            # Chrome 확장 설정
│   └── content.js                               # YouTube 페이지 스크립트
│
├── data/
│   └── tubestudy_db (H2 데이터베이스 파일)
│
├── pom.xml                                      # Maven 의존성
└── README.md                                    # 이 파일
```

---

## 🔌 API 엔드포인트 (API Endpoints)

### **Core APIs**

| 메서드 | 엔드포인트                      | 설명                              |
|--------|--------------------------------|----------------------------------|
| `POST` | `/api/tracker/sync`            | 영상 재생 정보 동기화 (5초마다)  |
| `GET`  | `/api/dashboard/stats`         | 대시보드 통계 조회               |
| `GET`  | `/api/tracker/export/csv`      | 학습 기록 CSV 다운로드           |
| `DELETE`| `/api/tracker/clear-all`       | 모든 학습 기록 삭제              |

### **Settings APIs**

| 메서드 | 엔드포인트                      | 설명                              |
|--------|--------------------------------|----------------------------------|
| `GET`  | `/api/settings`                | 현재 설정값 조회                 |
| `PUT`  | `/api/settings`                | 설정 업데이트                    |
| `GET`  | `/api/settings/keywords`       | 커스텀 키워드 목록               |
| `POST` | `/api/settings/keywords`       | 새 키워드 추가                   |
| `PUT`  | `/api/settings/keywords/:id`   | 키워드 토글/수정                 |
| `DELETE`| `/api/settings/keywords/:id`   | 키워드 삭제                      |

---

## 🎨 UI/UX 특징 (UI/UX Features)

### **1. 다크/라이트 모드**
- 설정에서 토글로 전환
- localStorage에 자동 저장
- 모든 요소에 완벽하게 적용 (배경, 텍스트, 카드, 입력창 등)

### **2. 애니메이션**
- **fadeIn**: 페이지 요소 부드러운 진입
- **slideIn**: 카드 측면 슬라이드
- **cardHover**: 마우스 호버 시 카드 강조
- **pulse**: 중요 정보 강조 효과
- **spin**: 로딩 표시

### **3. 음성 알림**
- 한글 텍스트-음성 변환 (ko-KR)
- 어음 속도: 1.2배 (명확함)
- 딴짓 감지 & 목표 달성 시 자동 재생

### **4. 반응형 디자인**
- 모바일, 태블릿, 데스크톱 모두 지원
- Tailwind CSS 기반 유연한 레이아웃

---

## 🚀 배포 및 확장 계획 (Deployment & Future Plans)

### **단기 (1-2주)**
- ✅ 한글 인코딩 수정 (CSV UTF-8 BOM)
- ✅ 다크/라이트 모드 완성
- ✅ 음성 알림 구현
- ✅ 커스텀 키워드 CRUD

### **중기 (1-2개월)**
- 📦 **Docker 컨테이너화** - 간편 배포
- 🔐 **사용자 인증** - 로그인/회원가입
- 📱 **모바일 앱** - React Native
- 🌐 **클라우드 배포** - AWS/Google Cloud

### **장기**
- 🤖 **AI 기반 학습 패턴 분석** - TensorFlow
- 📊 **고급 통계 대시보드** - Chart.js/D3.js
- 🏫 **팀 기능** - 학습 공유, 랭킹
- 💾 **데이터 백업** - Google Drive 연동

---

## 🐛 문제 해결 (Troubleshooting)

### **Q1: "Connection refused" 에러**
- ✅ 백엔드 서버 실행 확인
- ✅ 포트 18085가 사용 중이 아닌지 확인
- ✅ 방화벽 설정 확인

### **Q2: Chrome 확장 로드 안 됨**
- ✅ `chrome://extensions` 개발자 모드 활성화
- ✅ `tube-study-extension` 폴더의 `manifest.json` 확인
- ✅ 폴더 경로 공백 제거

### **Q3: 한글이 깨져서 보임**
- ✅ 데이터베이스 인코딩: H2는 기본 UTF-8 지원
- ✅ CSV 내보내기: UTF-8 BOM 자동 추가됨
- ✅ 브라우저 개발자 도구에서 콘솔 에러 확인

### **Q4: 데이터가 저장 안 됨**
- ✅ `./data/tubestudy_db` 파일 권한 확인
- ✅ 로그에 SQL 에러 메시지 확인
- ✅ 서버 재시작 후 재시도

---

## 📄 라이선스 (License)

MIT License - 자유롭게 사용, 수정, 배포 가능

---

## 👤 작성자 (Author)

**GitHub**: [@vnme1](https://github.com/vnme1)  
**프로젝트**: [TubeStudy](https://github.com/vnme1/TubeStudy)

---

## 📞 지원 (Support)

문제 발생 시:
1. GitHub Issues 페이지에 이슈 등록
2. 설명: 문제 현상, 재현 방법, 환경 정보
3. 로그: 서버/브라우저 콘솔 에러 메시지 포함

---

**마지막 업데이트**: 2025-11-23
