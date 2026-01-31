# FIX 回报报文案例 (FIX 4.2)

本文档包含生产可用的 FIX 4.2 协议回报报文案例，适用于 FIX Server Simulator 系统。

---

## 1. ExecutionReport - 已报 (NEW)

**场景**: 订单已被交易所/撮合引擎接受，处于"已报"状态

**消息类型**: 8 (ExecutionReport)
**ExecType**: 0 (NEW)
**OrdStatus**: 0 (NEW)

```
8=FIX.4.2|9=236|35=8|34=2|49=FIXSIMULATOR|52=20260125-10:30:15.123|56=GATEWAY|37=ORD202601250001|17=EXEC_1706171415123|150=0|39=0|55=600519|54=1|38=100|151=0|14=0|6=0|11=CLORD202601250001|20=0|10=078|
```

### 字段说明

| Tag | 字段名 | 值 | 说明 |
|-----|--------|-----|------|
| 8 | BeginString | FIX.4.2 | 协议版本 |
| 9 | BodyLength | 236 | 报文长度 |
| 35 | MsgType | 8 | ExecutionReport |
| 34 | MsgSeqNum | 2 | 消息序列号 |
| 49 | SenderCompID | FIXSIMULATOR | 发送方ID |
| 52 | SendingTime | 20260125-10:30:15.123 | 发送时间 |
| 56 | TargetCompID | GATEWAY | 接收方ID |
| 37 | OrderID | ORD202601250001 | 订单ID |
| 17 | ExecID | EXEC_1706171415123 | 执行ID |
| 150 | ExecType | 0 (NEW) | 执行类型：新订单 |
| 39 | OrdStatus | 0 (NEW) | 订单状态：新订单 |
| 55 | Symbol | 600519 | 证券代码 |
| 54 | Side | 1 (BUY) | 买卖方向：买入 |
| 38 | OrderQty | 100 | 委托数量 |
| 151 | LeavesQty | 100 | 剩余数量 |
| 14 | CumQty | 0 | 累计成交量 |
| 6 | AvgPx | 0 | 平均成交价 |
| 11 | ClOrdID | CLORD202601250001 | 客户端订单ID |
| 20 | ExecTransType | 0 (NEW) | 执行事务类型 |
| 10 | Checksum | 078 | 校验和 |

---

## 2. ExecutionReport - 部分成交 (PARTIAL_FILL)

**场景**: 订单部分成交

**消息类型**: 8 (ExecutionReport)
**ExecType**: 1 (PARTIAL_FILL)
**OrdStatus**: 1 (PARTIALLY_FILLED)

```
8=FIX.4.2|9=245|35=8|34=3|49=FIXSIMULATOR|52=20260125-10:30:20.456|56=GATEWAY|37=ORD202601250001|17=EXEC_1706171420456|150=1|39=1|55=600519|54=1|32=50|151=50|14=50|6=123.45|11=CLORD202601250001|31=123.45|20=0|10=055|
```

### 字段说明

| Tag | 字段名 | 值 | 说明 |
|-----|--------|-----|------|
| 150 | ExecType | 1 (PARTIAL_FILL) | 执行类型：部分成交 |
| 39 | OrdStatus | 1 (PARTIALLY_FILLED) | 订单状态：部分成交 |
| 32 | LastQty | 50 | 最后成交数量 |
| 151 | LeavesQty | 50 | 剩余数量 |
| 14 | CumQty | 50 | 累计成交量 |
| 6 | AvgPx | 123.45 | 平均成交价 |
| 31 | LastPx | 123.45 | 最后成交价格 |

---

## 3. ExecutionReport - 完全成交 (FILL)

**场景**: 订单全部成交

**消息类型**: 8 (ExecutionReport)
**ExecType**: 2 (FILL)
**OrdStatus**: 2 (FILLED)

```
8=FIX.4.2|9=245|35=8|34=4|49=FIXSIMULATOR|52=20260125-10:30:25.789|56=GATEWAY|37=ORD202601250001|17=EXEC_1706171425789|150=2|39=2|55=600519|54=1|32=50|151=0|14=100|6=123.45|11=CLORD202601250001|31=123.45|20=0|10=112|
```

### 字段说明

