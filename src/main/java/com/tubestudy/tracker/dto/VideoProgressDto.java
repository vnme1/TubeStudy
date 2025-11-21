package com.tubestudy.tracker.dto;

import com.tubestudy.tracker.entity.VideoProgress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoProgressDto {

    private String videoId;
    private String title;
    private String channel;
    private double totalDurationSeconds; // 엔티티 필드명과 통일
    private double lastProgressSeconds;

    // ✅ 추가: 이번 동기화 간격 동안 실제로 시청한 시간 (클라이언트가 계산하여 전송)
    private double accumulatedStudySeconds;

    /**
     * DTO를 엔티티로 변환합니다. (새로운 기록 생성 시 사용)
     */
    public VideoProgress toEntity() {
        return VideoProgress.builder()
                .videoId(videoId)
                .title(title)
                .channel(channel)
                .totalDurationSeconds(totalDurationSeconds)
                .lastProgressSeconds(lastProgressSeconds)
                // ✅ 초기 생성 시 accumulatedStudySeconds 값을 studyTimeSeconds에 설정
                .studyTimeSeconds(accumulatedStudySeconds)
                .build();
    }
}