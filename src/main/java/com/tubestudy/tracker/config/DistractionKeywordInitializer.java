package com.tubestudy.tracker.config;

import com.tubestudy.tracker.entity.DistractionKeyword;
import com.tubestudy.tracker.repository.DistractionKeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 애플리케이션 시작 시 기본 딴짓 키워드를 초기화합니다.
 */
@Component
@RequiredArgsConstructor
public class DistractionKeywordInitializer implements CommandLineRunner {

    private final DistractionKeywordRepository repository;

    @Override
    public void run(String... args) throws Exception {
        // 이미 키워드가 있으면 초기화하지 않음
        if (repository.count() > 0) {
            return;
        }

        List<DistractionKeyword> defaultKeywords = Arrays.asList(
                DistractionKeyword.builder()
                        .keyword("vlog")
                        .category("Entertainment")
                        .isActive(true)
                        .alertMessage("Vlog는 잠시 후에! 지금은 공부할 시간입니다. 집중하세요! 👀")
                        .isCustom(false)
                        .build(),

                DistractionKeyword.builder()
                        .keyword("브이로그")
                        .category("Entertainment")
                        .isActive(true)
                        .alertMessage("Vlog는 잠시 후에! 지금은 공부할 시간입니다. 집중하세요! 👀")
                        .isCustom(false)
                        .build(),

                DistractionKeyword.builder()
                        .keyword("게임")
                        .category("Game")
                        .isActive(true)
                        .alertMessage("게임 유혹을 참아내고 다시 강의로 돌아오세요. 🕹️")
                        .isCustom(false)
                        .build(),

                DistractionKeyword.builder()
                        .keyword("gameplay")
                        .category("Game")
                        .isActive(true)
                        .alertMessage("게임 유혹을 참아내고 다시 강의로 돌아오세요. 🕹️")
                        .isCustom(false)
                        .build(),

                DistractionKeyword.builder()
                        .keyword("asmr")
                        .category("Entertainment")
                        .isActive(true)
                        .alertMessage("휴식 시간에는 좋습니다. 하지만 지금은 강의를 시청 중인 것 같아요! 🎧")
                        .isCustom(false)
                        .build(),

                DistractionKeyword.builder()
                        .keyword("예능")
                        .category("Entertainment")
                        .isActive(true)
                        .alertMessage("휴식 시간에는 좋습니다. 하지만 지금은 강의를 시청 중인 것 같아요! 🎧")
                        .isCustom(false)
                        .build());

        repository.saveAll(defaultKeywords);
        System.out.println("✅ Default distraction keywords initialized: " + defaultKeywords.size() + " keywords");
    }
}
