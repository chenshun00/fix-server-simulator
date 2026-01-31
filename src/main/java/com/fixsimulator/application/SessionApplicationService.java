package com.fixsimulator.application;

import com.fixsimulator.domain.session.Session;
import com.fixsimulator.domain.session.SessionRepository;
import com.fixsimulator.interfaces.rest.dto.SessionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SessionApplicationService {

    private final SessionRepository sessionRepository;

    public List<SessionResponse> getAllSessions() {
        return sessionRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public SessionResponse getSession(String sessionId) {
        Session session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
        return toResponse(session);
    }

    private SessionResponse toResponse(Session session) {
        return SessionResponse.builder()
                .id(session.getId())
                .sessionId(session.getSessionId())
                .senderCompId(session.getSenderCompId())
                .targetCompId(session.getTargetCompId())
                .status(session.getStatus())
                .port(session.getPort())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .build();
    }
}
