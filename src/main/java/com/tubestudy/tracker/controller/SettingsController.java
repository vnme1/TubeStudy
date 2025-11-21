package com.tubestudy.tracker.controller;

import com.tubestudy.tracker.dto.SettingsDto;
import com.tubestudy.tracker.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    // 현재 설정 조회 (GET)
    @GetMapping
    public SettingsDto getSettings() {
        return settingsService.getSettings();
    }

    // 설정 업데이트 (POST/PUT)
    @PostMapping
    public SettingsDto updateGoal(@RequestBody SettingsDto dto) {
        return settingsService.updateGoal(dto);
    }
}