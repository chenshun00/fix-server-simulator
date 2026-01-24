package com.fixsimulator.service;

import org.springframework.stereotype.Service;
import quickfix.*;
import quickfix.field.*;

import java.util.ArrayList;
import java.util.List;

@Service
public class AutoResponseService {

    public void processOrder(Message orderMessage, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
        // 从订单消息中提取OrderQty字段
        int orderQty = extractOrderQty(orderMessage);

        // 根据OrderQty获取回报序列
        List<Message> responses = generateResponses(orderQty, orderMessage);

        // 发送回报消息
        for (Message response : responses) {
            sendMessage(response, sessionID);
        }
    }

    private int extractOrderQty(Message orderMessage) throws FieldNotFound {
        return (int) orderMessage.getDouble(OrderQty.FIELD);
    }

    private List<Message> generateResponses(int orderQty, Message originalOrder) throws FieldNotFound {
        ResponseGenerationStrategy strategy = getStrategyByQuantity(orderQty);
        return strategy.getResponseSequence(originalOrder);
    }

    private ResponseGenerationStrategy getStrategyByQuantity(int quantity) {
        switch(quantity) {
            case 100: return new Quantity100Strategy();
            case 200: return new Quantity200Strategy();
            case 300: return new Quantity300Strategy();
            case 400: return new Quantity400Strategy();
            default: return new NoAutoResponseStrategy(); // 其他数量不自动回报
        }
    }

    private void sendMessage(Message response, SessionID sessionID) {
        try {
            Session.sendToTarget(response, sessionID);
        } catch (SessionNotFound e) {
            System.err.println("Session not found when sending response: " + e.getMessage());
        }
    }

    // 策略接口
    interface ResponseGenerationStrategy {
        List<Message> getResponseSequence(Message originalOrder) throws FieldNotFound;
    }

    // 数量100策略：返回NEW报文
    class Quantity100Strategy implements ResponseGenerationStrategy {
        @Override
        public List<Message> getResponseSequence(Message originalOrder) throws FieldNotFound {
            List<Message> responses = new ArrayList<>();
            Message newResponse = createNewResponse(originalOrder);
            responses.add(newResponse);
            return responses;
        }

        private Message createNewResponse(Message originalOrder) throws FieldNotFound {
            // 创建ExecutionReport报文 (ExecType=0, OrdStatus=0)
            quickfix.fix42.ExecutionReport response = new quickfix.fix42.ExecutionReport();

            // 设置必需的字段
            response.setField(new OrderID(originalOrder.getString(OrderID.FIELD)));
            response.setField(new ExecID(java.util.UUID.randomUUID().toString()));
            response.setField(new ExecType('0'));  // NEW
            response.setField(new OrdStatus('0')); // NEW
            response.setField(new Symbol(originalOrder.getString(Symbol.FIELD)));
            response.setField(new Side(originalOrder.getChar(Side.FIELD)));

            // 复制必要的字段
            copyFields(originalOrder, response);

            return response;
        }
    }

    // 数量200策略：返回NEW报文 + 部成100股报文
    class Quantity200Strategy implements ResponseGenerationStrategy {
        @Override
        public List<Message> getResponseSequence(Message originalOrder) throws FieldNotFound {
            List<Message> responses = new ArrayList<>();
            // 添加NEW报文
            responses.add(createNewResponse(originalOrder));
            // 添加部成报文 (LastShares=100, CumQty=100, LeavesQty=100)
            responses.add(createPartialFillResponse(originalOrder, 100, 100, 100));
            return responses;
        }
    }

    // 数量300策略：返回NEW报文 + 2个部成报文 + 全成报文
    class Quantity300Strategy implements ResponseGenerationStrategy {
        @Override
        public List<Message> getResponseSequence(Message originalOrder) throws FieldNotFound {
            List<Message> responses = new ArrayList<>();
            // 添加NEW报文
            responses.add(createNewResponse(originalOrder));
            // 添加第一个部成报文 (LastShares=100, CumQty=100, LeavesQty=200)
            responses.add(createPartialFillResponse(originalOrder, 100, 100, 200));
            // 添加第二个部成报文 (LastShares=100, CumQty=200, LeavesQty=100)
            responses.add(createPartialFillResponse(originalOrder, 100, 200, 100));
            // 添加全成报文 (CumQty=300, LeavesQty=0)
            responses.add(createFullFillResponse(originalOrder, 300, 0));
            return responses;
        }
    }

    // 数量400策略：返回拒绝报文
    class Quantity400Strategy implements ResponseGenerationStrategy {
        @Override
        public List<Message> getResponseSequence(Message originalOrder) throws FieldNotFound {
            List<Message> responses = new ArrayList<>();
            // 添加拒绝报文 (ExecType=8, OrdStatus=8)
            responses.add(createRejectedResponse(originalOrder));
            return responses;
        }

