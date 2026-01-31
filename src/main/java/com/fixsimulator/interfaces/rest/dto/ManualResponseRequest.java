package com.fixsimulator.interfaces.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManualResponseRequest {
    // 识别信息
    private String sessionId;

    // 固定字段
    private String clOrdId;
    private String symbol;
    private String side;
    private BigDecimal orderQty;
    private BigDecimal price;

    // 回报类型
    private String execType;
    private String ordStatus;

    // 条件字段
    private BigDecimal lastQty;
    private BigDecimal lastPx;
    private BigDecimal cumQty;
    private BigDecimal avgPx;
    private String origClOrdId;
    private String text;
}
