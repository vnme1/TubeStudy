package com.tubestudy.tracker.repository;

import com.tubestudy.tracker.entity.StudyStreak;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StudyStreakRepository extends JpaRepository<StudyStreak, Long> {
    // 기본 스트릭 조회 (ID = 1로 고정)
    Optional<StudyStreak> findById(Long id);
}
