package com.tubestudy.tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 딴짓 키워드 DTO
 * API 요청/응답에 사용됩니다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistractionKeywordDto {
    private Long id;
    private String keyword;
    private String category;
    private Boolean isActive;
    private String alertMessage;
    private Boolean isCustom;
    private LocalDateTime createdAt;
}
