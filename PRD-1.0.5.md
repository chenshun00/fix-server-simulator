# FIX Server Simulator - V1.0.5 产品需求文档

## 一、需求背景

目前系统只能手动进行回报，需要支持自动回报功能，以提高模拟器的自动化程度和实用性。

## 二、产品目标

### 【核心目标】

根据委托数量进行自动回报处理：

- 委托数量100：回复已报（NEW）报文
- 委托数量200：回复已报（NEW）报文 + 部成100股报文
- 委托数量300：回复已报（NEW）报文 + 部成100股报文 + 部成200股报文 + 全成300股报文
- 委托数量400：回复拒绝报文（ExecType=8, OrdStatus=8），拒绝原因：rejected by simulator, don't worry
- 其他委托数量：不需要自动回报

### 【详细说明】

- NEW报文：ExecType(150)=0, OrdStatus(39)=0
- 对于委托数量200的情况：先回复NEW报文，再回复部成报文，LastShares=100股，累计成交100股，剩余成交100股
- 委托数量300：每次成交都是100股，分别回复1个NEW报文，2个部成报文，一个全成报文，最终累计成交300，剩余成交0
- 自动回报只对New Order单据进行自动回报
- 委托数量400会触发拒绝，业务描述，不需要依据

### 【非目标】

- 禁止调整前端界面

## 三、验收标准

- 支持按数量的自动回报，参考核心目标
- 系统能够自动识别New Order单据并根据委托数量生成相应的回报报文
- 不同委托数量对应正确的回报序列

## 四、技术约束

- 新增方法进行实现
- 保持现有API接口不变
- 不影响现有的手动回报功能

## 五、功能规格

### 5.1 自动回报触发条件

- 仅对New Order单据（Order Single消息）进行自动回报处理
- 根据OrderQty(38)字段的值来决定回报策略

### 5.2 回报策略

| 委托数量 | 回报序列 |
|---------|----------|
| 100 | 1个NEW报文 (ExecType=0, OrdStatus=0) |
| 200 | 1个NEW报文 (ExecType=0, OrdStatus=0) + 1个部成报文 (ExecType=1, OrdStatus=1, LastShares=100, CumQty=100, LeavesQty=100) |
| 300 | 1个NEW报文 (ExecType=0, OrdStatus=0) + 2个部成报文 (ExecType=1, OrdStatus=1) + 1个全成报文 (ExecType=2, OrdStatus=2) |
| 400 | 1个拒绝报文 (ExecType=8, OrdStatus=8, Text="rejected by simulator, don't worry") |
| 其他 | 无自动回报 |

### 5.3 报文格式规范

#### NEW报文 (已报)
```
ExecType(150) = 0
OrdStatus(39) = 0
```

#### PARTIAL_FILL报文 (部成)
```
ExecType(150) = 1
OrdStatus(39) = 1
LastShares(32) = 100 (固定值)
CumQty(14) = 累计成交量
LeavesQty(151) = 剩余未成交量
```

#### FULL_FILL报文 (全成)
```
ExecType(150) = 2
OrdStatus(39) = 2
CumQty(14) = 总委托量
LeavesQty(151) = 0
```

#### REJECTED报文 (拒绝)
```
ExecType(150) = 8
OrdStatus(39) = 8
Text(58) = "rejected by simulator, don't worry"
```

## 六、风险评估

- 可能与现有的手动回报功能产生冲突，需要确保两种模式可以共存
- 需要确保自动回报不会影响系统的性能