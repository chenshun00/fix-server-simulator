package com.fixsimulator.interfaces.fix;

import com.fixsimulator.interfaces.rest.dto.ManualResponseRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import quickfix.Message;
import quickfix.field.*;

import java.math.BigDecimal;

@Slf4j
@Component
public class ExecutionReportFactory {

    public Message createExecutionReport(ManualResponseRequest request) {
        String orderId = "ORD_" + System.currentTimeMillis();
        String execId = "EXEC_" + System.currentTimeMillis();

        // 创建 ExecutionReport - 使用正确的构造器，包含 Symbol
        quickfix.fix42.ExecutionReport report = new quickfix.fix42.ExecutionReport(
                new OrderID(orderId),
                new ExecID(execId),
                new ExecTransType(ExecTransType.NEW),
                new ExecType(request.getExecType().charAt(0)),
                new OrdStatus(request.getOrdStatus().charAt(0)),
                new Symbol(request.getSymbol()),
                new Side(request.getSide().charAt(0)),
                new LeavesQty(getLeavesQty(request).doubleValue()),
                new CumQty(getCumQty(request).doubleValue()),
                new AvgPx(getAvgPx(request).doubleValue())
        );

        // 设置基本字段
        report.set(new ClOrdID(request.getClOrdId()));
        report.set(new OrderQty(request.getOrderQty().doubleValue()));
        report.setField(new TransactTime());

        // 根据条件字段设置额外内容
        addConditionalFields(report, request);

        log.info("Created manual ExecutionReport: execType={}, ordStatus={}, clOrdId={}",
                request.getExecType(), request.getOrdStatus(), request.getClOrdId());

        return report;
    }

    private void addConditionalFields(quickfix.fix42.ExecutionReport report, ManualResponseRequest request) {
        String execType = request.getExecType();

        switch (execType) {
            case "1": // PARTIAL_FILL
                if (request.getLastQty() != null) {
                    report.setField(new LastQty(request.getLastQty().doubleValue()));
                }
                if (request.getLastPx() != null) {
                    report.setField(new LastPx(request.getLastPx().doubleValue()));
                }
                break;

            case "2": // FILL
                if (request.getLastQty() != null) {
                    report.setField(new LastQty(request.getLastQty().doubleValue()));
                }
                if (request.getLastPx() != null) {
                    report.setField(new LastPx(request.getLastPx().doubleValue()));
                }
                break;

            case "4": // CANCEL
            case "5": // REPLACE
                if (request.getOrigClOrdId() != null) {
                    report.set(new OrigClOrdID(request.getOrigClOrdId()));
                }
                if (request.getText() != null) {
                    report.set(new Text(request.getText()));
                }
                break;

            case "8": // REJECTED
                if (request.getText() != null) {
                    report.set(new Text(request.getText()));
                }
                break;

            case "0": // NEW
            default:
                // 无额外字段
                break;
        }
    }

    private BigDecimal getLeavesQty(ManualResponseRequest request) {
        String execType = request.getExecType();
        BigDecimal orderQty = request.getOrderQty();

        switch (execType) {
            case "2": // FILL - 剩余数量为0
                return BigDecimal.ZERO;
            case "1": // PARTIAL_FILL - 需要计算
                if (request.getLastQty() != null) {
                    return orderQty.subtract(request.getLastQty());
                }
                return orderQty;
            case "4": // CANCEL - 剩余数量为0
            case "8": // REJECTED - 剩余数量为0
                return BigDecimal.ZERO;
            default: // NEW - 剩余数量等于委托数量
                return orderQty;
        }
    }

    private BigDecimal getCumQty(ManualResponseRequest request) {
        String execType = request.getExecType();

        if ("2".equals(execType)) {
            return request.getOrderQty(); // FILL 时累计成交量等于委托量
        }
        if ("1".equals(execType) && request.getLastQty() != null) {
            return request.getLastQty(); // PARTIAL_FILL 时等于最后成交量
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal getAvgPx(ManualResponseRequest request) {
        if (request.getLastPx() != null) {
            return request.getLastPx();
        }
        if (request.getPrice() != null) {
            return request.getPrice();
        }
        return BigDecimal.ZERO;
    }
}
