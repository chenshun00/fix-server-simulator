package com.fixsimulator.interfaces.rest;

import com.fixsimulator.application.MessageApplicationService;
import com.fixsimulator.interfaces.rest.dto.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageApplicationService messageApplicationService;

    @GetMapping
    public Page<MessageResponse> searchMessages(
            @RequestParam(required = false) String symbol,
            @RequestParam(required = false) String clOrdId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return messageApplicationService.searchMessages(symbol, clOrdId, page, size);
    }
}
