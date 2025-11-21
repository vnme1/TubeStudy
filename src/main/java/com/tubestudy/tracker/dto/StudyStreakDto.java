package com.tubestudy.tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyStreakDto {
    private int currentStreak;
    private int longestStreak;
    private LocalDate lastStudyDate;
    private LocalDate streakStartDate;
    private LocalDate longestStreakDate;
    private boolean streakBroken;

    // 알림 관련 필드
    private String notificationMessage;
    private String notificationType; // "milestone", "achievement", "encouragement"
    private boolean shouldNotify;
}
