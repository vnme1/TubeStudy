package com.tubestudy.tracker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Builder.Default;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DistractionKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 키워드 (예: "vlog", "게임", "ASMR")
    @Column(nullable = false)
    private String keyword;

    // 키워드 카테고리 (예: "Vlog", "Game", "Entertainment")
    @Column(nullable = false)
    private String category;

    // 활성화 여부 (true: 사용, false: 미사용)
    @Column(nullable = false)
    @Default
    private boolean isActive = true;

    // 알림 메시지
    @Column(nullable = false)
    private String alertMessage;

    // 사용자 커스텀 여부 (false: 기본 키워드, true: 사용자 추가 키워드)
    @Column(nullable = false)
    @Default
    private boolean isCustom = false;

    @Column(name = "created_at", updatable = false)
    private java.time.LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = java.time.LocalDateTime.now();
    }
}
