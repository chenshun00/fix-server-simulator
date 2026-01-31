package com.fixsimulator.domain.autoresponse;

import lombok.Data;

import java.util.Map;

@Data
public class Action {
    private String type;  // "SEND_REPORT"
    private Map<String, Object> params;
    private long delayMs;  // 延迟执行时间（毫秒）
}
