package com.fixsimulator.application;

import com.fixsimulator.domain.message.ParsedMessage;
import com.fixsimulator.domain.message.ParsedMessageRepository;
import com.fixsimulator.interfaces.rest.dto.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageApplicationService {

    private final ParsedMessageRepository messageRepository;

    public Page<MessageResponse> searchMessages(String symbol, String clOrdId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<ParsedMessage> messages = messageRepository.searchMessages(symbol, clOrdId, pageRequest);
        return messages.map(this::toResponse);
    }

    private MessageResponse toResponse(ParsedMessage message) {
        return MessageResponse.builder()
                .id(message.getId())
                .msgType(message.getMsgType())
                .symbol(message.getSymbol())
                .clOrdId(message.getClOrdId())
                .origClOrdId(message.getOrigClOrdId())
                .price(message.getPrice())
                .orderQty(message.getOrderQty())
                .side(message.getSide())
                .ordType(message.getOrdType())
                .receivedAt(message.getReceivedAt())
                .build();
    }
}
