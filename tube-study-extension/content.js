// =======================================================
// tube-study-extension/content.js
// 학습 진도 데이터를 추출하고 서버로 전송하는 스크립트
// =======================================================

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
async function sendDataToServer() {
  const data = getVideoData();

  // 영상 ID와 길이가 있을 때만 서버로 전송
  if (data.videoId && data.duration > 0) {
    // 디버깅을 위한 콘솔 로그 (서버 전송 확인용)
    console.log(
      `[TubeStudy] 전송 중: ${data.title} (${Math.floor(
        data.currentTime
      )}/${Math.floor(data.duration)}s)`
    );

    try {
      // 포트 18085로 전송 (수정된 포트)
      const response = await fetch("http://localhost:18085/api/tracker/sync", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data),
      });

      if (response.ok) {
        const syncResponse = await response.json(); // 서버 응답 JSON 파싱

        // 딴짓 방지 로직 실행
        if (syncResponse.requiresNotification) {
          console.warn(
            `[TubeStudy] 딴짓 감지! 메시지: ${syncResponse.message}`
          );
          // 알림 함수 호출 (alert() 대신 인-페이지 모달 사용)
          showDistractionAlert(syncResponse.message);
        }
      } else {
        console.error("서버 응답 오류:", response.status);
      }
    } catch (err) {
      // 서버가 꺼져있을 때 에러 로그 무시
      // console.log("[TubeStudy] 서버 오프라인 또는 연결 오류");
    }
  }
}

// 3. 경고 메시지를 유튜브 페이지에 직접 삽입하는 함수
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

  // 5. 8초 후 자동으로 사라지도록 설정
  setTimeout(() => {
    if (document.getElementById("tubestudy-alert")) {
      document.getElementById("tubestudy-alert").remove();
    }
  }, 8000);
}

// 4. 5초마다 데이터 전송 로직 실행
// 이 주기는 서버의 누적 학습 시간 계산 로직(15초 이내)과 연관되어 있습니다.
setInterval(sendDataToServer, 5000); // 5000ms = 5초
