package com.tubestudy.tracker.service;

import com.tubestudy.tracker.dto.ContinueWatchingDto;
import com.tubestudy.tracker.dto.SyncResponseDto;
import com.tubestudy.tracker.dto.VideoProgressDto;
import com.tubestudy.tracker.dto.CourseItemDto;
import com.tubestudy.tracker.dto.DashboardStatsDto;
import com.tubestudy.tracker.dto.StudyStreakDto;
import com.tubestudy.tracker.dto.AnalyticsDto;
import com.tubestudy.tracker.entity.VideoProgress;
import com.tubestudy.tracker.entity.StudyStreak;
import com.tubestudy.tracker.repository.VideoProgressRepository;
import com.tubestudy.tracker.repository.StudyStreakRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Comparator;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.temporal.ChronoUnit;
// import java.util.*;
import java.util.stream.Collectors;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class TrackerService {

    private final VideoProgressRepository repository;
    private final StudyStreakRepository studyStreakRepository;
    private final SettingsService settingsService;

    // ========================================================
    // [Core Logic] 1. 동기화 및 기록 저장/응답
    // ========================================================

    /**
     * 데이터를 저장하고 익스텐션에 보낼 응답을 생성합니다.
     * 이 메서드에서 DB 저장/업데이트와 딴짓 분석 로직을 모두 처리합니다.
     * * @param dto 익스텐션으로부터 받은 데이터 (accumulatedStudySeconds 포함)
     * 
     * @return 익스텐션에게 보낼 SyncResponseDto
     */
    @Transactional
    public SyncResponseDto saveAndGenerateResponse(VideoProgressDto dto) {

        // 1. 기존 기록 찾기
        Optional<VideoProgress> existingProgressOpt = repository.findByVideoId(dto.getVideoId());

        // 2. 딴짓 분석
        String distractionMessage = analyzeDistraction(dto.getTitle());

        // 3. 진도 및 완료 상태 계산
        double ratio = dto.getLastProgressSeconds() / dto.getTotalDurationSeconds();
        int currentPercentage = (int) (ratio * 100);

        VideoProgress progress;

        if (existingProgressOpt.isPresent()) {
            progress = existingProgressOpt.get();

            // 3-1. 기록이 있으면 업데이트

            // ✅ A. 실제 시청 시간 누적 및 최종 진도 업데이트
            progress.update(dto.getAccumulatedStudySeconds(), dto.getLastProgressSeconds()); // 💡 update 메서드 시그니처 조정 필요

        } else {
            // 3-2. 기록이 없으면 신규 생성

            // ✅ B. 신규 생성 시 초기 최고 진도 및 완료 상태 설정
            int initialHighestPercentage = (currentPercentage >= 98) ? 100 : currentPercentage;
            boolean initialIsCompleted = (currentPercentage >= 98);

            progress = VideoProgress.builder()
                    .videoId(dto.getVideoId())
                    .title(dto.getTitle())
                    .channel(dto.getChannel())
                    .totalDurationSeconds(dto.getTotalDurationSeconds())
                    .lastProgressSeconds(dto.getLastProgressSeconds())
                    .studyTimeSeconds(dto.getAccumulatedStudySeconds())
                    .highestProgressPercentage(initialHighestPercentage) // ✅ 최고 진도 초기값
                    .isCompleted(initialIsCompleted) // ✅ 완료 상태 초기값
                    .build();

            repository.save(progress);
        }

        // ************ ✅ 4. 최고 진도 및 완료 상태 공통 업데이트 로직 (새로 추가) ************

        // C. 최고 진도 업데이트: 현재 진도가 저장된 최고 진도보다 높으면 업데이트
        if (currentPercentage > progress.getHighestProgressPercentage()) {
            progress.setHighestProgressPercentage(currentPercentage);
        }

        // D. 완료 상태 업데이트: 98% 이상 도달하면 isCompleted를 true로 설정 (한번 true가 되면 유지됨)
        if (currentPercentage >= 98) {
            progress.setCompleted(true);
        }

        // (JPA의 변경 감지(Dirty Checking) 덕분에 기존 기록 업데이트 시에는 별도 save 호출 불필요)

        // ✅ 5. 스트릭 업데이트 (오늘 학습이 감지되었을 때)
        updateStudyStreak();

        // ************ ✅ 6. 응답 DTO 생성 (기존 로직 유지) ************
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

    /**
     * 영상 제목을 분석하여 딴짓 여부를 판단하는 로직
     * 영문 및 한글 키워드 모두 지원
     * 
     * @param title 영상 제목
     * @return 딴짓 알림 메시지 (딴짓이 아니면 null)
     */
    private String analyzeDistraction(String title) {
        String lowerTitle = title.toLowerCase();

        // Vlog/브이로그/먹방 관련
        if (lowerTitle.contains("vlog") || lowerTitle.contains("브이로그") ||
                lowerTitle.contains("먹방") || lowerTitle.contains("브이로그 혹은 음식")) {
            return "Vlog는 잠시 후에! 지금은 공부할 시간입니다. 집중하세요! 👀";
        }

        // 게임/게임플레이 관련
        if (lowerTitle.contains("게임") || lowerTitle.contains("game play") ||
                lowerTitle.contains("게임플레이") || lowerTitle.contains("gameplay")) {
            return "게임을 유혹을 참아내고 다시 강의로 돌아오세요. 🕹️";
        }

        // ASMR/예능 관련
        if (lowerTitle.contains("asmr") || lowerTitle.contains("예능") ||
                lowerTitle.contains("예술") || lowerTitle.contains("엔터테인먼트")) {
            return "휴식 시간에는 좋습니다. 하지만 지금은 강의를 시청 중인 것 같아요! 🎧";
        }

        return null; // 딴짓 키워드가 없으면 null 반환
    }

    // ========================================================
    // [Dashboard] 2. 통계 데이터 조회 (Stats)
    // ========================================================

    @Transactional(readOnly = true)
    public DashboardStatsDto getDashboardStats(String periodType) {

        // 1. 조회 기간 결정 및 기록 조회
        LocalDateTime[] range = calculateTimeRange(periodType);
        LocalDateTime startDate = range[0];
        // LocalDateTime endDate = range[1]; // 사용하지 않음

        List<VideoProgress> allVideos;
        if (startDate != null) {
            // 기간이 설정되면 LastSyncedAt을 기준으로 조회
            allVideos = repository.findByLastSyncedAtBetween(startDate, range[1]);
        } else {
            // "all" 또는 잘못된 값이 들어오면 기존대로 전체 조회
            allVideos = repository.findAll();
        }

        // ✅ 2. 목표 시간 동적 조회
        int weeklyGoalHours = settingsService.getSettings().getWeeklyGoalHours();

        // 3. 총 학습 시간 계산 및 과목별 누적 시간 계산
        double totalStudySeconds = 0;
        Map<String, Double> subjectAccumulatedSeconds = new HashMap<>();

        for (VideoProgress video : allVideos) {
            // ⚠️ 변경됨: LastProgressSeconds 대신 studyTimeSeconds를 통계 기준으로 사용
            double studyTimeForVideo = video.getStudyTimeSeconds();
            totalStudySeconds += studyTimeForVideo;

            // 과목 분류
            String subject = classifySubject(video.getTitle());
            subjectAccumulatedSeconds.merge(subject, studyTimeForVideo, Double::sum);
        }

        final double finalTotalStudySeconds = totalStudySeconds;

        // 4. 과목 분포 퍼센트 계산
        List<DashboardStatsDto.SubjectStatDto> subjectStats = subjectAccumulatedSeconds.entrySet().stream()
                .map(entry -> {
                    String subjectName = entry.getKey();
                    double seconds = entry.getValue();
                    double percentage = (finalTotalStudySeconds > 0) ? (seconds / finalTotalStudySeconds) * 100 : 0;
                    String color = getSubjectColor(subjectName);

                    return DashboardStatsDto.SubjectStatDto.builder()
                            .subjectName(subjectName)
                            .percentage(Math.round(percentage * 100.0) / 100.0)
                            .color(color)
                            .build();
                })
                .sorted(Comparator.comparing(DashboardStatsDto.SubjectStatDto::getPercentage).reversed())
                .collect(Collectors.toList());

        // 5. 포맷팅 및 DTO 완성
        String formattedTime = formatTotalSeconds(totalStudySeconds);

        // 주간 목표 시간(Hours)을 초(Seconds)로 변환
        double totalGoalSeconds = (double) weeklyGoalHours * 3600;

        return DashboardStatsDto.builder()
                .totalStudySeconds(totalStudySeconds)
                .totalStudyTimeFormatted(formattedTime)
                .totalStudyHours(totalStudySeconds / 3600.0)
                .subjectStats(subjectStats)
                // 동적으로 조회된 목표 시간을 사용하여 퍼센트 계산
                .weekGoalPercentage(Math.min(100, (totalStudySeconds / totalGoalSeconds) * 100))
                .build();
    }

    // ... (calculateTimeRange, classifySubject, getSubjectColor, formatTotalSeconds
    // 등 기존 헬퍼 메서드는 유지)

    /**
     * 기간 유형에 따른 시작 시간과 종료 시간을 계산합니다.
     * 
     * @param periodType "today", "week", "month", "all"
     * @return [startDate, endDate] 배열 (startDate가 null이면 전체)
     */
    private LocalDateTime[] calculateTimeRange(String periodType) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = null;
        LocalDateTime endDate = now;

        switch (periodType.toLowerCase()) {
            case "today":
                startDate = now.truncatedTo(ChronoUnit.DAYS); // 오늘 00:00:00
                break;
            case "week":
                // 이번 주 월요일 00:00:00
                // DayOfWeek.MONDAY에서 현재 요일까지 계산하여 정확한 월요일 도출
                java.time.LocalDate mondayOfThisWeek = now.toLocalDate()
                        .with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
                startDate = mondayOfThisWeek.atStartOfDay();
                break;
            case "month":
                startDate = now.toLocalDate().withDayOfMonth(1).atStartOfDay(); // 이번 달 1일 00:00:00
                break;
            case "all":
            default:
                // startDate = null; (전체 조회, 기본값)
                break;
        }

        return new LocalDateTime[] { startDate, endDate };
    }

    /**
     * 영상 제목을 기준으로 과목 분류 (시뮬레이션)
     */
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

    /**
     * 과목별 색상 지정 (프런트엔드 Tailwind CSS 색상 코드)
     */
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

    /**
     * 총 초를 시:분 형식으로 변환하는 헬퍼 함수
     */
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

    // ========================================================
    // [Dashboard] 3. 이어보기 및 코스 목록 조회/삭제 (Courses)
    // ========================================================

    @Transactional(readOnly = true)
    public ContinueWatchingDto getContinueWatchingData() {
        // 1. 가장 최근에 동기화된 영상을 1개 찾습니다.
        Optional<VideoProgress> latestVideoOpt = repository.findTopByOrderByLastSyncedAtDesc();

        if (latestVideoOpt.isEmpty()) {
            return null;
        }

        VideoProgress video = latestVideoOpt.get();

        // 진도율 계산 (추출된 메서드 사용)
        int percentage = calculateProgressPercentage(video);

        // 시간 포맷팅 헬퍼 메서드
        String progressTime = formatSeconds(video.getLastProgressSeconds());
        String durationTime = formatSeconds(video.getTotalDurationSeconds());

        // 유튜브 이어보기 링크 생성
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

    // 초를 분:초 형식으로 변환하는 간단한 헬퍼 메서드
    private String formatSeconds(double seconds) {
        long totalSeconds = (long) seconds;
        long minutes = totalSeconds % 3600 / 60;
        long secs = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    // 코스 목록 조회 API 로직
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
        // 진도율 계산 (추출된 메서드 사용)
        int percentage = calculateProgressPercentage(video);

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

    // 진도 비율을 퍼센트로 계산하는 헬퍼 메서드 (98% 보정 포함)
    private int calculateProgressPercentage(VideoProgress video) {
        int percentage;
        if (video.isCompleted()) {
            // 완료 상태라면 무조건 100% 표시
            percentage = 100;
        } else {
            // 완료 상태가 아니면 현재 lastProgressSeconds를 반영
            double ratio = video.getLastProgressSeconds() / video.getTotalDurationSeconds();
            // 98% 보정 적용
            percentage = (int) (ratio >= 0.98 ? 100 : Math.min(100, ratio * 100));
        }
        return percentage;
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

    // ========================================================
    // [Gamification] 4. 학습 스트릭 관리
    // ========================================================

    /**
     * 학습 스트릭을 업데이트합니다.
     * 매번 학습이 감지될 때 호출되어 연속 학습일 수를 추적합니다.
     */
    @Transactional
    public void updateStudyStreak() {
        // ID = 1인 스트릭 레코드 조회 또는 생성
        StudyStreak streak = studyStreakRepository.findById(1L).orElseGet(() -> {
            StudyStreak newStreak = StudyStreak.builder()
                    .currentStreak(0)
                    .longestStreak(0)
                    .lastStudyDate(null)
                    .streakStartDate(null)
                    .longestStreakDate(null)
                    .streakBroken(false)
                    .build();
            return studyStreakRepository.save(newStreak);
        });

        // 스트릭 업데이트
        streak.updateStreak();
    }

    /**
     * 현재 학습 스트릭 정보를 반환합니다.
     */
    @Transactional(readOnly = true)
    public StudyStreakDto getStudyStreak() {
        StudyStreak streak = studyStreakRepository.findById(1L).orElseGet(() -> {
            StudyStreak newStreak = StudyStreak.builder()
                    .currentStreak(0)
                    .longestStreak(0)
                    .lastStudyDate(null)
                    .streakStartDate(null)
                    .longestStreakDate(null)
                    .streakBroken(false)
                    .build();
            return studyStreakRepository.save(newStreak);
        });

        // 알림 로직 추가
        String notificationMessage = null;
        String notificationType = null;
        boolean shouldNotify = false;

        // 마일스톤 체크 (7일, 14일, 30일, 100일)
        int currentStreak = streak.getCurrentStreak();
        if (currentStreak == 7) {
            notificationMessage = "🎉 축하합니다! 7일 연속 학습을 달성했어요!";
            notificationType = "milestone";
            shouldNotify = true;
        } else if (currentStreak == 14) {
            notificationMessage = "🔥 놀라워요! 14일 연속 학습 달성!";
            notificationType = "milestone";
            shouldNotify = true;
        } else if (currentStreak == 30) {
            notificationMessage = "⭐ 최고예요! 1개월 연속 학습! 당신은 학습 챔피언입니다!";
            notificationType = "milestone";
            shouldNotify = true;
        } else if (currentStreak == 100) {
            notificationMessage = "👑 전설이 되었어요! 100일 연속 학습 달성! 🏆";
            notificationType = "milestone";
            shouldNotify = true;
        }

        // 스트릭 끝남 감지
        if (streak.isStreakBroken() && currentStreak == 1) {
            notificationMessage = "💪 새로운 시작입니다! 오늘부터 다시 연속 학습을 시작하세요!";
            notificationType = "encouragement";
            shouldNotify = true;
        }

        return StudyStreakDto.builder()
                .currentStreak(streak.getCurrentStreak())
                .longestStreak(streak.getLongestStreak())
                .lastStudyDate(streak.getLastStudyDate())
                .streakStartDate(streak.getStreakStartDate())
                .longestStreakDate(streak.getLongestStreakDate())
                .streakBroken(streak.isStreakBroken())
                .notificationMessage(notificationMessage)
                .notificationType(notificationType)
                .shouldNotify(shouldNotify)
                .build();
    }

    // ========================================================
    // [Advanced Analytics] 고급 통계 분석
    // ========================================================
    @Transactional(readOnly = true)
    public AnalyticsDto getAnalytics() {
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);
        LocalDate monthAgo = today.minusDays(30);

        List<VideoProgress> allRecords = repository.findAll();

        // 시간별 통계
        long totalStudyTimeSeconds = (long) allRecords.stream()
                .mapToDouble(VideoProgress::getStudyTimeSeconds)
                .sum();

        long weeklyStudyTimeSeconds = (long) allRecords.stream()
                .filter(v -> !v.getLastSyncedAt().toLocalDate().isBefore(weekAgo))
                .mapToDouble(VideoProgress::getStudyTimeSeconds)
                .sum();

        long monthlyStudyTimeSeconds = (long) allRecords.stream()
                .filter(v -> !v.getLastSyncedAt().toLocalDate().isBefore(monthAgo))
                .mapToDouble(VideoProgress::getStudyTimeSeconds)
                .sum();

        // 일별 통계 (최근 7일)
        AnalyticsDto.DaylyAnalytics[] dailyStats = new AnalyticsDto.DaylyAnalytics[7];
        for (int i = 0; i < 7; i++) {
            LocalDate dayDate = today.minusDays(6 - i);
            final LocalDate currentDay = dayDate;

            long dayStudyTime = (long) allRecords.stream()
                    .filter(v -> v.getLastSyncedAt().toLocalDate().equals(currentDay))
                    .mapToDouble(VideoProgress::getStudyTimeSeconds)
                    .sum();

            long videoCount = allRecords.stream()
                    .filter(v -> v.getLastSyncedAt().toLocalDate().equals(currentDay))
                    .count();

            String dayOfWeek = currentDay.getDayOfWeek().toString();
            String koreanDay = translateDayOfWeek(dayOfWeek);

            dailyStats[i] = AnalyticsDto.DaylyAnalytics.builder()
                    .day(currentDay.toString())
                    .dayOfWeek(koreanDay)
                    .studyTimeSeconds(dayStudyTime)
                    .videoCount((int) videoCount)
                    .hasStudied(dayStudyTime > 0)
                    .build();
        }

        // 가장 생산적인 요일 찾기
        Map<String, Double> dayStudyMap = new HashMap<>();
        String[] daysOfWeek = { "월요일", "화요일", "수요일", "목요일", "금요일", "토요일", "일요일" };
        for (String day : daysOfWeek) {
            dayStudyMap.put(day, 0.0);
        }

        for (VideoProgress v : allRecords) {
            String dayOfWeek = translateDayOfWeek(v.getLastSyncedAt().toLocalDate().getDayOfWeek().toString());
            dayStudyMap.put(dayOfWeek, dayStudyMap.getOrDefault(dayOfWeek, 0.0) + v.getStudyTimeSeconds());
        }

        String mostProductiveDay = dayStudyMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("정보 없음");

        // 가장 생산적인 시간 찾기
        Map<Integer, Double> hourStudyMap = new HashMap<>();
        for (int h = 0; h < 24; h++) {
            hourStudyMap.put(h, 0.0);
        }

        for (VideoProgress v : allRecords) {
            int hour = v.getLastSyncedAt().getHour();
            hourStudyMap.put(hour, hourStudyMap.getOrDefault(hour, 0.0) + v.getStudyTimeSeconds());
        }

        int mostProductiveHour = hourStudyMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(0);

        // 평균 세션 시간
        double averageSessionDuration = allRecords.isEmpty() ? 0
                : totalStudyTimeSeconds / (double) allRecords.size();

        return AnalyticsDto.builder()
                .totalStudyTimeSeconds(totalStudyTimeSeconds)
                .weeklyStudyTimeSeconds(weeklyStudyTimeSeconds)
                .monthlyStudyTimeSeconds(monthlyStudyTimeSeconds)
                .dailyStats(dailyStats)
                .mostProductiveDay(mostProductiveDay)
                .mostProductiveHour(mostProductiveHour)
                .totalWatchedVideos(allRecords.size())
                .averageSessionDuration(averageSessionDuration)
                .build();
    }

    // 요일 번역 헬퍼 메서드
    private String translateDayOfWeek(String dayOfWeek) {
        return switch (dayOfWeek) {
            case "MONDAY" -> "월요일";
            case "TUESDAY" -> "화요일";
            case "WEDNESDAY" -> "수요일";
            case "THURSDAY" -> "목요일";
            case "FRIDAY" -> "금요일";
            case "SATURDAY" -> "토요일";
            case "SUNDAY" -> "일요일";
            default -> "정보 없음";
        };
    }

    // 특정 videoId에 해당하는 모든 시청 기록을 삭제합니다.
    @Transactional
    public void deleteVideoProgress(String videoId) {
        repository.deleteByVideoId(videoId);
    }
}