        private Message createRejectedResponse(Message originalOrder) throws FieldNotFound {
            // 创建拒绝报文 (ExecType=8, OrdStatus=8, Text="rejected by simulator, don't worry")
            quickfix.fix42.ExecutionReport response = new quickfix.fix42.ExecutionReport();

            // 设置必需的字段
            response.setField(new OrderID(originalOrder.getString(OrderID.FIELD)));
            response.setField(new ExecID(java.util.UUID.randomUUID().toString()));
            response.setField(new ExecType('8'));  // REJECTED
            response.setField(new OrdStatus('8')); // REJECTED
            response.setField(new Symbol(originalOrder.getString(Symbol.FIELD)));
            response.setField(new Side(originalOrder.getChar(Side.FIELD)));

            // 设置拒绝原因
            response.setField(new Text("rejected by simulator, don't worry"));

            // 复制必要的字段
            copyFields(originalOrder, response);

            return response;
        }
    }

    // 默认策略：不自动回报
    class NoAutoResponseStrategy implements ResponseGenerationStrategy {
        @Override
        public List<Message> getResponseSequence(Message originalOrder) {
            // 返回空列表，表示不自动回报
            return new ArrayList<>();
        }
    }

    // 辅助方法：创建NEW回报
    private quickfix.fix42.ExecutionReport createNewResponse(Message originalOrder) throws FieldNotFound {
        quickfix.fix42.ExecutionReport response = new quickfix.fix42.ExecutionReport();

        // 设置必需的字段
        response.setField(new OrderID(originalOrder.getString(OrderID.FIELD)));
        response.setField(new ExecID(java.util.UUID.randomUUID().toString()));
        response.setField(new ExecType('0'));  // NEW
        response.setField(new OrdStatus('0')); // NEW
        response.setField(new Symbol(originalOrder.getString(Symbol.FIELD)));
        response.setField(new Side(originalOrder.getChar(Side.FIELD)));

        // 复制必要的字段
        copyFields(originalOrder, response);

        return response;
    }

    // 辅助方法：创建部成回报
    private quickfix.fix42.ExecutionReport createPartialFillResponse(Message originalOrder, int lastShares, int cumQty, int leavesQty) throws FieldNotFound {
        quickfix.fix42.ExecutionReport response = new quickfix.fix42.ExecutionReport();

        // 设置必需的字段
        response.setField(new OrderID(originalOrder.getString(OrderID.FIELD)));
        response.setField(new ExecID(java.util.UUID.randomUUID().toString()));
        response.setField(new ExecType('1'));  // PARTIAL_FILL
        response.setField(new OrdStatus('1')); // PARTIAL_FILL
        response.setField(new Symbol(originalOrder.getString(Symbol.FIELD)));
        response.setField(new Side(originalOrder.getChar(Side.FIELD)));

        // 设置成交量相关字段
        response.setField(new LastShares((double) lastShares));
        response.setField(new CumQty((double) cumQty));
        response.setField(new LeavesQty((double) leavesQty));

        // 复制必要的字段
        copyFields(originalOrder, response);

        return response;
    }

    // 辅助方法：创建全成回报
    private quickfix.fix42.ExecutionReport createFullFillResponse(Message originalOrder, int cumQty, int leavesQty) throws FieldNotFound {
        quickfix.fix42.ExecutionReport response = new quickfix.fix42.ExecutionReport();

        // 设置必需的字段
        response.setField(new OrderID(originalOrder.getString(OrderID.FIELD)));
        response.setField(new ExecID(java.util.UUID.randomUUID().toString()));
        response.setField(new ExecType('2'));  // FILL
        response.setField(new OrdStatus('2')); // FILLED
        response.setField(new Symbol(originalOrder.getString(Symbol.FIELD)));
        response.setField(new Side(originalOrder.getChar(Side.FIELD)));

        // 设置成交量相关字段
        response.setField(new LastShares((double) extractOrderQty(originalOrder))); // 最后一次成交量等于剩余量
        response.setField(new CumQty((double) cumQty));
        response.setField(new LeavesQty((double) leavesQty));

        // 复制必要的字段
        copyFields(originalOrder, response);

        return response;
    }

    // 辅助方法：复制必要字段
    private void copyFields(Message from, quickfix.fix42.ExecutionReport to) throws FieldNotFound {
        // 复制ClOrdID
        if(from.isSetField(ClOrdID.FIELD)) {
            to.setField(new ClOrdID(from.getString(ClOrdID.FIELD)));
        }

        // 复制OrderQty
        if(from.isSetField(OrderQty.FIELD)) {
            to.setField(new OrderQty(from.getDouble(OrderQty.FIELD)));
        }

        // 复制Price（如果存在）
        if(from.isSetField(Price.FIELD)) {
            to.setField(new Price(from.getDouble(Price.FIELD)));
        }

        // 复制Account（如果存在）
        if(from.isSetField(Account.FIELD)) {
            to.setField(new Account(from.getString(Account.FIELD)));
        }

        // 复制TimeInForce（如果存在）
        if(from.isSetField(TimeInForce.FIELD)) {
            to.setField(new TimeInForce((char)from.getInt(TimeInForce.FIELD)));
        }
    }
}