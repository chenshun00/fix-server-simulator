# FIX Server Simulator - Task Breakdown

## Task 1: Database Schema Design

### Objective
设计 FIX Server Simulator 的数据库结构，用于持久化 FIX 报文与 Session 信息。

### Inputs (Frozen)
- PRD: PRD.md
- FIX Engine: QuickFIX/J
- Database: MySQL

### Scope (Must Do)
- 设计表结构（DDL）
- 明确定义：
  - 解析字段表（派生视图）
  - Session 表（如需要)
- 定义主键、外键（如有）
- 明确哪些字段是事实源，哪些是派生数据

### Out of Scope (Must NOT Do)
- 不设计 ORM Entity
- 不考虑性能优化索引（除非显式要求）
- 不引入 PRD 未定义的新业务字段
- 不做表拆分优化

### Design Principles
- 原始 FIX 报文是唯一事实源
- 解析字段仅作为查询与展示用途
- 数据模型必须支持：
  - 按 Symbol 查询
  - 按 ClOrdID 查询
  - 按时间查询

### Deliverables
- design-db.md
  - 表结构说明
  - 字段含义
  - 表之间关系说明

### Result 
- 是否存在任何有歧义的地方，如果存在，需要在开始设计DDL之前表述清楚，并让我裁决
