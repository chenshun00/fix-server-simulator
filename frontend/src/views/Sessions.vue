<template>
  <div class="sessions-page">
    <div class="page-header">
      <div class="header-title">
        <h1 class="page-title">
          <span class="title-icon">⬡</span>
          会话监控
        </h1>
        <p class="page-subtitle">实时追踪 FIX 会话连接状态</p>
      </div>
      <div class="header-stats">
        <div class="stat-card">
          <span class="stat-label">活跃会话</span>
          <span class="stat-value">{{ activeCount }}</span>
        </div>
        <div class="stat-card">
          <span class="stat-label">总会话</span>
          <span class="stat-value">{{ sessionStore.sessions.length }}</span>
        </div>
      </div>
    </div>

    <div class="sessions-grid">
      <div
        v-for="session in sessionStore.sessions"
        :key="session.sessionId"
        class="session-card"
        :class="{ 'status-connected': session.status === 'CONNECTED' }"
      >
        <div class="card-header">
          <div class="session-id">
            <span class="id-label">SESSION_ID</span>
            <span class="id-value">{{ session.sessionId }}</span>
          </div>
          <div class="status-badge" :class="`status-${session.status.toLowerCase()}`">
            <span class="status-dot"></span>
            <span>{{ session.status }}</span>
          </div>
        </div>

        <div class="card-body">
          <div class="info-row">
            <span class="info-label">SENDER</span>
            <span class="info-value">{{ session.senderCompId }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">TARGET</span>
            <span class="info-value">{{ session.targetCompId }}</span>
          </div>
          <div class="info-row" v-if="session.port">
            <span class="info-label">PORT</span>
            <span class="info-value">{{ session.port }}</span>
          </div>
        </div>

        <div class="card-footer">
          <span class="timestamp">{{ formatTime(session.createdAt) }}</span>
        </div>
      </div>

      <div v-if="sessionStore.sessions.length === 0" class="empty-state">
        <div class="empty-icon">◯</div>
        <p>暂无活动会话</p>
        <span class="empty-hint">等待客户端连接...</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted } from 'vue'
import { useSessionStore } from '../stores/session'

const sessionStore = useSessionStore()

const activeCount = computed(() =>
  sessionStore.sessions.filter(s => s.status === 'CONNECTED').length
)

function formatTime(dateStr: string) {
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  })
}

async function refresh() {
  await sessionStore.fetchSessions()
}

onMounted(() => {
  refresh()
  const timer = setInterval(refresh, 5000)
  onUnmounted(() => clearInterval(timer))
})
</script>

<style scoped>
.sessions-page {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 2rem;
  gap: 2rem;
}

.header-title {
  flex: 1;
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
  color: var(--accent-green);
  font-size: 1.5rem;
}

.page-subtitle {
  color: var(--text-secondary);
  font-size: 0.875rem;
}

.header-stats {
  display: flex;
  gap: 1rem;
}

.stat-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 1rem 1.5rem;
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  min-width: 100px;
}

.stat-label {
  font-size: 0.7rem;
  color: var(--text-muted);
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--accent-green);
  margin-top: 0.25rem;
}

/* Sessions Grid */
.sessions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 1rem;
}

.session-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.session-card:hover {
  border-color: var(--accent-green);
  box-shadow: 0 8px 30px rgba(0, 255, 159, 0.1);
}

.session-card.status-connected {
  border-color: rgba(0, 255, 159, 0.3);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  border-bottom: 1px solid var(--border-color);
  background: var(--bg-tertiary);
}

.session-id {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.id-label {
  font-size: 0.65rem;
  color: var(--text-muted);
  font-weight: 500;
  letter-spacing: 0.05em;
}

.id-value {
  font-size: 0.875rem;
  font-weight: 600;
  font-family: var(--font-mono);
}

.status-badge {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.375rem 0.75rem;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.status-badge.status-connected {
  background: rgba(0, 255, 159, 0.1);
  color: var(--accent-green);
  border: 1px solid rgba(0, 255, 159, 0.3);
}

.status-badge.status-connected .status-dot {
  background: var(--accent-green);
  animation: pulse 2s ease-in-out infinite;
}

.status-badge.status-disconnected,
.status-badge.status-logged_out {
  background: rgba(255, 71, 87, 0.1);
  color: var(--accent-red);
  border: 1px solid rgba(255, 71, 87, 0.3);
}

.status-badge.status-disconnected .status-dot,
.status-badge.status-logged_out .status-dot {
  background: var(--accent-red);
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.card-body {
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-label {
  font-size: 0.7rem;
  color: var(--text-muted);
  font-weight: 500;
  letter-spacing: 0.05em;
}

.info-value {
  font-size: 0.875rem;
  font-weight: 500;
  font-family: var(--font-mono);
}

.card-footer {
  padding: 0.75rem 1rem;
  border-top: 1px solid var(--border-color);
  background: var(--bg-tertiary);
}

.timestamp {
  font-size: 0.75rem;
  color: var(--text-muted);
  font-family: var(--font-mono);
}

/* Empty State */
.empty-state {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  background: var(--bg-secondary);
  border: 1px dashed var(--border-color);
  border-radius: 8px;
}

.empty-icon {
  font-size: 3rem;
  color: var(--text-muted);
  margin-bottom: 1rem;
  opacity: 0.5;
}

.empty-state p {
  font-size: 1rem;
  color: var(--text-secondary);
  margin-bottom: 0.5rem;
}

.empty-hint {
  font-size: 0.875rem;
  color: var(--text-muted);
  font-family: var(--font-mono);
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}
</style>
