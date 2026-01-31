<template>
  <div class="messages-page">
    <div class="page-header">
      <div class="header-title">
        <h1 class="page-title">
          <span class="title-icon">◇</span>
          消息查询
        </h1>
        <p class="page-subtitle">FIX 协议消息记录与检索</p>
      </div>
    </div>

    <!-- Search Panel -->
    <div class="search-panel">
      <form @submit.prevent="handleSearch" class="search-form">
        <div class="search-row">
          <div class="search-field">
            <label class="field-label">股票代码</label>
            <input
              v-model="searchForm.symbol"
              type="text"
              placeholder="如: 600519"
              class="terminal-input"
              clearable
            />
          </div>
          <div class="search-field">
            <label class="field-label">ClOrdID</label>
            <input
              v-model="searchForm.clOrdId"
              type="text"
              placeholder="客户订单ID"
              class="terminal-input"
              clearable
            />
          </div>
          <div class="search-actions">
            <button type="submit" class="terminal-btn primary">
              <span class="btn-icon">⏎</span>
              <span>搜索</span>
            </button>
            <button type="button" @click="handleReset" class="terminal-btn">
              <span class="btn-icon">↺</span>
              <span>重置</span>
            </button>
          </div>
        </div>
      </form>
    </div>

    <!-- Messages Table -->
    <div class="messages-table-container">
      <div class="table-header">
        <span class="table-title">消息列表</span>
        <span class="table-count">{{ messageStore.total }} 条记录</span>
      </div>

      <div class="table-wrapper">
        <table class="terminal-table">
          <thead>
            <tr>
              <th>类型</th>
              <th>股票</th>
              <th>ClOrdID</th>
              <th>OrigClOrdID</th>
              <th>价格</th>
              <th>数量</th>
              <th>方向</th>
              <th>类型</th>
              <th>时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="msg in messageStore.messages" :key="msg.id" class="table-row">
              <td>
                <span class="msg-type" :class="`type-${msg.msgType}`">
                  {{ msg.msgType }}
                </span>
              </td>
              <td class="mono">{{ msg.symbol }}</td>
              <td class="mono text-truncate">{{ msg.clOrdId || '-' }}</td>
              <td class="mono text-truncate">{{ msg.origClOrdId || '-' }}</td>
              <td class="mono">{{ msg.price || '-' }}</td>
              <td class="mono">{{ msg.orderQty || '-' }}</td>
              <td>
                <span class="side-badge" :class="msg.side?.toLowerCase()">
                  {{ msg.side || '-' }}
                </span>
              </td>
              <td>{{ msg.ordType || '-' }}</td>
              <td class="timestamp">{{ formatTime(msg.receivedAt) }}</td>
              <td>
                <button
                  @click="openResponseDialog(msg)"
                  class="action-btn"
                  title="手动回报"
                >
                  回报
                </button>
              </td>
            </tr>
            <tr v-if="messageStore.messages.length === 0">
              <td colspan="10" class="empty-cell">
                <div class="table-empty">
                  <span class="empty-icon">◇</span>
                  <p>暂无消息记录</p>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="table-footer">
        <div class="pagination">
          <button
            @click="goToPage(pagination.page - 1)"
            :disabled="pagination.page === 0"
            class="page-btn"
          >
            ←
          </button>
          <span class="page-info">第 {{ pagination.page + 1 }} 页</span>
          <button
            @click="goToPage(pagination.page + 1)"
            :disabled="!hasNextPage"
            class="page-btn"
          >
            →
          </button>
        </div>
      </div>
    </div>

    <!-- Response Dialog -->
    <ResponseDialog
      v-model:visible="dialogVisible"
      :message="selectedMessage"
      @success="handleResponseSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { useMessageStore } from '../stores/message'
import { type Message as MessageType } from '../stores/message'
import ResponseDialog from '../components/ResponseDialog.vue'

const messageStore = useMessageStore()
const searchForm = reactive({ symbol: '', clOrdId: '' })
const pagination = reactive({ page: 0, size: 20 })

const dialogVisible = ref(false)
const selectedMessage = ref<MessageType | null>(null)

const hasNextPage = computed(() =>
  (pagination.page + 1) * pagination.size < messageStore.total
)

function handleSearch() {
  messageStore.searchMessages({
    symbol: searchForm.symbol || undefined,
    clOrdId: searchForm.clOrdId || undefined,
    page: pagination.page,
    size: pagination.size
  })
}

function handleReset() {
  searchForm.symbol = ''
  searchForm.clOrdId = ''
  pagination.page = 0
  handleSearch()
}

function goToPage(page: number) {
  if (page < 0) return
  pagination.page = page
  handleSearch()
}

function formatTime(dateStr: string) {
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  })
}

function openResponseDialog(msg: MessageType) {
  selectedMessage.value = msg
  dialogVisible.value = true
}

function handleResponseSuccess() {
  // 可以在这里添加刷新逻辑
}

handleSearch()
</script>

<style scoped>
.messages-page {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 2rem;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 1.75rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
  letter-spacing: -0.02em;
}

.title-icon {
  color: var(--accent-blue);
  font-size: 1.5rem;
}

.page-subtitle {
  color: var(--text-secondary);
  font-size: 0.875rem;
}

/* Search Panel */
.search-panel {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
}

.search-row {
  display: flex;
  gap: 1rem;
  align-items: flex-end;
}

