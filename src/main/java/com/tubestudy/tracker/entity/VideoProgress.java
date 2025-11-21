package com.tubestudy.tracker.entity;

// import com.tubestudy.tracker.dto.VideoProgressDto;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class VideoProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // videoId는 유니크한 식별자로 사용하며, @Id 대신 일반 필드로 설정합니다.
    @Column(unique = true)
    private String videoId;

    private String title;
    private String channel;

    // 총 영상 길이
    @Column(name = "total_duration_seconds")
    private double totalDurationSeconds;

    // 마지막 재생 위치
    @Column(name = "last_progress_seconds")
    private double lastProgressSeconds;

    // ✅ 실제 누적 학습 시간 (추가/수정된 필드)
    @Column(name = "study_time_seconds")
    private double studyTimeSeconds;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.lastSyncedAt = LocalDateTime.now();
        // studyTimeSeconds는 @Builder에 의해 0으로 초기화되므로 별도 설정 로직 불필요
    }

    // -----------------------------------------------------
    // ✅ 비즈니스 로직: 업데이트 메서드
    // -----------------------------------------------------

    // ✅ 1. 달성한 최고 진행률 (0~100)
    private int highestProgressPercentage;

    // ✅ 2. 완료 상태 플래그
    private boolean isCompleted;

    /**
     * 새로운 진도 데이터를 기반으로 엔티티를 업데이트하고 학습 시간을 누적합니다.
     * 
     * @param dto                     업데이트할 DTO
     * @param accumulatedStudySeconds 이번 동기화에서 누적된 '실제' 학습 시간
     */
    // public void update(VideoProgressDto dto, double accumulatedStudySeconds) {
    // // 1. 최종 진도 시간 업데이트
    // this.lastProgressSeconds = dto.getLastProgressSeconds();

    // // 2. 실제 누적 학습 시간 합산
    // this.studyTimeSeconds += accumulatedStudySeconds;

    // // 3. 동기화 시간 업데이트
    // this.lastSyncedAt = LocalDateTime.now();
    // }

    public void update(double accumulatedStudySeconds, double lastProgressSeconds) {
        this.studyTimeSeconds += accumulatedStudySeconds; // 학습 시간 누적
        this.lastProgressSeconds = lastProgressSeconds; // 최종 재생 위치 업데이트
        this.lastSyncedAt = LocalDateTime.now();
    }

}