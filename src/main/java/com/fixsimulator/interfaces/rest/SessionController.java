package com.fixsimulator.interfaces.rest;

import com.fixsimulator.application.SessionApplicationService;
import com.fixsimulator.interfaces.rest.dto.SessionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionApplicationService sessionApplicationService;

    @GetMapping
    public List<SessionResponse> getAllSessions() {
        return sessionApplicationService.getAllSessions();
    }

    @GetMapping("/{sessionId}")
    public SessionResponse getSession(@PathVariable String sessionId) {
        return sessionApplicationService.getSession(sessionId);
    }
}