| Tag | 字段名 | 值 | 说明 |
|-----|--------|-----|------|
| 150 | ExecType | 2 (FILL) | 执行类型：完全成交 |
| 39 | OrdStatus | 2 (FILLED) | 订单状态：完全成交 |
| 32 | LastQty | 50 | 最后成交数量 |
| 151 | LeavesQty | 0 | 剩余数量为0 |
| 14 | CumQty | 100 | 累计成交量等于委托量 |
| 6 | AvgPx | 123.45 | 平均成交价 |

---

## 4. ExecutionReport - 改单确认 (REPLACE)

**场景**: 订单修改成功

**消息类型**: 8 (ExecutionReport)
**ExecType**: 5 (REPLACE)
**OrdStatus**: 5 (REPLACED)

```
8=FIX.4.2|9=248|35=8|34=5|49=FIXSIMULATOR|52=20260125-10:31:00.123|56=GATEWAY|37=ORD202601250002|17=EXEC_1706171500123|150=5|39=5|55=600519|54=1|38=200|151=200|14=0|6=0|11=CLORD202601250002|41=CLORD202601250001|20=0|10=023|
```

### 字段说明

| Tag | 字段名 | 值 | 说明 |
|-----|--------|-----|------|
| 150 | ExecType | 5 (REPLACE) | 执行类型：改单确认 |
| 39 | OrdStatus | 5 (REPLACED) | 订单状态：已替换 |
| 38 | OrderQty | 200 | 修改后的委托数量 |
| 41 | OrigClOrdID | CLORD202601250001 | 原始客户端订单ID |

---

## 5. ExecutionReport - 撤单确认 (CANCEL)

**场景**: 订单撤销成功

**消息类型**: 8 (ExecutionReport)
**ExecType**: 4 (CANCEL)
**OrdStatus**: 4 (CANCELED)

```
8=FIX.4.2|9=248|35=8|34=6|49=FIXSIMULATOR|52=20260125-10:32:00.456|56=GATEWAY|37=ORD202601250003|17=EXEC_1706171600456|150=4|39=4|55=600519|54=1|38=150|151=0|14=50|6=0|11=CLORD202601250003|41=CLORD202601250002|20=0|10=089|
```

### 字段说明

| Tag | 字段名 | 值 | 说明 |
|-----|--------|-----|------|
| 150 | ExecType | 4 (CANCEL) | 执行类型：撤单确认 |
| 39 | OrdStatus | 4 (CANCELED) | 订单状态：已撤销 |
| 41 | OrigClOrdID | CLORD202601250002 | 原始客户端订单ID |
| 14 | CumQty | 50 | 撤单前的累计成交量 |
| 151 | LeavesQty | 0 | 剩余数量为0 |

---

## 6. ExecutionReport - 业务拒绝 (REJECTED)

**场景**: 订单被业务层面拒绝（如资金不足、风控拦截等）

**消息类型**: 8 (ExecutionReport)
**ExecType**: 8 (REJECTED)
**OrdStatus**: 8 (REJECTED)

```
8=FIX.4.2|9=256|35=8|34=7|49=FIXSIMULATOR|52=20260125-10:33:00.789|56=GATEWAY|37=ORD202601250004|17=EXEC_1706171700789|150=8|39=8|55=600519|54=1|38=500|151=0|14=0|6=0|11=CLORD202601250004|58=rejected by simulator, don't worry|20=0|10=167|
```

### 字段说明

| Tag | 字段名 | 值 | 说明 |
|-----|--------|-----|------|
| 150 | ExecType | 8 (REJECTED) | 执行类型：业务拒绝 |
| 39 | OrdStatus | 8 (REJECTED) | 订单状态：已拒绝 |
| 58 | Text | rejected by simulator... | 拒绝原因说明 |

---

## 7. OrderCancelReject - 撤单拒绝

**场景**: 撤单请求被拒绝（如订单已完成、不存在等）

**消息类型**: 9 (OrderCancelReject)

```
8=FIX.4.2|9=186|35=9|34=8|49=FIXSIMULATOR|52=20260125-10:34:00.123|56=GATEWAY|37=ORD202601250005|41=CLORD202601250005|11=CLORD202601250006|39=8|434=1|436=4|58=Order already filled|10=156|
```

### 字段说明

