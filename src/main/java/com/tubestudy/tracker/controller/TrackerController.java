package com.tubestudy.tracker.controller;

import com.tubestudy.tracker.dto.ContinueWatchingDto;
import com.tubestudy.tracker.dto.VideoProgressDto;
import com.tubestudy.tracker.dto.DashboardStatsDto;
import com.tubestudy.tracker.dto.StudyStreakDto;
import com.tubestudy.tracker.dto.AnalyticsDto;
import com.tubestudy.tracker.service.TrackerService;
import com.tubestudy.tracker.service.CsvExportService;
import lombok.RequiredArgsConstructor;
import com.tubestudy.tracker.dto.SyncResponseDto;
import org.springframework.web.bind.annotation.*;
import com.tubestudy.tracker.dto.CourseItemDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.List;
import java.io.IOException;

@RestController
@RequestMapping("/api/tracker")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class TrackerController {

    private final TrackerService trackerService;
    private final CsvExportService csvExportService;

    // ********************************************
    // 통계 정보 조회 API (기간 필터링 적용) - 1단계 수정
    // ********************************************
    @GetMapping("/dashboard/stats")
    public DashboardStatsDto getDashboardStats(
            @RequestParam(defaultValue = "all") String periodType) { // ✅ @RequestParam 추가

        // Service 메서드에 periodType을 전달
        return trackerService.getDashboardStats(periodType);
    }

    // ********************************************
    // 스트릭 조회 API
    // ********************************************
    @GetMapping("/streak")
    public StudyStreakDto getStudyStreak() {
        return trackerService.getStudyStreak();
    }

    // ********************************************
    // 고급 통계 조회 API
    // ********************************************
    @GetMapping("/analytics")
    public AnalyticsDto getAnalytics() {
        return trackerService.getAnalytics();
    }

    // ********************************************
    // 기록 삭제 API - 2단계 기능 추가 (미리 구현)
    // ********************************************
    /**
     * 특정 videoId에 해당하는 모든 시청 기록을 삭제합니다.
     * 
     * @param videoId 삭제할 영상의 ID
     * @return 삭제 성공 응답
     */
    @DeleteMapping("/video/{videoId}")
    public ResponseEntity<Void> deleteVideoProgress(@PathVariable String videoId) { // ✅ 새로 추가
        trackerService.deleteVideoProgress(videoId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content 반환
    }

    // ********************************************
    // CSV 내보내기 API
    // ********************************************
    /**
     * 모든 학습 기록을 CSV 파일로 내보냅니다. (UTF-8 BOM 포함)
     * 
     * @return CSV 파일 다운로드
     */
    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv() {
        try {
            byte[] csvContent = csvExportService.exportStudyRecordsAsCsv();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "study_records.csv");
            
            return new ResponseEntity<>(csvContent, headers, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }    // ********************************************
    // 전체 데이터 삭제 API
    // ********************************************
    /**
     * 모든 학습 기록 및 관련 데이터를 삭제합니다.
     * 
     * @return 삭제 성공 응답
     */
    @DeleteMapping("/clear-all")
    public ResponseEntity<Void> clearAllStudyData() {
        trackerService.clearAllStudyData();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // ********************************************
    // 기존 기능 유지
    // ********************************************

    @GetMapping("/dashboard/courses")
    public List<CourseItemDto> getAllCourseItems() {
        return trackerService.getAllCourseItems();
    }

    @PostMapping("/sync")
    public SyncResponseDto syncProgress(@RequestBody VideoProgressDto dto) {
        SyncResponseDto response = trackerService.saveAndGenerateResponse(dto);
        System.out.println("[SYNC RESPONSE] 알림 필요 여부: " + response.isRequiresNotification());
        return response;
    }

    @GetMapping("/dashboard/continue")
    public ContinueWatchingDto getContinueWatchingData() {
        return trackerService.getContinueWatchingData();
    }
}