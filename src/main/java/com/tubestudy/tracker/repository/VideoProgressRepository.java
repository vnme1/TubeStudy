package com.tubestudy.tracker.repository;

import com.tubestudy.tracker.entity.VideoProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;

// <엔티티 타입, ID 타입>
public interface VideoProgressRepository extends JpaRepository<VideoProgress, String> {
    // findById, findAll, save 등은 JpaRepository에서 이미 제공됩니다.
    // 가장 최근 동기화된 시간을 기준으로 최상위(Top) 1개의 영상을 찾습니다.
    Optional<VideoProgress> findTopByOrderByLastSyncedAtDesc();

    // 모든 영상을 마지막 동기화 시간 내림차순으로 조회합니다.
    List<VideoProgress> findAllByOrderByLastSyncedAtDesc();

    // 특정 기간 내 동기화된 기록만 조회합니다.
    List<VideoProgress> findByLastSyncedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // videoId로 모든 기록 삭제합니다.
    void deleteByVideoId(String videoId);
}