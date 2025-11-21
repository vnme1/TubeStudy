package com.tubestudy.tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsDto {
    // 시간별 통계
    private long totalStudyTimeSeconds;
    private long weeklyStudyTimeSeconds;
    private long monthlyStudyTimeSeconds;

    // 일별 통계
    private DaylyAnalytics[] dailyStats; // 최근 7일

    // 패턴 분석
    private String mostProductiveDay; // 가장 많이 학습한 요일
    private int mostProductiveHour; // 가장 많이 학습한 시간 (0-23)
    private long totalWatchedVideos;
    private double averageSessionDuration; // 평균 세션 시간 (초)

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DaylyAnalytics {
        private String day; // "2025-11-21"
        private String dayOfWeek; // "금요일"
        private long studyTimeSeconds;
        private int videoCount;
        private boolean hasStudied; // 그 날 학습했는지 여부
    }
}
