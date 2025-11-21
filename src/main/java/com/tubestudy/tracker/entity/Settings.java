package com.tubestudy.tracker.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Settings {

    // 단일 설정을 위해 ID를 고정합니다. (예: 1)
    @Id
    private Long id = 1L;

    // 주간 목표 시간 (시간 단위, 기본값 20시간)
    private int weeklyGoalHours = 20;

    // 딴짓 방지 알림 활성화 여부 (기본값: true)
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private boolean distractionAlertEnabled = true;

    // 목표 달성 알림 활성화 여부 (기본값: true)
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private boolean achievementAlertEnabled = true;

    @Builder
    public Settings(int weeklyGoalHours, boolean distractionAlertEnabled, boolean achievementAlertEnabled) {
        // ID는 항상 1로 고정하여 DB에 하나의 설정 레코드만 유지되도록 합니다.
        this.id = 1L;
        this.weeklyGoalHours = weeklyGoalHours;
        this.distractionAlertEnabled = distractionAlertEnabled;
        this.achievementAlertEnabled = achievementAlertEnabled;
    }

    // 목표 시간을 변경하는 메서드
    public void updateGoal(int weeklyGoalHours) {
        this.weeklyGoalHours = weeklyGoalHours;
    }
}