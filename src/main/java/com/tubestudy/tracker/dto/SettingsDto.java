package com.tubestudy.tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettingsDto {
    private int weeklyGoalHours;
    private boolean distractionAlertEnabled;
    private boolean achievementAlertEnabled;
    private boolean darkModeEnabled;
    private boolean voiceNotificationEnabled;
    private boolean animationEnabled;
}