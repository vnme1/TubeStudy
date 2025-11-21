package com.tubestudy.tracker.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SettingsDto {
    private int weeklyGoalHours;

    @Builder
    public SettingsDto(int weeklyGoalHours) {
        this.weeklyGoalHours = weeklyGoalHours;
    }
}