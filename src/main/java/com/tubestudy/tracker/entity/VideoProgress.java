package com.tubestudy.tracker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VideoProgress {

    @Id
    private String videoId; // 유튜브 영상 ID를 기본 키(Primary Key)로 사용

    private String title;
    private String channel;

    private double lastProgressSeconds; // 마지막 재생 위치 (초)
    private double durationSeconds; // 총 영상 길이 (초)
    private double studyTimeSeconds; // 누적된 총 공부 시간 (나중에 사용할 누적 시간)

    private LocalDateTime lastSyncedAt; // 마지막 동기화 시간
    private LocalDateTime createdAt; // 최초 생성 시간

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.lastSyncedAt = LocalDateTime.now();
        // 초기 누적 공부 시간은 0
        if (this.studyTimeSeconds == 0) {
            this.studyTimeSeconds = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.lastSyncedAt = LocalDateTime.now();
    }
}