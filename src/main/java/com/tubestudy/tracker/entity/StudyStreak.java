package com.tubestudy.tracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * 사용자의 연속 학습일 수(Streak)를 추적하는 엔티티
 */
@Entity
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StudyStreak {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 현재 진행 중인 연속 학습일 수
    private int currentStreak;

    // 최장 연속 학습일 수 (기록)
    private int longestStreak;

    // 마지막으로 학습한 날짜
    private LocalDate lastStudyDate;

    // 스트릭이 시작된 날짜
    private LocalDate streakStartDate;

    // 최장 스트릭이 기록된 날짜
    private LocalDate longestStreakDate;

    // 공휴일, 휴식일 등으로 인한 스트릭 초기화 여부
    private boolean streakBroken;

    /**
     * 현재 스트릭을 업데이트합니다.
     * 오늘 학습이 감지되었을 때 호출됩니다.
     */
    public void updateStreak() {
        LocalDate today = LocalDate.now();

        if (lastStudyDate == null) {
            // 첫 학습 기록
            this.currentStreak = 1;
            this.lastStudyDate = today;
            this.streakStartDate = today;
            this.longestStreak = 1;
            this.longestStreakDate = today;
            this.streakBroken = false;
        } else if (lastStudyDate.equals(today)) {
            // 오늘 이미 학습함 (변화 없음)
            // Do nothing
        } else if (lastStudyDate.plusDays(1).equals(today)) {
            // 연속된 날짜에 학습함 (스트릭 증가)
            this.currentStreak++;
            this.lastStudyDate = today;
            this.streakBroken = false;

            // 최장 스트릭 업데이트
            if (this.currentStreak > this.longestStreak) {
                this.longestStreak = this.currentStreak;
                this.longestStreakDate = today;
            }
        } else {
            // 중단된 스트릭 (1일 이상 빈 날이 있음)
            this.currentStreak = 1;
            this.lastStudyDate = today;
            this.streakStartDate = today;
            this.streakBroken = true;
        }
    }

    /**
     * 스트릭을 초기화합니다.
     */
    public void resetStreak() {
        this.currentStreak = 0;
        this.lastStudyDate = null;
        this.streakBroken = true;
    }
}
