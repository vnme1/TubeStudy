package com.tubestudy.tracker.controller;

import com.tubestudy.tracker.dto.SettingsDto;
import com.tubestudy.tracker.dto.DistractionKeywordDto;
import com.tubestudy.tracker.service.SettingsService;
import com.tubestudy.tracker.service.DistractionKeywordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/settings")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;
    private final DistractionKeywordService distractionKeywordService;

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

    // ========================================================
    // 딴짓 키워드 관리 엔드포인트
    // ========================================================

    /**
     * 모든 활성 키워드 조회
     */
    @GetMapping("/keywords")
    public List<DistractionKeywordDto> getAllActiveKeywords() {
        return distractionKeywordService.getAllActiveKeywords();
    }

    /**
     * 모든 키워드 조회 (활성/비활성 포함)
     */
    @GetMapping("/keywords/all")
    public List<DistractionKeywordDto> getAllKeywords() {
        return distractionKeywordService.getAllKeywords();
    }

    /**
     * 새로운 키워드 추가
     */
    @PostMapping("/keywords")
    public DistractionKeywordDto addKeyword(@RequestBody DistractionKeywordDto dto) {
        return distractionKeywordService.addKeyword(dto);
    }

    /**
     * 기존 키워드 수정
     */
    @PutMapping("/keywords/{id}")
    public DistractionKeywordDto updateKeyword(@PathVariable Long id, @RequestBody DistractionKeywordDto dto) {
        return distractionKeywordService.updateKeyword(id, dto);
    }

    /**
     * 키워드 활성화/비활성화 토글
     */
    @PutMapping("/keywords/{id}/toggle")
    public DistractionKeywordDto toggleKeywordActive(@PathVariable Long id) {
        return distractionKeywordService.toggleKeywordActive(id);
    }

    /**
     * 키워드 삭제 (커스텀 키워드만)
     */
    @DeleteMapping("/keywords/{id}")
    public void deleteKeyword(@PathVariable Long id) {
        distractionKeywordService.deleteKeyword(id);
    }
}