| Tag | 字段名 | 值 | 说明 |
|-----|--------|-----|------|
| 35 | MsgType | 9 | OrderCancelReject |
| 37 | OrderID | ORD202601250005 | 订单ID |
| 41 | OrigClOrdID | CLORD202601250005 | 原始客户端订单ID |
| 11 | ClOrdID | CLORD202601250006 | 撤单请求的客户端订单ID |
| 39 | OrdStatus | 8 (REJECTED) | 订单状态 |
| 434 | CxlRejResponseTo | 1 (ORDER_CANCEL_REQUEST) | 被拒绝的请求类型 |
| 436 | CxlRejReason | 4 (ALREADY_FILLED) | 拒绝原因：已成交 |
| 58 | Text | Order already filled | 拒绝说明 |

---

## 8. BusinessMessageReject - 业务拒绝

**场景**: 接收到的业务消息无法被处理

**消息类型**: j (BusinessMessageReject)

```
8=FIX.4.2|9=168|35=j|34=9|49=FIXSIMULATOR|52=20260125-10:35:00.456|56=GATEWAY|45=5|372=6|380=10|58=Invalid Symbol|373=55|10=234|
```

### 字段说明

| Tag | 字段名 | 值 | 说明 |
|-----|--------|-----|------|
| 35 | MsgType | j | BusinessMessageReject |
| 45 | RefSeqNum | 5 | 被拒绝消息的序列号 |
| 372 | RefMsgType | 6 (NEW_ORDER_SINGLE) | 被拒绝的消息类型 |
| 380 | BusinessRejectReason | 10 (OTHER) | 业务拒绝原因 |
| 58 | Text | Invalid Symbol | 拒绝说明 |
| 373 | BusinessRejectRefID | 55 | 被拒绝的字段标签 (Symbol) |

---

## 9. Session 管理消息

### 9.1 Logon - 登录请求 (INBOUND)

```
8=FIX.4.2|9=73|35=A|34=1|49=GATEWAY|52=20260125-09:00:00.000|56=FIXSIMULATOR|98=0|108=30|10=234|
```

### 9.2 Logon - 登录响应 (OUTBOUND)

```
8=FIX.4.2|9=73|35=A|34=1|49=FIXSIMULATOR|52=20260125-09:00:00.001|56=GATEWAY|98=0|108=30|10=189|
```

### 9.3 Heartbeat - 心跳

```
8=FIX.4.2|9=53|35=0|34=10|49=FIXSIMULATOR|52=20260125-10:00:00.000|56=GATEWAY|10=055|
```

### 9.4 Logout - 登出

```
8=FIX.4.2|9=53|35=5|34=20|49=FIXSIMULATOR|52=20260125-15:00:00.000|56=GATEWAY|58=Normal logout|10=234|
```

---

## 10. 自动回报策略示例 (基于 OrderQty)

根据项目 PRD V1.0.5 定义的自动回报策略：

### 10.1 委托数量 100 - 仅回报已报

```
8=FIX.4.2|9=236|35=8|34=100|49=FIXSIMULATOR|52=20260125-10:30:15.123|56=GATEWAY|37=ORD100|17=EXEC_100|150=0|39=0|55=600519|54=1|38=100|151=100|14=0|6=0|11=CLORD100|20=0|10=023|
```

### 10.2 委托数量 200 - 已报 + 部成100

**报文1 (已报)**:
```
8=FIX.4.2|9=236|35=8|34=200|49=FIXSIMULATOR|52=20260125-10:30:15.123|56=GATEWAY|37=ORD200|17=EXEC_200_1|150=0|39=0|55=600519|54=1|38=200|151=200|14=0|6=0|11=CLORD200|20=0|10=089|
```

**报文2 (部成100)**:
```
8=FIX.4.2|9=245|35=8|34=201|49=FIXSIMULATOR|52=20260125-10:30:20.456|56=GATEWAY|37=ORD200|17=EXEC_200_2|150=1|39=1|55=600519|54=1|32=100|151=100|14=100|6=123.45|11=CLORD200|31=123.45|20=0|10=067|
```

### 10.3 委托数量 300 - 已报 + 部成100 + 部成200 + 全成

**报文1 (已报)**:
```
8=FIX.4.2|9=236|35=8|34=300|49=FIXSIMULATOR|52=20260125-10:30:15.123|56=GATEWAY|37=ORD300|17=EXEC_300_1|150=0|39=0|55=600519|54=1|38=300|151=300|14=0|6=0|11=CLORD300|20=0|10=112|
```

