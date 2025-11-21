package com.tubestudy.tracker.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SyncResponseDto {
    private boolean requiresNotification; // 알림이 필요한가? (true/false)
    private String message; // 알림 메시지 내용

    // 딴짓 방지 알림 필드
    private boolean isDistraction; // 딴짓 콘텐츠인지 여부
    private String distractionMessage; // 딴짓 알림 메시지
    private boolean distractionAlertEnabled; // 서버 설정: 딴짓 알림 활성화 여부
}