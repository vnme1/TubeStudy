
---

# TubeStudy: 유튜브 학습 진도 추적 및 딴짓 방지 대시보드

TubeStudy는 **유튜브 기반 온라인 학습자들을 위해 제작된 Chrome Extension + Spring Boot 백엔드 트래커 프로젝트**입니다.
유튜브 시청 정보를 5초 간격으로 수집·분석하여 **학습 진도 관리**, **딴짓 방지**, **대시보드 통계 분석**을 제공합니다.

---

## 🚀 주요 기능 (Key Features)

| 카테고리          | 기능 설명                                                   | 포트폴리오 강조점                         |
| ------------- | ------------------------------------------------------- | --------------------------------- |
| **실시간 진도 추적** | 유튜브 강의의 시청 시간, 진도율, 마지막 시청 위치를 **5초 간격으로 서버에 동기화**      | Client-Server 실시간 통신, 데이터의 연속성 관리 |
| **딴짓 방지 알림**  | 영상 제목에 *Vlog/게임/먹방* 등 딴짓 키워드 포함 시 Chrome 알림 발생          | 문제 해결 능력, 비즈니스 로직(딴짓 분류) 구현       |
| **대시보드 통계**   | 총 학습 시간, 주간 목표 달성률, 과목별(Java/Backend/CS 등) 분포를 시각화하여 제공 | 데이터 분석 및 시각화, Stream API 기반 통계 처리 |
| **학습 편의 기능**  | 최근 시청한 강의 카드, 마지막 시청 지점 링크 제공                           | UX/UI 개선, 데이터 기반 링크 생성 로직         |

---

## 🛠️ 기술 스택 (Tech Stack)

### **1. Backend & Data**

* **Spring Boot 3.0+**
* **Java 17**
* **Spring Data JPA**
* **H2 Database (개발용)**
* **Maven**

### **2. Frontend & Chrome Extension**

* **Chrome Extension Manifest V3**
* **JavaScript / HTML5 / CSS3**
* **Tailwind CSS (Dashboard UI)**
* **Docker (확장 예정)**

---

## ⚙️ 아키텍처 (Architecture)

TubeStudy는 **3-Tier 구조**를 기반으로 합니다.

### **1) Client (Chrome Extension)**

* content.js가 유튜브 페이지에서

  * 재생 시간
  * 제목
  * 진행률(progress)
    을 **5초 주기로 감지**
* `fetch()`를 통해 `/api/tracker/sync`로 비동기 전송

### **2) Server (Spring Boot)**

#### Controller

* 크롬 익스텐션의 요청 수신
* `TrackerService` 호출 → `SyncResponseDto` 반환
  (딴짓 여부 포함)

#### Service

* VideoProgress 데이터 저장
* 딴짓 여부 분석 (`analyzeDistraction`)
* Stream API 기반 대시보드 통계 계산 (`getDashboardStats()`)

#### Repository

* `VideoProgress` 엔티티 기반 JPA 영속성 관리
* H2 DB에 학습 기록 저장

---

## 💻 설치 및 실행 방법 (Installation & Run)

### **1️⃣ 백엔드 서버 실행**

```bash
# 프로젝트 클론 후
mvn clean install
java -jar target/tracker-0.0.1-SNAPSHOT.jar
```

또는 IDE에서 `TrackerApplication.java` 실행

> 서버 기본 주소: **[http://localhost:8085](http://localhost:8085)**

---

### **2️⃣ Chrome Extension 설치**

1. Chrome 실행
2. 주소창에 `chrome://extensions` 입력
3. 우측 상단 **개발자 모드 ON**
4. **“압축 해제된 확장 프로그램 로드”** 클릭
5. 프로젝트의 **extension 폴더** 선택

설치 후 TubeStudy 아이콘이 나타납니다.

---

### **3️⃣ 사용 방법**

* 유튜브 강의 시청
  → 5초마다 서버로 자동 동기화
* 딴짓 키워드 포함 영상 시
  → Chrome 알림 표시
* 학습 현황·통계를 보려면
  → **[http://localhost:8085](http://localhost:8085)** 접속
<img width="1899" height="1556" alt="image" src="https://github.com/user-attachments/assets/bb84b92a-efaf-450f-a888-6cac016faf37" />

---

## 💡 향후 확장 계획 (Future Plans)

* **기간별 통계 필터**
  (주간/월간 기간 필터링 기능)
* **사용자 설정 기능**
  (딴짓 키워드·주간 목표 시간 사용자 설정)
* **Docker 기반 배포 및 CI/CD 자동화**

---

