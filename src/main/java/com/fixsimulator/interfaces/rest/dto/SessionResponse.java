package com.fixsimulator.interfaces.rest.dto;

import com.fixsimulator.domain.session.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponse {
    private Long id;
    private String sessionId;
    private String senderCompId;
    private String targetCompId;
    private SessionStatus status;
    private Integer port;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
