package com.tubestudy.tracker.service;

import com.tubestudy.tracker.dto.DistractionKeywordDto;
import com.tubestudy.tracker.entity.DistractionKeyword;
import com.tubestudy.tracker.repository.DistractionKeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 딴짓 키워드 관리 서비스
 * 사용자가 커스텀 딴짓 키워드를 추가/수정/삭제할 수 있습니다.
 */
@Service
@RequiredArgsConstructor
public class DistractionKeywordService {

    private final DistractionKeywordRepository repository;

    /**
     * 모든 활성 키워드 조회
     */
    @Transactional(readOnly = true)
    public List<DistractionKeywordDto> getAllActiveKeywords() {
        return repository.findAllByIsActiveTrue()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 모든 키워드 조회 (활성/비활성 포함)
     */
    @Transactional(readOnly = true)
    public List<DistractionKeywordDto> getAllKeywords() {
        return repository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 새로운 키워드 추가
     */
    @Transactional
    public DistractionKeywordDto addKeyword(DistractionKeywordDto dto) {
        DistractionKeyword keyword = DistractionKeyword.builder()
                .keyword(dto.getKeyword())
                .category(dto.getCategory() != null ? dto.getCategory() : "custom")
                .isActive(true)
                .alertMessage(dto.getAlertMessage() != null ? dto.getAlertMessage() : "집중을 잃지 마세요!")
                .isCustom(true)
                .createdAt(LocalDateTime.now())
                .build();

        DistractionKeyword saved = repository.save(keyword);
        return toDto(saved);
    }

    /**
     * 기존 키워드 수정
     */
    @Transactional
    public DistractionKeywordDto updateKeyword(Long id, DistractionKeywordDto dto) {
        DistractionKeyword keyword = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("키워드를 찾을 수 없습니다: " + id));

        if (dto.getKeyword() != null) {
            keyword.setKeyword(dto.getKeyword());
        }
        if (dto.getCategory() != null) {
            keyword.setCategory(dto.getCategory());
        }
        if (dto.getAlertMessage() != null) {
            keyword.setAlertMessage(dto.getAlertMessage());
        }

        DistractionKeyword updated = repository.save(keyword);
        return toDto(updated);
    }

    /**
     * 키워드 활성화/비활성화 토글
     */
    @Transactional
    public DistractionKeywordDto toggleKeywordActive(Long id) {
        DistractionKeyword keyword = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("키워드를 찾을 수 없습니다: " + id));

        keyword.setActive(!keyword.isActive());
        DistractionKeyword updated = repository.save(keyword);
        return toDto(updated);
    }

    /**
     * 키워드 삭제 (커스텀 키워드만 삭제 가능)
     */
    @Transactional
    public void deleteKeyword(Long id) {
        DistractionKeyword keyword = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("키워드를 찾을 수 없습니다: " + id));

        if (!keyword.isCustom()) {
            throw new IllegalArgumentException("기본 키워드는 삭제할 수 없습니다.");
        }

        repository.deleteById(id);
    }

    /**
     * Entity를 DTO로 변환
     */
    private DistractionKeywordDto toDto(DistractionKeyword keyword) {
        return DistractionKeywordDto.builder()
                .id(keyword.getId())
                .keyword(keyword.getKeyword())
                .category(keyword.getCategory())
                .isActive(keyword.isActive())
                .alertMessage(keyword.getAlertMessage())
                .isCustom(keyword.isCustom())
                .createdAt(keyword.getCreatedAt())
                .build();
    }

    /**
     * DTO를 Entity로 변환
     */
    public DistractionKeyword toEntity(DistractionKeywordDto dto) {
        return DistractionKeyword.builder()
                .id(dto.getId())
                .keyword(dto.getKeyword())
                .category(dto.getCategory())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .alertMessage(dto.getAlertMessage())
                .isCustom(dto.getIsCustom() != null ? dto.getIsCustom() : false)
                .createdAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : LocalDateTime.now())
                .build();
    }
}
