# FIX Server Simulator - 产品需求文档

> **版本状态：（作为后续架构设计与实现的唯一输入）**
>
> 本 PRD 明确限定系统边界、技术选型与实现原则，用于指导 FIX Server Simulator 的完整研发，不再引入功能性扩展。

---

## 1. 项目概述

### 1.1 产品名称

FIX Server Simulator（带 Web 控制台）

### 1.2 产品定位

FIX Server Simulator 是一个 **FIX Acceptor（服务端）模拟系统**，
用于本地开发、测试与联调场景：

* 被动接受来自 FIX Gateway 的连接与报文
* 稳定、完整地记录 FIX 报文
* 支持人工与规则化方式触发模拟回报
* 通过 Web 控制台进行可视化查看与操作

该系统 **不用于生产交易**，仅作为工程与测试工具。

### 1.3 目标用户

* 后端开发人员
* 测试工程师
* 交易系统集成人员

### 1.4 使用场景

* 本地调试 FIX Gateway 行为
* 模拟对手方 FIX Server
* 分析和验证 FIX 报文交互

---

## 2. 产品目标

### 2.1 核心目标

* 稳定建立并维护 FIX Session
* 接收并 **完整落库** 所有 FIX 报文
* 支持人工与规则化的模拟响应
* 提供清晰的 Web 操作界面

### 2.2 非目标（明确不做）

* 不实现真实撮合逻辑
* 不承担任何生产交易职责
* 不构建完整 OMS / 风控系统

---

## 3. 用户与使用流程

### 3.1 使用角色

* **开发人员**：调试 FIX Gateway、验证协议行为
* **测试人员**：验证异常场景、回报逻辑与稳定性

### 3.2 核心使用流程（端到端）

1. FIX Gateway 主动发起 Session（Logon）
2. Simulator 作为 Acceptor 建立 Session
3. Gateway 发送 Session / 业务报文
4. Simulator 接收报文并落库（原始 + 解析）
5. Web 控制台通过http查询并且展示解析后的报文
6. 通过 Web 触发模拟回报（或按规则自动回报）

---

## 4. 功能需求

### 4.1 FIX Session 管理

#### 4.1.1 Session 生命周期

* 支持来自 Gateway 的 TCP 连接
* 支持 Logon / Logout
* Session 状态可查询（连接中 / 已断开 / 异常）
* heartbeat 30s

> 约束：
>
> * Web 仅支持查询Session状态
> * Session 生命周期严格遵循 FIX 协议

#### 4.1.2 SeqNum 管理

* 自动维护 InSeq / OutSeq
* 支持 ResendRequest / GapFill
* SeqNum 行为由 FIX Engine 统一管理

#### 4.1.3 心跳管理

* 自动发送 Heartbeat
* 正确响应 TestRequest
* Session 超时检测

> 约束：
>
> * 心跳与 SeqNum 行为 **不受模拟规则影响**

---

### 4.2 报文接收与存储

#### 4.2.1 原始 FIX 报文存储（唯一事实源）

* 完整保存接收到的原始 FIX 报文字符串
* 记录：

  * 接收时间
  * Session 标识
  * MsgType

> 设计原则：
> **原始 FIX 报文是系统的唯一事实源（Source of Truth）**

#### 4.2.2 解析后字段存储（派生视图）

* 将 FIX 报文解析为结构化数据
* 存储常用业务字段（MsgType、ClOrdID、OrigClOrdId、Symbol 、OrderQty、Price、Side、SenderCompId，TargetCompId）
* 解析结果仅作为查询与展示使用

#### 4.2.3 查询能力

* 支持分页
* 按  ClOrdId 查询, ClOrdID 查询为精确匹配，区分大小写，基于原始 FIX 报文字段。
* 按 Symbol 查询
* 这一期只需要支持按ClOrdID和Symbol查询
* V1.0.1: ClOrdId和Symbol查询条件变为可选，如果不填则查询最近一天的数据

---

### 4.3 Web 控制台

#### 4.3.1 Session 列表页

* Session 状态,Session 唯一标识由 BeginString + SenderCompID + TargetCompID 组成。

#### 4.3.2 报文列表页

* 报文列表展示（分页）
* 支持按 ClOrdId，Symbol 查询
* 支持HTTP刷新（非强实时）, Web 与后端通过 REST API 交互，不要求实时推送能力。

#### 4.3.3 报文详情页

* 原始 FIX 报文展示
* 解析字段展示
* 报文关联信息（基于 ClOrdID）

#### 4.3.4 人工触发响应（核心约束）

* 人工回报 **必须基于已接收的报文生成**
  * 已报(New)
  * 部分成交： lastQty，CumQty，leaveQty，ExecType，ordStatus需要web输入，如果用户未输入，http接口拒绝并提示参数xx不完整
  * 完全成交： lastQty，CumQty，leaveQty，ExecType，ordStatus需要web输入，如果用户未输入，http接口拒绝并提示参数xx不完整
  * 改单、撤单拒绝：ordStatus，ExecType需要web输入，如果用户未输入，http接口拒绝并提示参数xx不完整
  * 改单、撤单确认：ordStatus，ExecType需要web输入，如果用户未输入，http接口拒绝并提示参数xx不完整
