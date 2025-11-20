package com.tubestudy.tracker.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;
import java.util.List;

@Data
@Builder
public class DashboardStatsDto {
    private double totalStudySeconds; // 총 학습 시간 (초)
    private String totalStudyTimeFormatted; // 총 학습 시간 포맷 (예: 12시간 30분)
    private double totalStudyHours; // 총 학습 시간 (시간 단위)

    // 과목별 분포를 위한 Map (예: { "Java": 60, "Frontend": 30, "CS": 10 })
    private Map<String, Double> subjectDistribution;
    private double weekGoalPercentage; // 주간 목표 달성률 (시뮬레이션)

    // 과목 분포를 프런트엔드가 사용하기 쉽게 리스트로 변환 (추가적인 DTO 구조)
    private List<SubjectStatDto> subjectStats;

    @Data
    @Builder
    public static class SubjectStatDto {
        private String subjectName;
        private double percentage;
        private String color;
    }
}