.search-field {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.field-label {
  font-size: 0.7rem;
  color: var(--text-muted);
  font-weight: 500;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.terminal-input {
  width: 100%;
  padding: 0.75rem 1rem;
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: 4px;
  color: var(--text-primary);
  font-family: var(--font-mono);
  font-size: 0.875rem;
  transition: all 0.2s ease;
}

.terminal-input:focus {
  outline: none;
  border-color: var(--accent-blue);
  box-shadow: 0 0 0 3px var(--accent-blue-dim);
}

.terminal-input::placeholder {
  color: var(--text-muted);
}

.search-actions {
  display: flex;
  gap: 0.5rem;
}

.terminal-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.25rem;
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  border-radius: 4px;
  color: var(--text-primary);
  font-family: var(--font-mono);
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.terminal-btn:hover {
  border-color: var(--text-muted);
  background: var(--bg-primary);
}

.terminal-btn.primary {
  background: var(--accent-blue-dim);
  border-color: var(--accent-blue);
  color: var(--accent-blue);
}

.terminal-btn.primary:hover {
  background: rgba(59, 130, 246, 0.2);
  box-shadow: 0 0 20px rgba(59, 130, 246, 0.25);
}

.terminal-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.btn-icon {
  font-size: 1rem;
}

/* Table Container */
.messages-table-container {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  overflow: hidden;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  border-bottom: 1px solid var(--border-color);
  background: var(--bg-tertiary);
}

.table-title {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-primary);
}

.table-count {
  font-size: 0.75rem;
  color: var(--text-muted);
  font-family: var(--font-mono);
}

.table-wrapper {
  overflow-x: auto;
}

.terminal-table {
  width: 100%;
  border-collapse: collapse;
}

.terminal-table thead {
  background: var(--bg-tertiary);
}

.terminal-table th {
  padding: 0.875rem 1rem;
  text-align: left;
  font-size: 0.7rem;
  font-weight: 600;
  color: var(--text-muted);
  letter-spacing: 0.05em;
  text-transform: uppercase;
  border-bottom: 1px solid var(--border-color);
}

.terminal-table td {
  padding: 0.875rem 1rem;
  font-size: 0.875rem;
  border-bottom: 1px solid var(--border-color);
}

.table-row {
  transition: background 0.2s ease;
}

.table-row:hover {
  background: var(--bg-tertiary);
}

.table-row:last-child td {
  border-bottom: none;
}

.mono {
  font-family: var(--font-mono);
  font-size: 0.8rem;
}

.text-truncate {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.msg-type {
  display: inline-block;
  padding: 0.25rem 0.5rem;
  font-family: var(--font-mono);
  font-size: 0.75rem;
  font-weight: 600;
  border-radius: 3px;
}

.msg-type.type-D {
  background: rgba(59, 130, 246, 0.1);
  color: var(--accent-blue);
  border: 1px solid rgba(59, 130, 246, 0.3);
}

.msg-type.type-F {
  background: rgba(255, 165, 2, 0.1);
  color: var(--accent-amber);
  border: 1px solid rgba(255, 165, 2, 0.3);
}

.msg-type.type-G {
  background: rgba(16, 185, 129, 0.1);
  color: var(--accent-green);
  border: 1px solid rgba(16, 185, 129, 0.3);
}

.side-badge {
  display: inline-block;
  padding: 0.25rem 0.5rem;
  font-size: 0.75rem;
  font-weight: 600;
  border-radius: 3px;
  text-transform: uppercase;
}

.side-badge.buy {
  background: rgba(231, 76, 60, 0.1);
  color: #e74c3c;
  border: 1px solid rgba(231, 76, 60, 0.3);
}

.side-badge.sell {
  background: rgba(16, 185, 129, 0.1);
  color: var(--accent-green);
  border: 1px solid rgba(16, 185, 129, 0.3);
}

.timestamp {
  color: var(--text-muted);
  font-size: 0.75rem;
  font-family: var(--font-mono);
}

.action-btn {
  padding: 0.375rem 0.75rem;
  background: var(--accent-blue-dim);
  border: 1px solid var(--accent-blue);
  border-radius: 4px;
  color: var(--accent-blue);
  font-size: 0.75rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: var(--font-mono);
}

.action-btn:hover {
  background: rgba(59, 130, 246, 0.2);
  box-shadow: 0 0 10px rgba(59, 130, 246, 0.2);
}

.empty-cell {
  padding: 0 !important;
}

.table-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem;
}

.empty-icon {
  font-size: 2rem;
  color: var(--text-muted);
  margin-bottom: 0.5rem;
  opacity: 0.5;
}

.table-empty p {
  color: var(--text-secondary);
  font-size: 0.875rem;
}

/* Footer & Pagination */
.table-footer {
  display: flex;
  justify-content: center;
  padding: 1rem;
  border-top: 1px solid var(--border-color);
  background: var(--bg-tertiary);
}

.pagination {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.page-btn {
  padding: 0.5rem 0.75rem;
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: 4px;
  color: var(--text-primary);
  font-family: var(--font-mono);
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.page-btn:hover:not(:disabled) {
  border-color: var(--accent-blue);
  color: var(--accent-blue);
}

.page-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.page-info {
  font-size: 0.875rem;
  color: var(--text-secondary);
}
</style>
