package com.tubestudy.tracker.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SyncResponseDto {
    private boolean requiresNotification; // 알림이 필요한가? (true/false)
    private String message; // 알림 메시지 내용
}