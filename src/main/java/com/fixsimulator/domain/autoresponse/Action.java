package com.fixsimulator.domain.autoresponse;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Action {
    private String type;  // "SEND_REPORT"
    private long delayMs = 0;  // 延迟执行时间（毫秒）

    // 存储所有额外的属性（execType, ordStatus, lastQty, text 等）
    private Map<String, Object> params = new HashMap<>();

    // 捕获所有未映射的属性到 params 中
    @JsonAnySetter
    public void setParam(String key, Object value) {
        params.put(key, value);
    }
}
