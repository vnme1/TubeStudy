package com.tubestudy.tracker.service;

import com.tubestudy.tracker.dto.ContinueWatchingDto;
import com.tubestudy.tracker.dto.SyncResponseDto;
import com.tubestudy.tracker.dto.VideoProgressDto;
import com.tubestudy.tracker.dto.CourseItemDto;
import com.tubestudy.tracker.dto.DashboardStatsDto;
//import com.tubestudy.tracker.dto.DashboardStatsDto.SubjectStatDto;
import com.tubestudy.tracker.entity.VideoProgress;
import com.tubestudy.tracker.repository.VideoProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor // Repository 자동 주입을 위한 Lombok 어노테이션
public class TrackerService {

    private final VideoProgressRepository repository;

    @Transactional
    public void saveOrUpdate(VideoProgressDto dto) {
        // 1. 기존 기록 조회
        Optional<VideoProgress> existingProgressOpt = repository.findById(dto.getVideoId());

        VideoProgress progress;

        if (existingProgressOpt.isPresent()) {
            // 2. 기록이 있으면 업데이트
            progress = existingProgressOpt.get();

            // 3. 누적 공부 시간 계산 (핵심 로직)
            // 마지막 동기화 시간과 현재 시간 차이를 초로 계산 (최대 10초)
            long timeElapsed = ChronoUnit.SECONDS.between(progress.getLastSyncedAt(), LocalDateTime.now());

            // 15초 이내의 간격만 유효한 학습 시간으로 인정 (content.js의 5초 간격 전송 고려)
            if (timeElapsed > 0 && timeElapsed <= 15) {
                progress.setStudyTimeSeconds(progress.getStudyTimeSeconds() + timeElapsed);
            }

        } else {
            // 2. 기록이 없으면 신규 생성
            progress = new VideoProgress();
            progress.setVideoId(dto.getVideoId());
            progress.setCreatedAt(LocalDateTime.now());
        }

        // 공통 업데이트 (제목, 채널명, 재생 위치)
        progress.setTitle(dto.getTitle());
        progress.setChannel(dto.getChannel());
        progress.setDurationSeconds(dto.getDuration());
        progress.setLastProgressSeconds(dto.getCurrentTime());

        // save를 호출하여 저장 (JPA가 ID를 보고 insert/update를 결정)
        repository.save(progress);
    }

    // (기존 코드 아래에 추가)
    // 현재 시청 중인 (가장 최근에 동기화된) 영상을 찾아서 DTO로 변환
    @Transactional(readOnly = true)
    public ContinueWatchingDto getContinueWatchingData() {
        // 1. 가장 최근에 동기화된 영상을 1개 찾습니다.
        // 이 기능을 위해 Repository에 새로운 메서드가 필요합니다. (아래 3번 참고)
        Optional<VideoProgress> latestVideoOpt = repository.findTopByOrderByLastSyncedAtDesc();

        if (latestVideoOpt.isEmpty()) {
            // 데이터가 없는 경우 null 반환 또는 기본 DTO 반환
            return null;
        }

        VideoProgress video = latestVideoOpt.get();

        // 2. DTO로 변환 및 포맷팅
        int percentage = (int) Math.min(100, (video.getLastProgressSeconds() / video.getDurationSeconds()) * 100);

        // 시간 포맷팅 헬퍼 메서드 (간단히 구현)
        String progressTime = formatSeconds(video.getLastProgressSeconds());
        String durationTime = formatSeconds(video.getDurationSeconds());

        // 유튜브 이어보기 링크 생성 (핵심 기능)
        String continueUrl = String.format("https://www.youtube.com/watch?v=%s&t=%ds",
                video.getVideoId(),
                (int) video.getLastProgressSeconds());

        return ContinueWatchingDto.builder()
                .videoId(video.getVideoId())
                .title(video.getTitle())
                .channel(video.getChannel())
                .thumbnailUrl(String.format("https://img.youtube.com/vi/%s/maxresdefault.jpg", video.getVideoId()))
                .percentage(percentage)
                .lastProgressTimeFormatted(progressTime)
                .totalDurationFormatted(durationTime)
                .continueWatchUrl(continueUrl)
                .build();
    }