**报文2 (部成100)**:
```
8=FIX.4.2|9=245|35=8|34=301|49=FIXSIMULATOR|52=20260125-10:30:20.456|56=GATEWAY|37=ORD300|17=EXEC_300_2|150=1|39=1|55=600519|54=1|32=100|151=200|14=100|6=123.45|11=CLORD300|31=123.45|20=0|10=078|
```

**报文3 (部成200)**:
```
8=FIX.4.2|9=245|35=8|34=302|49=FIXSIMULATOR|52=20260125-10:30:25.789|56=GATEWAY|37=ORD300|17=EXEC_300_3|150=1|39=1|55=600519|54=1|32=100|151=100|14=200|6=123.45|11=CLORD300|31=123.45|20=0|10=089|
```

**报文4 (全成)**:
```
8=FIX.4.2|9=245|35=8|34=303|49=FIXSIMULATOR|52=20260125-10:30:30.123|56=GATEWAY|37=ORD300|17=EXEC_300_4|150=2|39=2|55=600519|54=1|32=100|151=0|14=300|6=123.45|11=CLORD300|31=123.45|20=0|10=098|
```

### 10.4 委托数量 400 - 拒绝

```
8=FIX.4.2|9=256|35=8|34=400|49=FIXSIMULATOR|52=20260125-10:30:15.123|56=GATEWAY|37=ORD400|17=EXEC_400|150=8|39=8|55=600519|54=1|38=400|151=0|14=0|6=0|11=CLORD400|58=rejected by simulator, don't worry|20=0|10=234|
```

---

## 常用 ExecType 和 OrdStatus 对照表

| ExecType | OrdStatus | 说明 |
|----------|-----------|------|
| 0 (NEW) | 0 (NEW) | 新订单已报 |
| 1 (PARTIAL_FILL) | 1 (PARTIALLY_FILLED) | 部分成交 |
| 2 (FILL) | 2 (FILLED) | 完全成交 |
| 3 (DONE_FOR_DAY) | 3 (DONE_FOR_DAY) | 当日结束 |
| 4 (CANCEL) | 4 (CANCELED) | 撤单确认 |
| 5 (REPLACE) | 5 (REPLACED) | 改单确认 |
| 6 (PENDING_CANCEL) | 6 (PENDING_CANCEL) | 撤单挂起 |
| 7 (STOPPED) | 7 (STOPPED) | 止损订单已触发 |
| 8 (REJECTED) | 8 (REJECTED) | 业务拒绝 |
| 9 (SUSPENDED) | 9 (SUSPENDED) | 订单暂停 |
| A (PENDING_NEW) | A (PENDING_NEW) | 新订单挂起 |
| B (CALCULATED) | B (CALCULATED) | 计算中 |
| C (EXPIRED) | C (EXPIRED) | 订单已过期 |
| D (RESTATED) | D (RESTATED) | 订单重述 |
| E (PENDING_REPLACE) | E (PENDING_REPLACE) | 改单挂起 |

---

## 常用 Side 买卖方向

| 值 | 说明 |
|----|------|
| 1 | BUY (买入) |
| 2 | SELL (卖出) |
| 3 | BUY_MINUS (买入减价) |
| 4 | SELL_PLUS (卖出加价) |
| 5 | SELL_SHORT (卖空) |
| 6 | SELL_SHORT_EXEMPT (豁免卖空) |
| 7 | UNDISCLOSED (未披露) |
| 8 | CROSS (交叉) |
| 9 | CROSS_SHORT (交叉卖空) |

---

## 注意事项

1. **校验和 (Tag 10)**: 本文档中的校验和为示例值，实际发送时需要根据报文内容重新计算
2. **序列号 (Tag 34)**: 实际使用时需要根据会话状态维护正确的序列号
3. **时间戳 (Tag 52)**: 格式为 `YYYYMMDD-HH:MM:SS.sss`，需使用 UTC 或上海时区
4. **分隔符**: 本文档使用 `|` 作为分隔符，实际协议应使用 SOH (`\u0001`)
5. **BodyLength (Tag 9)**: 需要根据报文实际长度动态计算
