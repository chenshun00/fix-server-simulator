package com.fixsimulator.controller;

import com.fixsimulator.service.ResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/responses")
public class ResponseController {
    
    @Autowired
    private ResponseService responseService;
    
    /**
     * 发送ExecutionReport回报 - 部分成交（V1.0.1: 从报文数据中推导sessionKey）
     */
    @PostMapping("/execution-report/partial-fill")
    public ResponseEntity<?> sendPartialFillExecutionReport(
            @RequestParam String clOrdId,
            @RequestParam String execType,
            @RequestParam String ordStatus,
            @RequestParam BigDecimal lastQty,
            @RequestParam BigDecimal cumQty,
            @RequestParam BigDecimal leavesQty) {

        try {
            // 验证必需参数
            if (execType == null || execType.isEmpty()) {
                return ResponseEntity.badRequest().body("Parameter 'execType' is required");
            }
            if (ordStatus == null || ordStatus.isEmpty()) {
                return ResponseEntity.badRequest().body("Parameter 'ordStatus' is required");
            }
            if (lastQty == null) {
                return ResponseEntity.badRequest().body("Parameter 'lastQty' is required");
            }
            if (cumQty == null) {
                return ResponseEntity.badRequest().body("Parameter 'cumQty' is required");
            }
            if (leavesQty == null) {
                return ResponseEntity.badRequest().body("Parameter 'leavesQty' is required");
            }

            boolean success = responseService.sendExecutionReportWithDerivedSession(
                    clOrdId, execType, ordStatus, lastQty, cumQty, leavesQty);

            if (success) {
                return ResponseEntity.ok().body("Partial fill execution report sent successfully");
            } else {
                return ResponseEntity.status(500).body("Failed to send execution report");
            }
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error sending execution report: " + e.getMessage());
        }
    }

    /**
     * 发送ExecutionReport回报 - 完全成交（V1.0.1: 从报文数据中推导sessionKey）
     */
    @PostMapping("/execution-report/fill")
    public ResponseEntity<?> sendFillExecutionReport(
            @RequestParam String clOrdId,
            @RequestParam String execType,
            @RequestParam String ordStatus,
            @RequestParam BigDecimal lastQty,
            @RequestParam BigDecimal cumQty,
            @RequestParam BigDecimal leavesQty) {

        try {
            // 验证必需参数
            if (execType == null || execType.isEmpty()) {
                return ResponseEntity.badRequest().body("Parameter 'execType' is required");
            }
            if (ordStatus == null || ordStatus.isEmpty()) {
                return ResponseEntity.badRequest().body("Parameter 'ordStatus' is required");
            }
            if (lastQty == null) {
                return ResponseEntity.badRequest().body("Parameter 'lastQty' is required");
            }
            if (cumQty == null) {
                return ResponseEntity.badRequest().body("Parameter 'cumQty' is required");
            }
            if (leavesQty == null) {
                return ResponseEntity.badRequest().body("Parameter 'leavesQty' is required");
            }

            boolean success = responseService.sendExecutionReportWithDerivedSession(
                    clOrdId, execType, ordStatus, lastQty, cumQty, leavesQty);

            if (success) {
                return ResponseEntity.ok().body("Fill execution report sent successfully");
            } else {
                return ResponseEntity.status(500).body("Failed to send execution report");
            }
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error sending execution report: " + e.getMessage());
        }
    }

    /**
     * 发送New回报（已报）（V1.0.1: 从报文数据中推导sessionKey）
     */
    @PostMapping("/execution-report/new")
    public ResponseEntity<?> sendNewOrderAck(
            @RequestParam String clOrdId) {

        try {
            boolean success = responseService.sendNewOrderAckWithDerivedSession(clOrdId);

            if (success) {
                return ResponseEntity.ok().body("New order acknowledgment sent successfully");
            } else {
                return ResponseEntity.status(500).body("Failed to send new order acknowledgment");
            }
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error sending new order acknowledgment: " + e.getMessage());
        }
    }

    /**
     * 发送Reject回报（V1.0.1: 从报文数据中推导sessionKey）
     */
    @PostMapping("/reject")
    public ResponseEntity<?> sendReject(
            @RequestParam String clOrdId,
            @RequestParam(required = false) String text) {

        try {
            boolean success = responseService.sendRejectWithDerivedSession(clOrdId, text);

            if (success) {
                return ResponseEntity.ok().body("Reject message sent successfully");
            } else {
                return ResponseEntity.status(500).body("Failed to send reject message");
            }
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error sending reject message: " + e.getMessage());
        }
    }

    /**
     * 发送订单修改确认（V1.0.1: 从报文数据中推导sessionKey）
     */
    @PostMapping("/execution-report/modify")
    public ResponseEntity<?> sendOrderModifyConfirmation(
            @RequestParam String clOrdId,
            @RequestParam String execType,
            @RequestParam String ordStatus) {

        try {
            // 验证必需参数
            if (execType == null || execType.isEmpty()) {
                return ResponseEntity.badRequest().body("Parameter 'execType' is required");
            }
            if (ordStatus == null || ordStatus.isEmpty()) {
                return ResponseEntity.badRequest().body("Parameter 'ordStatus' is required");
            }

            boolean success = responseService.sendOrderModifyConfirmationWithDerivedSession(clOrdId, execType, ordStatus);

            if (success) {
                return ResponseEntity.ok().body("Order modify confirmation sent successfully");
            } else {
                return ResponseEntity.status(500).body("Failed to send order modify confirmation");
            }
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error sending order modify confirmation: " + e.getMessage());
        }
    }

    /**
     * 发送订单取消确认（V1.0.1: 从报文数据中推导sessionKey）
     */
    @PostMapping("/execution-report/cancel")
    public ResponseEntity<?> sendOrderCancelConfirmation(
            @RequestParam String clOrdId,
            @RequestParam String execType,
            @RequestParam String ordStatus) {

        try {
            // 验证必需参数
            if (execType == null || execType.isEmpty()) {
                return ResponseEntity.badRequest().body("Parameter 'execType' is required");
            }
            if (ordStatus == null || ordStatus.isEmpty()) {
                return ResponseEntity.badRequest().body("Parameter 'ordStatus' is required");
            }

            boolean success = responseService.sendOrderCancelConfirmationWithDerivedSession(clOrdId, execType, ordStatus);

            if (success) {
                return ResponseEntity.ok().body("Order cancel confirmation sent successfully");
            } else {
                return ResponseEntity.status(500).body("Failed to send order cancel confirmation");
            }
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error sending order cancel confirmation: " + e.getMessage());
        }
    }
}