package com.fixsimulator.interfaces.rest;

import com.fixsimulator.interfaces.rest.dto.HealthResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public HealthResponse health() {
        return HealthResponse.builder()
                .status("UP")
                .message("FIX Server Simulator is running")
                .timestamp(LocalDateTime.now())
                .build();
    }
}
