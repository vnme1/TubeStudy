package com.tubestudy.tracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller // 이 클래스가 HTTP 요청을 처리하는 컨트롤러임을 명시
public class DashboardController {

    /**
     * 루트 URL ("/")로 접속하면 study.html로 리다이렉트합니다.
     */
    @GetMapping("/")
    public String redirectToDashboard() {
        // 'redirect:' 접두사를 사용하면 브라우저에게 해당 경로로 재요청하라고 명령합니다.
        return "redirect:/study.html";
    }
}