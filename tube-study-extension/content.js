// =======================================================
// tube-study-extension/content.js
// 학습 진도 데이터를 추출하고 서버로 전송하는 스크립트
// =======================================================

// 마지막으로 서버에 전송했던 재생 위치 (초 단위)
let lastSentProgress = 0; 

// 마지막으로 서버에 전송했던 시점의 타임스탬프 (밀리초)
let lastSentTimestamp = 0;

// 1. 유튜브 영상 관련 데이터를 추출하는 함수
function getVideoData() {
  // 현재 재생 중인 비디오 요소 찾기
  const video = document.querySelector("video");

  // 비디오가 없거나 재생 중이 아니면 빈 객체 반환
  if (!video || video.paused || video.ended) {
    return {};
  }

  // 영상 제목, 채널명 추출
  const titleElement = document.querySelector(
    "h1.style-scope.ytd-watch-metadata, h1.style-scope.ytd-video-primary-info-renderer"
  );
  const channelElement = document.querySelector("ytd-channel-name a");

  const videoId = new URLSearchParams(window.location.search).get("v");
  const currentTime = video.currentTime;
  const duration = video.duration;

  return {
    videoId: videoId,
    title: titleElement ? titleElement.innerText.trim() : "Unknown Title",
    channel: channelElement
      ? channelElement.innerText.trim()
      : "Unknown Channel",
    currentTime: currentTime,
    duration: duration,
    // 현재 탭의 URL을 저장하여 나중에 대시보드에서 링크로 사용 가능
    url: window.location.href,
  };
}

// 2. 서버로 데이터 전송하는 함수 (async/await 및 응답 처리)
// 2. 서버로 데이터 전송하는 함수 (async/await 및 응답 처리)
async function sendDataToServer() {
    const data = getVideoData(); // data 객체는 { videoId, title, channel, currentTime, duration, url } 포함

    // 영상 ID와 길이가 있을 때만 서버로 전송
    if (data.videoId && data.duration > 0) {
        
        // ----------------------------------------------------
        // ✅ 1. 실제 시청 시간(accumulatedStudySeconds) 계산 로직 (핵심)
        // ----------------------------------------------------
        let accumulatedStudySeconds = 0;
        
        // **A. 초기화 및 첫 전송 처리**
        if (lastSentProgress === 0) {
            lastSentProgress = data.currentTime;
            lastSentTimestamp = Date.now();
            // 첫 전송은 학습 시간 누적 없이 현재 진도만 기록
            accumulatedStudySeconds = 0; 
        } else {
            // **B. 누적 학습 시간 계산**
            
            // 실제 경과된 시간 (초)
            const timeElapsed = (Date.now() - lastSentTimestamp) / 1000; 
            
            // 재생 위치의 변화량 (현재 위치 - 이전 전송 위치)
            const progressChange = data.currentTime - lastSentProgress;
            
            // 🚨 유효성 검사: 시청으로 인정되는 조건
            // 1. 앞으로 재생되었고 (progressChange > 0)
            // 2. 변화량이 경과된 시간의 2.0배 이하여야 함 (배속 재생 및 작은 오차 허용)
            if (progressChange > 0 && progressChange <= timeElapsed * 2.0) {
                // 재생 위치의 변화량(progressChange)을 실제 학습 시간으로 간주
                accumulatedStudySeconds = progressChange;
            } else {
                // 뒤로 감기, 앞으로 크게 건너뛰기, 탭 비활성화 등으로 인한 큰 시간차는 0으로 처리
                accumulatedStudySeconds = 0; 
            }
        }
        
        // ----------------------------------------------------
        // 2. 서버 DTO 형식에 맞춰 객체 생성
        // ----------------------------------------------------
        const progressDto = {
            videoId: data.videoId,
            title: data.title,
            channel: data.channel,
            totalDurationSeconds: data.duration, // 서버 필드명에 맞게 변경
            lastProgressSeconds: data.currentTime, // 서버 필드명에 맞게 변경
            // ✅ 새로 추가된 필드: 이번 동기화 간격 동안 실제로 시청한 시간
            accumulatedStudySeconds: accumulatedStudySeconds 
        };

        // 디버깅을 위한 콘솔 로그 (서버 전송 확인용)
        console.log(
            `[TubeStudy] 전송 중: ${progressDto.title} | ${Math.floor(progressDto.lastProgressSeconds)}/${Math.floor(progressDto.totalDurationSeconds)}s | 학습 시간: ${accumulatedStudySeconds.toFixed(2)}s`
        );
        
        try {
            // 포트 18085로 전송
            const response = await fetch("http://localhost:18085/api/tracker/sync", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(progressDto), // 수정된 DTO 전송
            });

            if (response.ok) {
                const syncResponse = await response.json();

                // 딴짓 방지 로직 실행
                if (syncResponse.requiresNotification) {
                    console.warn(
                        `[TubeStudy] 딴짓 감지! 메시지: ${syncResponse.message}`
                    );
                    showDistractionAlert(syncResponse.message);
                }
            } else {
                console.error("서버 응답 오류:", response.status);
            }
        } catch (err) {
            // 서버 오프라인 에러 무시
        }

        // 3. 다음 동기화를 위해 현재 상태를 저장
        lastSentProgress = data.currentTime;
        lastSentTimestamp = Date.now();
    }
}

// 3. 경고 메시지를 유튜브 페이지에 직접 삽입하는 함수 + 음성 알림
function showDistractionAlert(message) {
  // 1. 기존 알림이 있다면 제거
  let existingAlert = document.getElementById("tubestudy-alert");
  if (existingAlert) existingAlert.remove();

  // 2. 새로운 알림 요소 생성
  const alertDiv = document.createElement("div");
  alertDiv.id = "tubestudy-alert";
  alertDiv.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background-color: #cc0000; /* YouTube Red */
        color: white;
        padding: 15px 20px;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.4);
        z-index: 99999;
        font-size: 16px;
        font-weight: bold;
        animation: fadeInOut 1s ease-in-out;
        cursor: pointer;
    `;
  alertDiv.innerHTML = `
        🚨 집중 경고!
        <div style="font-weight: normal; font-size: 14px; margin-top: 5px;">${message}</div>
    `;

  // 3. 클릭하면 사라지도록 설정
  alertDiv.onclick = () => alertDiv.remove();

  // 4. 페이지에 추가
  document.body.appendChild(alertDiv);

  // 5. 음성 알림 재생
  playVoiceAlert(`집중 경고! ${message}`);

  // 6. 8초 후 자동으로 사라지도록 설정
  setTimeout(() => {
    if (document.getElementById("tubestudy-alert")) {
      document.getElementById("tubestudy-alert").remove();
    }
  }, 8000);
}

/**
 * 음성 알림 재생 함수
 * @param {string} message - 읽어줄 메시지
 */
function playVoiceAlert(message) {
  if ('speechSynthesis' in window) {
    const utterance = new SpeechSynthesisUtterance(message);
    utterance.lang = 'ko-KR';
    utterance.rate = 1.2;
    utterance.pitch = 1;
    utterance.volume = 1;
    speechSynthesis.speak(utterance);
  }
}

// 4. 5초마다 데이터 전송 로직 실행
// 이 주기는 서버의 누적 학습 시간 계산 로직(15초 이내)과 연관되어 있습니다.
setInterval(sendDataToServer, 5000); // 5000ms = 5초
