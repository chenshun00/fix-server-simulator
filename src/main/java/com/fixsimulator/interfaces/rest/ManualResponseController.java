package com.fixsimulator.interfaces.rest;

import com.fixsimulator.application.ManualResponseService;
import com.fixsimulator.interfaces.rest.dto.ApiResponse;
import com.fixsimulator.interfaces.rest.dto.ManualResponseRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/responses")
@RequiredArgsConstructor
public class ManualResponseController {

    private final ManualResponseService manualResponseService;

    @PostMapping("/manual")
    public ResponseEntity<ApiResponse<?>> sendManualResponse(
            @RequestBody ManualResponseRequest request
    ) {
        try {
            manualResponseService.sendManualResponse(request);
            return ResponseEntity.ok(ApiResponse.success("回报发送成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("参数错误: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("回报发送失败: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("系统错误: " + e.getMessage()));
        }
    }
}
