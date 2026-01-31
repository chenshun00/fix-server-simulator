# 手动回报功能设计文档

**日期**: 2026-01-31
**功能**: 支持用户手动回复各种 FIX 报文

---

## 一、功能概述

为 FIX Server Simulator 添加手动回报功能，允许用户对已接收的消息手动发送 ExecutionReport 回报。

### 核心需求

1. 在消息查询页面，对每条消息提供"手动回报"操作入口
2. 弹出表单对话框，支持编辑完整的 ExecutionReport 字段
3. 表单预填充原消息的关键字段
4. 根据选择的回报类型（ExecType）动态显示相关字段
5. 自动发送回原消息所属的会话

---

## 二、用户交互流程

```
┌─────────────────────────────────────────────────────────────┐
│  1. 用户在消息查询页面查看消息列表                           │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  2. 点击某条消息的"手动回报"按钮                            │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  3. 弹出手动回报对话框，表单自动预填充：                    │
│     - ClOrdID, Symbol, Side, OrderQty, Price               │
│     - ExecType/OrdStatus 默认为 NEW                        │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  4. 用户选择 ExecType，表单动态显示相关字段：               │
│     - PARTIAL_FILL: 显示 LastQty, LastPx                   │
│     - FILL: 显示 LastQty, LastPx, CumQty, AvgPx            │
│     - CANCEL: 显示 OrigClOrdID, Text                       │
│     - REPLACE: 显示 OrigClOrdID, 新 OrderQty, 新 Price     │
│     - REJECTED: 显示 Text                                  │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  5. 用户填写完整后点击"发送"                                │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  6. 系统发送回报，显示成功/失败提示                         │
└─────────────────────────────────────────────────────────────┘
```

---

## 三、表单字段设计

### 3.1 固定字段（所有类型必需）

| 字段 | Tag | 说明 | 预填充 |
|------|-----|------|--------|
| ClOrdID | 11 | 客户端订单ID | ✓ 原消息值 |
| Symbol | 55 | 股票代码 | ✓ 原消息值 |
| Side | 54 | 买卖方向 | ✓ 原消息值 |
| OrderQty | 38 | 委托数量 | ✓ 原消息值 |
| Price | 44 | 委托价格 | ✓ 原消息值（如有） |
| ExecType | 150 | 执行类型 | 下拉选择 |
| OrdStatus | 39 | 订单状态 | 联动 ExecType |

### 3.2 条件字段（根据 ExecType 动态显示）

| ExecType | OrdStatus | 条件字段 | 说明 |
|----------|-----------|----------|------|
| 0 (NEW) | 0 | 无 | 仅固定字段 |
| 1 (PARTIAL_FILL) | 1 | LastQty(32), LastPx(31) | 最后成交数量/价格 |
| 2 (FILL) | 2 | LastQty, LastPx, CumQty(14), AvgPx(6) | 完全成交 |
| 4 (CANCEL) | 4 | OrigClOrdID(41), Text(58) | 撤单确认 |
| 5 (REPLACE) | 5 | OrigClOrdID, 新 OrderQty, 新 Price | 改单确认 |
| 8 (REJECTED) | 8 | Text(58) | 业务拒绝 |

---

## 四、后端 API 设计

### 4.1 Controller

**ManualResponseController.java**
```java
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
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(ApiResponse.error("回报发送失败: " + e.getMessage()));
        }
    }
}
```

### 4.2 请求 DTO

**ManualResponseRequest.java**
```java
@Data
public class ManualResponseRequest {
    // 识别信息
    private String sessionId;

    // 固定字段
    private String clOrdId;
    private String symbol;
    private String side;
    private BigDecimal orderQty;
    private BigDecimal price;

    // 回报类型
    private String execType;
    private String ordStatus;

    // 条件字段
    private BigDecimal lastQty;
    private BigDecimal lastPx;
    private BigDecimal cumQty;
    private BigDecimal avgPx;
    private String origClOrdId;
    private String text;
}
```

### 4.3 响应 DTO

**ApiResponse.java**
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
```

---

## 五、Service 层设计

### 5.1 ManualResponseService

```java
@Service
@RequiredArgsConstructor
public class ManualResponseService {

    private final SessionRepository sessionRepository;
    private final ExecutionReportFactory reportFactory;

