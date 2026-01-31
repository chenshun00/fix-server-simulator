package com.fixsimulator.interfaces.rest.dto;

import com.fixsimulator.domain.message.MsgType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private Long id;
    private MsgType msgType;
    private String symbol;
    private String clOrdId;
    private String origClOrdId;
    private BigDecimal price;
    private BigDecimal orderQty;
    private String side;
    private String ordType;
    private LocalDateTime receivedAt;
}
