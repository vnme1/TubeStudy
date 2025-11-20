package com.tubestudy.tracker.controller;

import com.tubestudy.tracker.dto.ContinueWatchingDto;
import com.tubestudy.tracker.dto.VideoProgressDto;
import com.tubestudy.tracker.dto.DashboardStatsDto;
import com.tubestudy.tracker.service.TrackerService;
import lombok.RequiredArgsConstructor;
import com.tubestudy.tracker.dto.SyncResponseDto;
import org.springframework.web.bind.annotation.*;
import com.tubestudy.tracker.dto.CourseItemDto;
import java.util.List;

@RestController
@RequestMapping("/api/tracker")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor // final 필드(service)를 자동으로 주입
public class TrackerController {

    private final TrackerService trackerService; // 서비스 주입

    // ********************************************
    // 코스 목록 조회 API (새로 추가)
    // ********************************************

    @GetMapping("/dashboard/courses")
    public List<CourseItemDto> getAllCourseItems() {
        return trackerService.getAllCourseItems();
    }

    @GetMapping("/dashboard/stats") // 통계 정보 조회 API (새로 추가)
    public DashboardStatsDto getDashboardStats() {
        return trackerService.getDashboardStats();
    }

    @PostMapping("/sync")
    public SyncResponseDto syncProgress(@RequestBody VideoProgressDto dto) {
        // 기존 DB 저장 로직 대신, 새로운 저장 및 응답 생성 로직 호출
        SyncResponseDto response = trackerService.saveAndGenerateResponse(dto);

        // 어떤 응답이 익스텐션으로 가는지 콘솔로 확인
        System.out.println("[SYNC RESPONSE] 알림 필요 여부: " + response.isRequiresNotification());

        return response; // 익스텐션으로 응답 전달
    }

    @GetMapping("/dashboard/continue")
    public ContinueWatchingDto getContinueWatchingData() {
        // 서비스에서 데이터를 조회하여 바로 JSON 형태로 반환합니다.
        return trackerService.getContinueWatchingData();
    }
}