    public void sendManualResponse(ManualResponseRequest request) {
        // 1. 查找 Session
        Session session = sessionRepository.findBySessionId(request.getSessionId())
            .orElseThrow(() -> new IllegalArgumentException("会话不存在: " + request.getSessionId()));

        // 2. 构建回报报文
        Message report = reportFactory.createExecutionReport(request);

        // 3. 获取 QuickFIX/J SessionID 并发送
        SessionID qfSessionId = toQuickFixSessionId(session);
        Session.sendToTarget(report, qfSessionId);
    }

    private SessionID toQuickFixSessionId(Session session) {
        return new SessionID(
            "FIX.4.2",
            session.getSenderCompId(),
            session.getTargetCompId()
        );
    }
}
```

### 5.2 ExecutionReportFactory

```java
@Component
public class ExecutionReportFactory {

    public Message createExecutionReport(ManualResponseRequest request) {
        String orderId = "ORD_" + System.currentTimeMillis();
        String execId = "EXEC_" + System.currentTimeMillis();

        ExecutionReport report = new ExecutionReport(
            new OrderID(orderId),
            new ExecID(execId),
            new ExecType(request.getExecType().charAt(0)),
            new OrdStatus(request.getOrdStatus().charAt(0)),
            new Side(request.getSide().charAt(0)),
            new LeavesQty(request.getOrderQty().doubleValue()),
            new CumQty(getCumQty(request).doubleValue()),
            new AvgPx(getAvgPx(request).doubleValue())
        );

        // 设置基本字段
        report.set(new ClOrdID(request.getClOrdId()));
        report.set(new Symbol(request.getSymbol()));
        report.set(new OrderQty(request.getOrderQty().doubleValue()));
        report.setField(new TransactTime());
        report.setField(new SendingTime());

        // 根据条件字段设置额外内容
        addConditionalFields(report, request);

        return report;
    }

    private void addConditionalFields(ExecutionReport report, ManualResponseRequest request) {
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
                break;

            case "8": // REJECTED
                if (request.getText() != null) {
                    report.set(new Text(request.getText()));
                }
                break;
        }
    }

    private BigDecimal getCumQty(ManualResponseRequest request) {
        if ("2".equals(request.getExecType())) {
            return request.getOrderQty(); // FILL 时累计成交量等于委托量
        }
        if ("1".equals(request.getExecType()) && request.getLastQty() != null) {
            return request.getLastQty(); // PARTIAL_FILL 时等于最后成交量
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal getAvgPx(ManualResponseRequest request) {
        if (request.getLastPx() != null) {
            return request.getLastPx();
        }
        return BigDecimal.ZERO;
    }
}
```

---

## 六、前端设计

### 6.1 组件结构

```
frontend/src/
├── components/
│   └── ResponseDialog.vue          # 手动回报弹窗
├── api/
│   └── response.ts                 # 回报相关 API
└── stores/
    └── response.ts                 # 回报状态管理
```

### 6.2 ResponseDialog.vue

关键功能：
- 接收 props: `visible`, `message` (原消息数据)
- 表单字段绑定到 `responseForm`
- `execType` 变化时触发 `handleExecTypeChange` 动态显示字段
- 调用 `sendManualResponse` API
- 发送完成后 emit `success` 事件

### 6.3 修改 Messages.vue

在表格中添加操作列：

```vue
<el-table-column label="操作" width="100" fixed="right">
  <template #default="{ row }">
    <el-button
      type="primary"
      size="small"
      @click="openResponseDialog(row)"
    >
      手动回报
    </el-button>
  </template>
</el-table-column>
```

---

## 七、实现任务列表

1. ✅ 创建 ManualResponseRequest DTO
2. ✅ 创建 ApiResponse DTO
3. ✅ 创建 ManualResponseController
4. ✅ 创建 ManualResponseService
5. ✅ 创建 ExecutionReportFactory
6. ✅ 前端创建 ResponseDialog.vue 组件
7. ✅ 前端创建 response.ts API
8. ✅ 修改 Messages.vue 添加操作列
9. ✅ 集成测试

---

## 八、验收标准

- [ ] 消息列表显示"手动回报"按钮
- [ ] 点击按钮弹出对话框，表单预填充正确
- [ ] 选择不同 ExecType 时，条件字段正确显示/隐藏
- [ ] 点击发送，后端正确构建并发送回报报文
- [ ] 发送成功后显示成功提示
- [ ] 发送失败显示错误信息
