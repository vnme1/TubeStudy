# 💾 TubeStudy 설치 및 사용 가이드 (Installation & User Guide)

완벽한 단계별 설치 및 사용 방법 가이드입니다.

---

## 📋 목차

1. [시스템 요구사항](#시스템-요구사항)
2. [설치 전 준비](#설치-전-준비)
3. [Backend 서버 설치](#backend-서버-설치)
4. [Chrome 확장 설치](#chrome-확장-설치)
5. [첫 실행 및 설정](#첫-실행-및-설정)
6. [기본 사용법](#기본-사용법)
7. [상세 기능 설명](#상세-기능-설명)
8. [자주 묻는 질문 (FAQ)](#자주-묻는-질문-faq)

---

## 🖥️ 시스템 요구사항

### **필수 요구사항**
| 항목 | 최소 사양 | 권장 사양 |
|------|---------|---------|
| **OS** | Windows 10, macOS 10.14, Ubuntu 18.04+ | Windows 11, macOS 12+, Ubuntu 20.04+ |
| **Java** | Java 17 | Java 17+ |
| **RAM** | 2GB | 4GB |
| **디스크** | 500MB | 1GB |
| **브라우저** | Chrome 90+ | Chrome 최신 버전 |
| **인터넷** | 필수 | 필수 |

### **네트워크**
- 포트 18085: Backend 서버 (변경 가능)
- 포트 35729: LiveReload (개발 모드)

---

## ⚙️ 설치 전 준비

### **Step 1: Java 17 설치 확인**

#### **Windows**
```powershell
# 명령 프롬프트 또는 PowerShell에서
java -version

# 출력 예시:
# openjdk version "17.0.1"
```

Java 17이 없으면:
- [OpenJDK 다운로드](https://jdk.java.net/17/)
- 또는 [Adoptium (Eclipse Temurin)](https://adoptium.net/)에서 설치

#### **macOS**
```bash
java -version

# 없으면:
brew install openjdk@17
```

#### **Linux**
```bash
sudo apt update
sudo apt install openjdk-17-jdk

# 버전 확인
java -version
```

---

### **Step 2: Maven 설치 확인**

#### **모든 OS**
```bash
mvn -version

# 출력 예시:
# Apache Maven 3.8.6
```

Maven이 없으면:
- [Maven 공식 사이트](https://maven.apache.org/download.cgi)에서 다운로드
- 또는 패키지 매니저로 설치

```bash
# macOS
brew install maven

# Linux
sudo apt install maven

# Windows
choco install maven
```

---

### **Step 3: Git 설치 (선택)**

프로젝트를 git으로 받으려면:
```bash
# 버전 확인
git --version

# 없으면: https://git-scm.com/ 다운로드
```

---

## 🚀 Backend 서버 설치

### **방법 1: IDE에서 실행 (추천 - 초보자)**

#### **IntelliJ IDEA**
1. **IntelliJ IDEA 설치**
   - [공식 사이트](https://www.jetbrains.com/idea/)
   - Community Edition 무료 버전으로 충분

2. **프로젝트 열기**
   - IntelliJ 메뉴: File → Open
   - `tracker` 폴더 선택
   - "pom.xml"을 Project File로 인식 → Open as Project

3. **프로젝트 로드**
   - 자동으로 Maven 의존성 다운로드
   - 우측 하단 "Indexing..." 완료 대기

4. **실행**
   ```
   src/main/java/com/tubestudy/tracker/TrackerApplication.java 우클릭
   → Run 'TrackerApplication'
   ```

#### **VS Code**
1. **VS Code 설치** + 확장 프로그램
   - Extension: "Extension Pack for Java" 설치

2. **프로젝트 열기**
   - File → Open Folder
   - `tracker` 폴더 선택

3. **실행**
   - Explorer 에서 `TrackerApplication.java` 우클릭
   - "Run" 클릭

---

### **방법 2: 터미널에서 실행 (빠른 방법)**

#### **Git으로 프로젝트 다운로드** (선택)
```bash
git clone https://github.com/vnme1/TubeStudy.git
cd TubeStudy/tracker
```

#### **또는 ZIP 다운로드**
- GitHub 저장소에서 "Code" → "Download ZIP"
- 압축 해제 후 폴더 진입

#### **실행**
```bash
# 프로젝트 디렉토리에서
mvn spring-boot:run
```

**✅ 정상 실행 확인:**
```
Started TrackerApplication in X.XXX seconds (process running for Y.YYY)
```

---

### **방법 3: 실행 가능한 JAR로 배포**

#### **빌드**
```bash
mvn clean package -DskipTests
```

#### **실행**
```bash
java -jar target/tracker-0.0.1-SNAPSHOT.jar
```

---

### **Step 4: 백그라운드 실행 (선택)**

#### **Windows - 배치 파일 생성**
```batch
@echo off
title TubeStudy Backend
cd /d C:\path\to\tracker
java -jar target/tracker-0.0.1-SNAPSHOT.jar
pause
```

`run_server.bat` 저장 후 더블클릭

#### **macOS/Linux - 쉘 스크립트**
```bash
#!/bin/bash
cd /path/to/tracker
nohup java -jar target/tracker-0.0.1-SNAPSHOT.jar > server.log 2>&1 &
echo "Server started"
```

`chmod +x run_server.sh` 후 실행

---

## 🎯 Chrome 확장 설치

### **Step 1: 확장 파일 준비**

#### **옵션 A: ZIP 파일 이용** (권장)
- `tube-study-extension.zip` 다운로드 완료됨
- 임시 폴더에 압축 해제
  ```
  Downloads/tube-study-extension/
    ├── manifest.json
    └── content.js
  ```

#### **옵션 B: GitHub에서 다운로드**
- 저장소 폴더 직접 사용

---

### **Step 2: Chrome에 로드**

1. **Chrome 브라우저 열기**

2. **주소창에 입력**
   ```
   chrome://extensions
   ```

3. **개발자 모드 활성화**
   - 우측 상단 토글 스위치 "개발자 모드" ON
   ```
   👤 프로필 아이콘 ← 개발자 모드 스위치 (우측 상단)
   ```

4. **"압축 해제된 확장 프로그램 로드" 클릭**
   - 해제된 `tube-study-extension` 폴더 선택

5. ✅ **설치 완료**
   - Chrome 우측 상단에 TubeStudy 아이콘 표시됨
   ```
   🧩 TubeStudy (활성화됨)
   ```

---

### **Step 3: 확장 설정**

1. **TubeStudy 아이콘 우클릭**
   ```
   ┌─────────────────────┐
   │ 확장 항목           │
   │ ─────────────────── │
   │ ✓ 이 사이트에서 실행│
   │ 옵션                │
   │ 관리                │
   └─────────────────────┘
   ```

2. **"옵션" 클릭**
   - 확장 설정 페이지 열림

3. **서버 주소 확인/수정**
   - 기본값: `http://localhost:18085`
   - 변경 시 "저장" 클릭

---

## 🔑 첫 실행 및 설정

### **Step 1: 대시보드 접속**

브라우저에서 접속:
```
http://localhost:18085
```

### **Step 2: 기본 설정 (선택)**

**settings.html에서 설정:**

1. **다크/라이트 모드**
   - 토글로 전환
   - 선호도에 따라 선택

2. **음성 알림** (선택)
   - 활성화 시 딴짓 감지 & 목표 달성 시 음성 안내

3. **애니메이션** (선택)
   - 페이지 로딩 시 부드러운 애니메이션
   - 성능 중시 시 비활성화

4. **주간 목표 시간** 설정
   - 예: 주 30시간
   - 통계에 반영됨

5. **커스텀 키워드** 추가 (선택)
   - "추가" 입력 후 Enter
   - 게임, 쇼핑, SNS 등 딴짓 키워드 등록

---

## 📚 기본 사용법

### **1. 유튜브 학습 시작**

```
1️⃣ YouTube 접속
  ↓
2️⃣ 학습 영상 시작
  ↓
3️⃣ 자동 추적 시작 (5초마다)
  ↓
4️⃣ 딴짓 감지 시 알림 표시
```

### **2. 진도 확인**

**study.html 접속:**
```
http://localhost:18085/study.html
```

**보기:**
- 📊 **오늘**: 오늘 학습한 총 시간
- 📈 **이번주**: 주간 통계 + 목표 대비
- 📅 **이번달**: 월간 통계
- 🏆 **연속 시청**: 현재 연속 일수

### **3. 설정 변경**

**settings.html에서:**
- 다크/라이트 모드 토글
- 음성 알림 활성화/비활성화
- 키워드 추가/삭제/토글
- 목표 시간 수정

### **4. 데이터 내보내기**

**settings.html에서:**
```
[학습 기록 CSV로 내보내기] 클릭
  ↓
study_records_2025-11-23.csv 다운로드
  ↓
Excel/Google Sheets에서 열기
```

---

## 🎨 상세 기능 설명

### **대시보드 (study.html)**

#### **통계 카드**
```
┌──────────────────────────────────────┐
│  📊 오늘의 학습 시간                  │
│     2시간 30분                       │
│                                      │
│  🎯 주간 목표 달성률                 │
│     65% (15시간/30시간)              │
│                                      │
│  🏆 연속 시청 날짜                   │
│     7일 연속!                        │
└──────────────────────────────────────┘
```

#### **기간별 필터**
- **Today**: 오늘 (00:00 ~ 현재)
- **This Week**: 이번주 (월 00:00 ~ 현재)
- **This Month**: 이번달 (1일 00:00 ~ 현재)
- **All Time**: 전체 기간

---

### **설정 패널 (settings.html)**

#### **테마 설정**
```
🌙 다크 모드 토글
   ↳ ON: 어두운 배경, 밝은 텍스트
   ↳ OFF: 밝은 배경, 어두운 텍스트
```

#### **알림 설정**
```
🔊 음성 알림 토글
   ↳ ON: 딴짓/목표달성 시 음성 안내
   ↳ OFF: 무음

✨ 애니메이션 토글
   ↳ ON: 페이지 로딩 시 부드러운 애니메이션
   ↳ OFF: 즉시 로드
```

#### **목표 설정**
```
⏱️ 주간 학습 목표
   입력 필드: 시간 단위
   예) 30 입력 → "주 30시간"
```

#### **키워드 관리**
```
🎮 딴짓 키워드 목록

기본 키워드 (수정 불가):
├─ 게임 (토글 가능)
├─ 먹방 (토글 가능)
├─ Vlog (토글 가능)
├─ 웹소설 (토글 가능)
├─ SNS (토글 가능)
└─ 쇼핑 (토글 가능)

추가 키워드:
├─ [새 키워드 추가] 입력 필드
└─ + 버튼으로 추가
```

---

## ❓ 자주 묻는 질문 (FAQ)

### **Q1: 서버가 시작 안 됨**

**A: 이렇게 확인하세요**
- ✅ Java 17 설치 확인
  ```bash
  java -version
  ```
- ✅ 포트 18085 사용 가능 확인
  ```bash
  # Windows
  netstat -ano | findstr 18085
  # macOS/Linux
  lsof -i :18085
  ```
- ✅ 데이터 폴더 권한 확인
  ```bash
  # data 폴더 쓰기 권한 필요
  ls -ld data/
  ```

---

### **Q2: Chrome 확장이 로드 안 됨**

**A: 확인 사항**
- ✅ `manifest.json` 파일 존재 확인
- ✅ 개발자 모드 ON 확인
- ✅ 폴더 경로에 한글/공백 없는지 확인
- ✅ Chrome 콘솔에 에러 메시지 확인
  ```
  Chrome 우클릭 → 검사 → Console 탭
  ```

---

### **Q3: 데이터가 저장 안 됨**

**A: 확인 순서**
1. 서버 콘솔 에러 확인
2. 브라우저 개발자 도구 (F12) → Console 에러 확인
3. 서버 재시작
4. 브라우저 캐시 삭제 (Ctrl+Shift+Delete)

---

### **Q4: 한글이 깨져서 보임**

**A: 원인과 해결**
- ✅ 브라우저 인코딩 확인: Ctrl+, → 언어 확인
- ✅ CSV 내보내기: UTF-8 BOM 자동 추가
- ✅ 터미널 인코딩: UTF-8로 설정

---

### **Q5: 딴짓 감지가 안 됨**

**A: 확인 사항**
1. YouTube 탭에서 확장 권한 확인
   ```
   TubeStudy 아이콘 우클릭 → "이 사이트에서 실행"
   ```
2. 키워드 설정 확인 (토글 ON)
3. 음성 알림 설정 확인
4. 콘솔 에러 확인

---

### **Q6: 포트 18085 이미 사용 중**

**A: 해결 방법**

#### **기존 프로세스 종료**
```bash
# Windows
taskkill /PID <PID> /F

# macOS/Linux
kill -9 <PID>
```

#### **다른 포트 사용**
```bash
# application.properties 수정
server.port=8080
```

---

### **Q7: 메모리 부족 에러**

**A: JVM 메모리 증가**
```bash
java -Xmx512m -Xms256m -jar target/tracker-0.0.1-SNAPSHOT.jar
```
- `-Xmx`: 최대 메모리 (512MB)
- `-Xms`: 초기 메모리 (256MB)

---

### **Q8: 성능이 느림**

**A: 최적화 방법**
1. 애니메이션 비활성화
2. 음성 알림 비활성화
3. 브라우저 탭 수 줄이기
4. 로컬 MySQL 데이터베이스 사용 (H2 대신)

---

## 📞 추가 지원

### **문제 해결**
- GitHub Issues: [TubeStudy Issues](https://github.com/vnme1/TubeStudy/issues)
- 상세한 에러 메시지와 함께 이슈 등록

### **문서**
- README.md: 프로젝트 개요
- DEPLOYMENT.md: 배포 가이드
- 이 파일: 설치 및 사용 가이드

---

**마지막 업데이트**: 2025-11-23  
**버전**: 1.0.0
