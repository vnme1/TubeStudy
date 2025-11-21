package com.tubestudy.tracker.repository;

import com.tubestudy.tracker.entity.DistractionKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DistractionKeywordRepository extends JpaRepository<DistractionKeyword, Long> {

    // 활성화된 모든 키워드 조회
    List<DistractionKeyword> findAllByIsActiveTrue();

    // 키워드로 검색
    DistractionKeyword findByKeyword(String keyword);

    // 카테고리별 조회
    List<DistractionKeyword> findByCategory(String category);

    // 사용자 커스텀 키워드 조회
    List<DistractionKeyword> findByIsCustomTrue();
}
