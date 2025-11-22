package com.tubestudy.tracker.service;

import com.tubestudy.tracker.dto.SettingsDto;
import com.tubestudy.tracker.entity.Settings;
import com.tubestudy.tracker.repository.SettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettingsService {
    private final SettingsRepository settingsRepository;

    private static final int DEFAULT_GOAL_HOURS = 20; // 기본 목표 시간

    /**
     * 현재 설정 값을 조회합니다. 없으면 기본값으로 생성 후 반환합니다.
     */
    @Transactional(readOnly = true)
    public SettingsDto getSettings() {
        // ID 1번으로 조회하여 설정 레코드가 있는지 확인합니다.
        Settings settings = settingsRepository.findById(1L).orElseGet(() -> {
            // 없으면 기본값으로 새로운 Settings 엔티티를 생성하고 저장합니다.
            Settings newSettings = Settings.builder()
                    .weeklyGoalHours(DEFAULT_GOAL_HOURS)
                    .distractionAlertEnabled(true)
                    .achievementAlertEnabled(true)
                    .darkModeEnabled(true)
                    .voiceNotificationEnabled(false)
                    .animationEnabled(true)
                    .build();
            return settingsRepository.save(newSettings);
        });

        return SettingsDto.builder()
                .weeklyGoalHours(settings.getWeeklyGoalHours())
                .distractionAlertEnabled(settings.isDistractionAlertEnabled())
                .achievementAlertEnabled(settings.isAchievementAlertEnabled())
                .darkModeEnabled(settings.isDarkModeEnabled())
                .voiceNotificationEnabled(settings.isVoiceNotificationEnabled())
                .animationEnabled(settings.isAnimationEnabled())
                .build();
    }

    /**
     * 주간 목표 시간을 업데이트합니다.
     */
    @Transactional
    public SettingsDto updateGoal(SettingsDto dto) {
        // ID 1번으로 조회하거나, 없으면 기본값으로 생성합니다.
        Settings settings = settingsRepository.findById(1L).orElseGet(() -> {
            return Settings.builder()
                    .weeklyGoalHours(DEFAULT_GOAL_HOURS)
                    .distractionAlertEnabled(true)
                    .achievementAlertEnabled(true)
                    .darkModeEnabled(true)
                    .voiceNotificationEnabled(false)
                    .animationEnabled(true)
                    .build();
        });

        // 목표 시간 업데이트
        settings.updateGoal(dto.getWeeklyGoalHours());

        // 알림 설정 업데이트
        settings.setDistractionAlertEnabled(dto.isDistractionAlertEnabled());
        settings.setAchievementAlertEnabled(dto.isAchievementAlertEnabled());

        // UI/UX 개선 설정 업데이트
        settings.setDarkModeEnabled(dto.isDarkModeEnabled());
        settings.setVoiceNotificationEnabled(dto.isVoiceNotificationEnabled());
        settings.setAnimationEnabled(dto.isAnimationEnabled());

        Settings updatedSettings = settingsRepository.save(settings);

        return SettingsDto.builder()
                .weeklyGoalHours(updatedSettings.getWeeklyGoalHours())
                .distractionAlertEnabled(updatedSettings.isDistractionAlertEnabled())
                .achievementAlertEnabled(updatedSettings.isAchievementAlertEnabled())
                .darkModeEnabled(updatedSettings.isDarkModeEnabled())
                .voiceNotificationEnabled(updatedSettings.isVoiceNotificationEnabled())
                .animationEnabled(updatedSettings.isAnimationEnabled())
                .build();
    }
}