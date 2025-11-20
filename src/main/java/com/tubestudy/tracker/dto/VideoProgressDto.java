package com.tubestudy.tracker.dto;

import lombok.Data;

@Data
public class VideoProgressDto {
    private String videoId; // 유튜브 영상 ID
    private String title; // 영상 제목
    private String channel; // 채널명
    private double currentTime; // 현재 재생 시점 (초)
    private double duration; // 전체 영상 길이 (초)
}