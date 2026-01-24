# FIX Server Simulator - MVP Frontend Design

## Overview
Single Page Application (SPA) using AngularJS for FIX Server Simulator

## Architecture
```
┌─────────────────────────────────────────┐
│              Header Bar                 │
├─────────────┬───────────────────────────┤
│             │                           │
│   Sidebar   │                           │
│             │                           │
│  ┌─────────┐│        Main Content       │
│  │Sessions ││         Area              │
│  └─────────┘│                           │
│             │                           │
│  ┌─────────┐│                           │
│  │Messages ││                           │
│  └─────────┘│                           │
│             │                           │
└─────────────┴───────────────────────────┘
```

## Components

### 1. Sidebar Component
```
[ SESSION STATUS ]
[ MESSAGE QUERY  ]
```

### 2. Session Status View
```
┌─────────────────────────────────────────┐
│            Session Status               │
├─────────────────────────────────────────┤
│ Session ID: FIX.4.2|FIXSIMULATOR|GATEWAY│
│ Status: Connected                       │
│ Next Expected Seq: 1                    │
│ Next Sender Seq: 1                      │
├─────────────────────────────────────────┤
│ Session ID: FIX.4.2|ANOTHER|GATEWAY     │
│ Status: Disconnected                    │
│ Next Expected Seq: 5                    │
│ Next Sender Seq: 3                      │
└─────────────────────────────────────────┘
```

### 3. Message Query View
```
┌─────────────────────────────────────────┐
│            Message Query                │
├─────────────────────────────────────────┤
│ ClOrdId: [___________] Symbol: [______] │
│ [Search] [Clear]                        │
├─────────────────────────────────────────┤
│ Message Table                          │
│ ┌─────┬─────────┬─────────┬──────────┐ │
│ │ID   │ClOrdId  │Symbol   │Actions   │ │
│ ├─────┼─────────┼─────────┼──────────┤ │
│ │1001 │ORDER001 │AAPL     │[Reply]   │ │
│ │1002 │ORDER002 │GOOGL    │[Reply]   │ │
│ └─────┴─────────┴─────────┴──────────┘ │
│ [Prev] Page 1 of 10 [Next]             │
└─────────────────────────────────────────┘
```

### 4. Reply Modal
```
┌─────────────────────────────────────────┐
│              Reply Action               │
├─────────────────────────────────────────┤
│ Reply Type: [ExecutionReport ▼]         │
│ ExecType: [NEW ▼]                       │
│ OrdStatus: [NEW ▼]                      │
│ LastQty: [___________]                  │
│ CumQty: [____________]                  │
│ LeavesQty: [_________]                  │
│ [Send Reply] [Cancel]                   │
└─────────────────────────────────────────┘
```

## AngularJS Structure

### App Module
```
app/
├── app.module.js
├── app.config.js
├── components/
│   ├── sidebar/
│   │   ├── sidebar.component.js
│   │   ├── sidebar.template.html
│   │   └── sidebar.controller.js
│   ├── session-status/
│   │   ├── session-status.component.js
│   │   ├── session-status.template.html
│   │   └── session-status.controller.js
│   ├── message-query/
│   │   ├── message-query.component.js
│   │   ├── message-query.template.html
│   │   └── message-query.controller.js
│   └── reply-modal/
│       ├── reply-modal.component.js
│       ├── reply-modal.template.html
│       └── reply-modal.controller.js
├── services/
│   ├── session.service.js
│   ├── message.service.js
│   └── reply.service.js
└── index.html
```

## API Integration Points

### Session Service
- GET `/api/sessions` - 获取所有session状态

### Message Service  
- GET `/api/messages/search?clOrdId={}&symbol={}&page={}&size={}` - 查询消息
- GET `/api/messages/clordid?clOrdId={}&page={}&size={}` - 按ClOrdId查询
- GET `/api/messages/symbol?symbol={}&page={}&size={}` - 按Symbol查询

### Reply Service
- POST `/api/response/execution-report/partial-fill` - 部分成交回报
- POST `/api/response/execution-report/fill` - 完全成交回报
- POST `/api/response/execution-report/new` - 新订单回报
- POST `/api/response/reject` - 拒绝回报
- POST `/api/response/execution-report/modify` - 修改确认回报
- POST `/api/response/execution-report/cancel` - 撤销确认回报

## Bootstrap Styling Classes
- Use Bootstrap 4/5 grid system
- Responsive layout
- Card components for content sections
- Modal components for reply actions
- Table components for message listings
- Button groups for actions

## MVP Features Priority
1. Basic SPA structure with sidebar navigation
2. Session status view
3. Message query view with search
4. Reply functionality from message query view
5. Pagination support
6. Responsive design