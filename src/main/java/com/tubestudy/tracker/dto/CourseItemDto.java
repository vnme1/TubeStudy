package com.tubestudy.tracker.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseItemDto {
    private String videoId;
    private String title;
    private String channel;
    private int percentage; // 진도율 (0~100)
    private String lastProgressTimeAgo; // 예: "5분 전", "2일 전"
    private String continueWatchUrl; // 이어보기 링크
}