package com.fixsimulator.util;

import quickfix.DataDictionary;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.field.MsgType;

public class FixMessageUtil {
    
    /**
     * 获取消息类型的字符串表示
     */
    public static String getMsgTypeString(Message message) {
        try {
            return message.getHeader().getString(MsgType.FIELD);
        } catch (FieldNotFound e) {
            return "UNKNOWN";
        }
    }
    
    /**
     * 检查消息是否为应用消息
     */
    public static boolean isApplicationMessage(Message message) {
        try {
            String msgType = getMsgTypeString(message);
            // 根据FIX 4.2规范，应用消息类型通常不是以下管理消息类型
            return !msgType.equals("0") && // Heartbeat
                   !msgType.equals("1") && // TestRequest
                   !msgType.equals("2") && // ResendRequest
                   !msgType.equals("3") && // Reject
                   !msgType.equals("4") && // SequenceReset
                   !msgType.equals("5");   // Logout
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 检查消息是否为管理消息
     */
    public static boolean isAdminMessage(Message message) {
        return !isApplicationMessage(message);
    }
    
    /**
     * 格式化FIX消息为易读的字符串
     */
    public static String formatFixMessage(Message message) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("MsgType: ").append(getMsgTypeString(message)).append("\n");
            sb.append("Body: ").append(message.toString()).append("\n");
            return sb.toString();
        } catch (Exception e) {
            return "Error formatting message: " + e.getMessage();
        }
    }
}