* lastQty、CumQty、leaveQty之间的数据中正确性由用户保证，系统不进行校验
* Web 不提供完全自由的 FIX 报文编辑能力
* 可选择回报类型：

  * ExecutionReport
  * Reject

---

### 4.4 模拟行为控制

#### 4.4.1 支持的模拟行为

* 已报（New报文）
* 成功回报（部分成交 / 完全成交）
* 拒单回报（业务拒绝）
* 不回报（模拟业务异常）

#### 4.4.2 行为边界

* 模拟规则 **仅影响业务回报**
* 不影响 Session 心跳、SeqNum、连接状态

---

## 5. 非功能性需求

### 5.1 可观测性

* 分级日志（DEBUG / INFO / WARN / ERROR）
* Session 与报文日志清晰可追踪

### 5.2 可追溯性

* 不需要

### 5.3 可扩展性

* 规则配置可扩展, 支持后期按照OrderQty进行回报，该能力不要求在本期实现，仅作为未来扩展方向。

### 5.4 可测试性

* 支持单元测试
* 支持集成测试（Gateway ↔ Simulator）

---

## 6. 技术架构约束（已冻结）

### 6.1 技术栈

* **后端**：Java + Spring Boot
* **FIX Engine**：QuickFIX/J（Acceptor 模式）, 仅支持 FIX Acceptor 模式，不支持 Initiator 模式。
  * 上海时区，支持从01:00到23:00进行连接，心跳30s
* **数据库**：MySQL
* **协议版本**：FIX 4.2
* **Web**：简单 Web UI（不追求前端复杂度）
* **性能**：不需要考虑性能问题

### 6.2 并发与部署假设

* 单 Gateway
* Session 数量 ≤ 10
* 单实例运行
* 不考虑高可用

---

## 7. 验收标准

* Gateway 能成功建立 FIX Session
* 所有接收的报文均可落库并展示
* 人工触发的回报可被 Gateway 正确接收
* Session 管理行为符合 FIX 协议
* 模拟行为（成功 / 拒绝 / 延迟 / 不回报）可用
* 模拟行为仅作用于业务类报文（如 NewOrderSingle），不影响 Session 报文。

---

## 8. 开发阶段划分（执行顺序固定）

### Phase 1：FIX Server 基础能力

* FIX Acceptor 可连接
* 原始报文落库

### Phase 2：Web 只读展示

* Session / 报文查询

### Phase 3：人工回报能力

* 基于报文生成回报
* 人工回报时，如果session不存在，那么系统需要提示session已断开链接
* 回报报文必须通过原 Session 发送，Sender/Target 方向反转

### Phase 4：规则化模拟

* 延迟 / 异常模拟

---

> **本 PRD 冻结后，后续设计与实现不得引入超出本文档定义范围的功能。**

## 版本 1.0.1 更新内容

### 需求背景
目前查询报文时, ClOrdId或者Symbol是必填的，调整为非必填
- 不填，查询最近一天的数据
- 填了，查询对应的数据
回报时不再需要输入sessionKey，数据本身包含了，应该由数据推导出来

### 核心变更

#### 4.2.3 查询能力 (V1.0.1更新)
* 支持分页
* 按  ClOrdId 查询, ClOrdID 查询为精确匹配，区分大小写，基于原始 FIX 报文字段。
* 按 Symbol 查询
* 这一期只需要支持按ClOrdID和Symbol查询
* V1.0.1: ClOrdId和Symbol查询条件变为可选，如果不填则查询最近一天的数据

#### 4.3.4 人工触发响应 (V1.0.1更新)
* 人工回报 **必须基于已接收的报文生成**
  * 已报(New)
  * 部分成交： lastQty，CumQty，leaveQty，ExecType，ordStatus需要web输入，如果用户未输入，http接口拒绝并提示参数xx不完整
  * 完全成交： lastQty，CumQty，leaveQty，ExecType，ordStatus需要web输入，如果用户未输入，http接口拒绝并提示参数xx不完整
  * 改单、撤单拒绝：ordStatus，ExecType需要web输入，如果用户未输入，http接口拒绝并提示参数xx不完整
  * 改单、撤单确认：ordStatus，ExecType需要web输入，如果用户未输入，http接口拒绝并提示参数xx不完整
* lastQty、CumQty、leaveQty之间的数据中正确性由用户保证，系统不进行校验
* Web 不提供完全自由的 FIX 报文编辑能力
* 可选择回报类型：
  * ExecutionReport
  * Reject
* V1.0.1: 回报时不再需要输入sessionKey，系统从报文数据中推导出sessionKey

## 重点
* 暂时不进入代码开发
* 是否还存在有歧义的地方，如果有需要明确指出
* 如果没有任何歧义，开始进入数据库和实体设计

