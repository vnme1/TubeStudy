package com.tubestudy.tracker.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContinueWatchingDto {
    private String videoId;
    private String title;
    private String channel;
    private String thumbnailUrl;
    private String lastProgressTimeFormatted; // 예: 08:15
    private int percentage; // 진도율 (0~100)
    private String totalDurationFormatted; // 예: 24:15
    private String continueWatchUrl; // 이어보기 링크 (유튜브로 바로 이동)
}