    // 초를 시:분:초 형식으로 변환하는 간단한 헬퍼 메서드
    private String formatSeconds(double seconds) {
        long totalSeconds = (long) seconds;
        long minutes = totalSeconds % 3600 / 60;
        long secs = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    /**
     * 영상 제목을 분석하여 딴짓 여부를 판단하는 시뮬레이션 로직
     * 
     * @param title 영상 제목
     * @return 딴짓 알림 메시지 (딴짓이 아니면 null)
     */
    private String analyzeDistraction(String title) {
        String lowerTitle = title.toLowerCase();

        if (lowerTitle.contains("vlog") || lowerTitle.contains("브이로그")) {
            return "Vlog는 잠시 후에! 지금은 공부할 시간입니다. 집중하세요! 👀";
        }
        if (lowerTitle.contains("게임") || lowerTitle.contains("game play")) {
            return "게임을 유혹을 참아내고 다시 강의로 돌아오세요. 🕹️";
        }
        if (lowerTitle.contains("asmr") || lowerTitle.contains("먹방")) {
            return "휴식 시간에는 좋습니다. 하지만 지금은 강의를 시청 중인 것 같아요! 🎧";
        }

        return null; // 딴짓 키워드가 없으면 null 반환
    }

    /**
     * 데이터를 저장하고 익스텐션에 보낼 응답을 생성합니다.
     * 
     * @param dto 익스텐션으로부터 받은 데이터
     * @return 익스텐션에게 보낼 SyncResponseDto
     */
    @Transactional
    public SyncResponseDto saveAndGenerateResponse(VideoProgressDto dto) {
        // 1. 기존 saveOrUpdate 로직을 수행합니다. (DB 저장)
        saveOrUpdate(dto);

        // 2. 딴짓 분석
        String distractionMessage = analyzeDistraction(dto.getTitle());

        // 3. 응답 DTO 생성
        if (distractionMessage != null) {
            return SyncResponseDto.builder()
                    .requiresNotification(true)
                    .message(distractionMessage)
                    .build();
        } else {
            return SyncResponseDto.builder()
                    .requiresNotification(false)
                    .message("Sync successful.")
                    .build();
        }
    }

    // ********************************************
    // 코스 목록 조회 API 로직 (새로 추가)
    // ********************************************
    @Transactional(readOnly = true)
    public List<CourseItemDto> getAllCourseItems() {
        // 1. 모든 시청 기록을 마지막 동기화 시간 내림차순으로 가져옵니다.
        List<VideoProgress> allVideos = repository.findAllByOrderByLastSyncedAtDesc();

        // 2. Entity 리스트를 DTO 리스트로 변환
        return allVideos.stream()
                .map(this::convertToCourseItemDto)
                .collect(Collectors.toList());
    }

    // Entity to DTO 변환 헬퍼 메서드
    private CourseItemDto convertToCourseItemDto(VideoProgress video) {
        int percentage = (int) Math.min(100, (video.getLastProgressSeconds() / video.getDurationSeconds()) * 100);

        // 유튜브 이어보기 링크 생성
        String continueUrl = String.format("https://www.youtube.com/watch?v=%s&t=%ds",
                video.getVideoId(),
                (int) video.getLastProgressSeconds());

        return CourseItemDto.builder()
                .videoId(video.getVideoId())
                .title(video.getTitle())
                .channel(video.getChannel())
                .percentage(percentage)
                .lastProgressTimeAgo(formatTimeAgo(video.getLastSyncedAt())) // 시간 포맷팅
                .continueWatchUrl(continueUrl)
                .build();
    }

    // 시간 포맷팅 헬퍼 메서드 (방금 전, 5분 전, 2일 전 등으로 표시)
    private String formatTimeAgo(LocalDateTime pastTime) {
        LocalDateTime now = LocalDateTime.now();
        long seconds = ChronoUnit.SECONDS.between(pastTime, now);

        if (seconds < 60)
            return "방금 전";

        long minutes = seconds / 60;
        if (minutes < 60)
            return minutes + "분 전";

        long hours = minutes / 60;
        if (hours < 24)
            return hours + "시간 전";

        long days = hours / 24;
        if (days < 7)
            return days + "일 전";

        // 7일 이상은 간단히 날짜만 표시 (예: 2024-11-20)
        return pastTime.toLocalDate().toString();
    }

    // ********************************************
    // 통계 계산 API 로직
    // ********************************************

    @Transactional(readOnly = true)
    public DashboardStatsDto getDashboardStats() {
        List<VideoProgress> allVideos = repository.findAll();

        // 1. 총 학습 시간 계산 및 과목별 누적 시간 계산

        // 루프 전에 final 변수를 선언할 필요가 없습니다.
        double totalStudySeconds = 0; // double totalStudySeconds = 0; 은 그대로 둡니다.
        Map<String, Double> subjectAccumulatedSeconds = new HashMap<>();

        for (VideoProgress video : allVideos) {
            // ... (이 부분의 기존 로직 유지)
            double studyTimeForVideo = video.getLastProgressSeconds();
            totalStudySeconds += studyTimeForVideo; // 이 루프 안에서 totalStudySeconds가 변경됩니다.

            // ... (과목 분류 로직 유지)
            String subject = classifySubject(video.getTitle());
            subjectAccumulatedSeconds.merge(subject, studyTimeForVideo, Double::sum);
        }

        final double finalTotalStudySeconds = totalStudySeconds; // 에러 방지를 위해 이 줄을 추가합니다.

        // 2. 과목 분포 퍼센트 계산
        List<DashboardStatsDto.SubjectStatDto> subjectStats = subjectAccumulatedSeconds.entrySet().stream()
                .map(entry -> {
                    String subjectName = entry.getKey();
                    double seconds = entry.getValue();
                    // 수정: totalStudySeconds 대신 finalTotalStudySeconds를 사용합니다.
                    double percentage = (seconds / finalTotalStudySeconds) * 100;
                    String color = getSubjectColor(subjectName);

                    return DashboardStatsDto.SubjectStatDto.builder()
                            .subjectName(subjectName)
                            .percentage(Math.round(percentage * 100.0) / 100.0)
                            .color(color)
                            .build();
                })
                .sorted(Comparator.comparing(DashboardStatsDto.SubjectStatDto::getPercentage).reversed())
                .collect(Collectors.toList());

        // 3. 포맷팅 및 DTO 완성
        String formattedTime = formatTotalSeconds(totalStudySeconds);

        return DashboardStatsDto.builder()
                .totalStudySeconds(totalStudySeconds)
                .totalStudyTimeFormatted(formattedTime)
                .totalStudyHours(totalStudySeconds / 3600.0)
                .subjectStats(subjectStats)
                .weekGoalPercentage(Math.min(100, (totalStudySeconds / (3600 * 20)) * 100))
                .build();
    }

    // 영상 제목을 기준으로 과목 분류 (시뮬레이션)
    private String classifySubject(String title) {
        String lowerTitle = title.toLowerCase();

        if (lowerTitle.contains("spring") || lowerTitle.contains("java") || lowerTitle.contains("jpa")
                || lowerTitle.contains("서버")) {
            return "Java / Backend";
        }
        if (lowerTitle.contains("react") || lowerTitle.contains("js") || lowerTitle.contains("css")
                || lowerTitle.contains("프론트")) {
            return "Frontend";
        }
        if (lowerTitle.contains("알고리즘") || lowerTitle.contains("cs") || lowerTitle.contains("자료구조")
                || lowerTitle.contains("네트워크")) {
            return "CS 지식";
        }
        return "기타";
    }

    // 과목별 색상 지정 (프런트엔드 Tailwind CSS 색상 코드)
    private String getSubjectColor(String subject) {
        switch (subject) {
            case "Java / Backend":
                return "red-500";
            case "Frontend":
                return "blue-500";
            case "CS 지식":
                return "green-500";
            default:
                return "gray-500";
        }
    }

    // 총 초를 시:분 형식으로 변환하는 헬퍼 함수
    private String formatTotalSeconds(double seconds) {
        long totalSeconds = (long) seconds;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;

        if (hours > 0) {
            return String.format("%d시간 %02d분", hours, minutes);
        } else {
            return String.format("%d분", minutes);
        }
    }
}