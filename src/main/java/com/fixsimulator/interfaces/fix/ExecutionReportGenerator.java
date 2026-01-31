package com.fixsimulator.interfaces.fix;

import com.fixsimulator.domain.autoresponse.Action;
import com.fixsimulator.domain.message.FixMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import quickfix.Message;
import quickfix.field.*;
import quickfix.fix42.ExecutionReport;

import java.math.BigDecimal;

@Slf4j
@Component
public class ExecutionReportGenerator {

    public Message generate(Action action, FixMessage originalMessage) {
        String execId = "EXEC_" + System.currentTimeMillis();
        String orderId = "ORD_" + System.currentTimeMillis();

        String execTypeStr = action.getParams().get("execType").toString();
        String ordStatusStr = action.getParams().get("ordStatus").toString();

        ExecutionReport report = new ExecutionReport(
                new OrderID(orderId),
                new ExecID(execId),
                new ExecTransType(ExecTransType.NEW),
                new ExecType(execTypeStr.charAt(0)),
                new OrdStatus(ordStatusStr.charAt(0)),
                new Symbol(originalMessage.getSymbol()),
                new Side(originalMessage.getSide().getCode().charAt(0)),
                new LeavesQty(originalMessage.getOrderQty().doubleValue()),
                new CumQty(0),
                new AvgPx(0)
        );

        // 设置基本字段
        report.set(new ClOrdID(originalMessage.getClOrdID()));
        report.set(new OrderQty(originalMessage.getOrderQty().doubleValue()));
        report.setField(new TransactTime());

        // 根据动作类型设置额外字段
        Object execType = action.getParams().get("execType");
        if ("1".equals(execType)) { // PARTIAL_FILL
            Object lastQty = action.getParams().get("lastQty");
            if (lastQty != null) {
                report.setField(new LastQty(new BigDecimal(lastQty.toString()).doubleValue()));
            }
        }

        if ("8".equals(execType)) { // REJECTED
            Object text = action.getParams().get("text");
            if (text != null) {
                report.setField(new Text(text.toString()));
            }
        }

        return report;
    }
}
