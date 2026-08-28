package com.project.tasktracker.controller;

import com.project.tasktracker.dto.SessionDto;
import com.project.tasktracker.service.SessionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public SessionDto createSession() {
        return sessionService.createSession();
    }
}
