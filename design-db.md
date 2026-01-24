# FIX Server Simulator - 数据库设计文档 (design-db.md)

## 1. 概述

本文档定义了FIX Server Simulator的数据库表结构设计，严格按照PRD要求，区分事实数据和派生数据。

## 2. 设计原则

- **原始FIX报文是唯一事实源**：所有解析字段均为派生数据，仅用于查询与展示
- **数据完整性**：确保FIX协议所需的所有关键信息都被持久化
- **查询支持**：支持PRD要求的按Symbol、ClOrdID、时间查询

## 3. 表清单及字段定义

### 3.1 fix_messages 表 (事实数据表)

| 字段名 | 类型 | 约束 | 含义 | 数据性质 |
|--------|------|------|------|----------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 报文主键 | 事实数据 |
| session_key | VARCHAR(120) | NOT NULL | Session唯一标识：BeginString+SenderCompID+TargetCompID | 事实数据 |
| msg_seq_num | INT | NOT NULL | 消息序列号 | 事实数据 |
| msg_type | VARCHAR(10) | NOT NULL | 消息类型 | 事实数据 |
| raw_message | TEXT | NOT NULL | 原始FIX报文内容 | 事实数据 |
| receive_time | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 接收时间 | 事实数据 |
| direction | ENUM('INBOUND','OUTBOUND') | NOT NULL | 报文方向：入站(INBOUND)或出站(OUTBOUND) | 事实数据 |

### 3.2 parsed_fields 表 (派生数据表)

| 字段名 | 类型 | 约束 | 含义 | 数据性质 |
|--------|------|------|------|----------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 解析字段主键 | 事实数据 |
| message_id | BIGINT | NOT NULL | 关联报文ID | 事实数据 |
| msg_type | VARCHAR(10) | INDEX | 消息类型 | 派生数据 |
| cl_ord_id | VARCHAR(50) | INDEX | 客户订单ID | 派生数据 |
| orig_cl_ord_id | VARCHAR(50) | INDEX | 原始客户订单ID | 派生数据 |
| symbol | VARCHAR(50) | INDEX | 证券代码 | 派生数据 |
| order_qty | DECIMAL(15,4) | DEFAULT NULL | 订单数量 | 派生数据 |
| price | DECIMAL(15,4) | DEFAULT NULL | 价格 | 派生数据 |
| side | CHAR(1) | DEFAULT NULL | 买卖方向 | 派生数据 |
| sender_comp_id | VARCHAR(50) | DEFAULT NULL | 发送方公司ID | 派生数据 |
| target_comp_id | VARCHAR(50) | DEFAULT NULL | 目标方公司ID | 派生数据 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 | 事实数据 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 | 事实数据 |

## 4. 表关系说明

- fix_messages 与 parsed_fields: 通过应用层逻辑关联 (一条原始报文最多对应一组解析字段)
- parsed_fields.message_id 与 fix_messages.id 通过应用层逻辑关联，无外键约束
- 删除原始报文记录时，需在应用层同步删除对应的解析字段记录

## 5. 数据分类说明

### 5.1 事实数据 (Source of Truth)
- fix_messages 表中的所有字段（除自增ID外）均属于事实数据
- 原始FIX报文内容 (raw_message) 是系统的唯一事实源
- 会话标识 (session_key)、消息序列号 (msg_seq_num)、消息类型 (msg_type)、接收时间 (receive_time)、方向 (direction) 均为事实数据

### 5.2 派生数据 (Derived View)
- parsed_fields 表中的所有业务字段均为派生数据
- 这些字段是从原始FIX报文中解析出来的，仅用于查询与展示
- 包括但不限于: ClOrdID, Symbol, OrderQty, Price, Side, ExecType, OrdStatus等

## 6. DDL语句

```sql
-- 创建fix_messages表 (事实数据表)
CREATE TABLE fix_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_key VARCHAR(120) NOT NULL COMMENT 'Session唯一标识：BeginString+SenderCompID+TargetCompID',
    msg_seq_num INT NOT NULL COMMENT '消息序列号',
    msg_type VARCHAR(10) NOT NULL COMMENT '消息类型',
    raw_message TEXT NOT NULL COMMENT '原始FIX报文内容',
    receive_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '接收时间',
    direction ENUM('INBOUND', 'OUTBOUND') NOT NULL COMMENT '报文方向：入站(INBOUND)或出站(OUTBOUND)'
);

-- 创建parsed_fields表 (派生数据表)
CREATE TABLE parsed_fields (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_id BIGINT NOT NULL COMMENT '关联报文ID',
    msg_type VARCHAR(10) COMMENT '消息类型',
    cl_ord_id VARCHAR(50) COMMENT '客户订单ID',
    orig_cl_ord_id VARCHAR(50) COMMENT '原始客户订单ID',
    symbol VARCHAR(50) COMMENT '证券代码',
    order_qty DECIMAL(15,4) COMMENT '订单数量',
    price DECIMAL(15,4) COMMENT '价格',
    side CHAR(1) COMMENT '买卖方向',
    sender_comp_id VARCHAR(50) COMMENT '发送方公司ID',
    target_comp_id VARCHAR(50) COMMENT '目标方公司ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 创建查询所需的索引
CREATE INDEX idx_fix_msg_session_key ON fix_messages(session_key);
CREATE INDEX idx_fix_msg_receive_time ON fix_messages(receive_time);
CREATE INDEX idx_fix_msg_direction ON fix_messages(direction);
CREATE INDEX idx_parsed_field_cl_ord_id ON parsed_fields(cl_ord_id);
CREATE INDEX idx_parsed_field_symbol ON parsed_fields(symbol);
CREATE INDEX idx_parsed_field_created_at ON parsed_fields(created_at);
```

## 7. 查询支持说明

- **按ClOrdID查询**：通过parsed_fields表的cl_ord_id字段支持
- **按Symbol查询**：通过parsed_fields表的symbol字段支持
- **按时间查询**：通过parsed_fields表的created_at字段支持