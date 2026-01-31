<template>
  <div id="app">
    <div class="terminal-frame">
      <!-- Terminal Header -->
      <header class="terminal-header">
        <div class="header-left">
          <div class="logo">
            <span class="logo-bracket">&lt;</span>
            <span class="logo-text">GSIMULATOR</span>
            <span class="logo-bracket">/&gt;</span>
          </div>
          <div class="status-indicator">
            <span class="status-dot"></span>
            <span class="status-text">FIX 4.2</span>
          </div>
        </div>
        <nav class="terminal-nav">
          <router-link to="/sessions" class="nav-item" :class="{ active: route.path === '/sessions' }">
            <span class="nav-icon">⬡</span>
            <span>会话</span>
          </router-link>
          <router-link to="/messages" class="nav-item" :class="{ active: route.path === '/messages' }">
            <span class="nav-icon">◇</span>
            <span>消息</span>
          </router-link>
        </nav>
      </header>

      <!-- Terminal Content -->
      <main class="terminal-main">
        <router-view />
      </main>

      <!-- Terminal Footer -->
      <footer class="terminal-footer">
        <div class="footer-info">
          <span class="footer-label">PORT:</span>
          <span class="footer-value">9876</span>
        </div>
        <div class="footer-info">
          <span class="footer-label">UPTIME:</span>
          <span class="footer-value">{{ uptime }}</span>
        </div>
        <div class="footer-info">
          <span class="footer-label">STATUS:</span>
          <span class="footer-value online">RUNNING</span>
        </div>
      </footer>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const uptime = ref('00:00:00')
const startTime = Date.now()

function updateUptime() {
  const elapsed = Math.floor((Date.now() - startTime) / 1000)
  const hours = String(Math.floor(elapsed / 3600)).padStart(2, '0')
  const minutes = String(Math.floor((elapsed % 3600) / 60)).padStart(2, '0')
  const seconds = String(elapsed % 60).padStart(2, '0')
  uptime.value = `${hours}:${minutes}:${seconds}`
}

let timer: number
onMounted(() => {
  timer = setInterval(updateUptime, 1000) as unknown as number
})

onUnmounted(() => {
  clearInterval(timer)
})
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;500;600;700&family=Space+Grotesk:wght@400;500;600;700&display=swap');

:root {
  --bg-primary: #f8fafc;
  --bg-secondary: #ffffff;
  --bg-tertiary: #f1f5f9;
  --border-color: #e2e8f0;
  --text-primary: #1e293b;
  --text-secondary: #64748b;
  --text-muted: #94a3b8;
  --accent-blue: #3b82f6;
  --accent-blue-dim: rgba(59, 130, 246, 0.1);
  --accent-green: #10b981;
  --accent-red: #ef4444;
  --accent-amber: #f59e0b;
  --grid-color: rgba(59, 130, 246, 0.05);
  --font-mono: 'JetBrains Mono', 'SF Mono', 'Fira Code', monospace;
  --font-display: 'Space Grotesk', 'Inter', system-ui, sans-serif;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  min-height: 100vh;
  background: var(--bg-primary);
  font-family: var(--font-mono);
  color: var(--text-primary);
}

/* Grid Background */
body {
  background-image:
    linear-gradient(var(--grid-color) 1px, transparent 1px),
    linear-gradient(90deg, var(--grid-color) 1px, transparent 1px);
  background-size: 20px 20px;
  background-position: -1px -1px;
}

.terminal-frame {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
}

.terminal-frame::before {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    radial-gradient(ellipse at top, rgba(59, 130, 246, 0.08) 0%, transparent 50%),
    radial-gradient(ellipse at bottom right, rgba(59, 130, 246, 0.05) 0%, transparent 50%);
  pointer-events: none;
  z-index: 0;
}

/* Header */
.terminal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
  position: relative;
  z-index: 1;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 2rem;
}

.logo {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 1.25rem;
  letter-spacing: -0.02em;
}

.logo-bracket {
  color: var(--accent-blue);
  font-size: 1rem;
}

.logo-text {
  color: var(--text-primary);
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.25rem 0.75rem;
  background: var(--bg-tertiary);
  border: 1px solid var(--border-color);
  border-radius: 4px;
  font-size: 0.75rem;
}

.status-dot {
  width: 8px;
  height: 8px;
  background: var(--accent-blue);
  border-radius: 50%;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; box-shadow: 0 0 8px var(--accent-blue); }
  50% { opacity: 0.5; box-shadow: 0 0 4px var(--accent-blue); }
}

.status-text {
  color: var(--text-secondary);
  font-weight: 500;
}

/* Navigation */
.terminal-nav {
  display: flex;
  gap: 0.5rem;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  color: var(--text-secondary);
  text-decoration: none;
  font-size: 0.875rem;
  font-weight: 500;
  border: 1px solid transparent;
  border-radius: 4px;
  transition: all 0.2s ease;
  position: relative;
  overflow: hidden;
}

.nav-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--accent-blue-dim);
  opacity: 0;
  transition: opacity 0.2s ease;
}

.nav-item:hover {
  color: var(--text-primary);
  border-color: var(--border-color);
}

.nav-item:hover::before {
  opacity: 1;
}

.nav-item.active {
  color: var(--accent-blue);
  border-color: var(--accent-blue);
  background: var(--accent-blue-dim);
}

.nav-icon {
  font-size: 1rem;
}

.nav-item span:last-child {
  position: relative;
  z-index: 1;
}

.nav-item .nav-icon {
  position: relative;
  z-index: 1;
}

/* Main Content */
.terminal-main {
  flex: 1;
  padding: 1.5rem;
  position: relative;
  z-index: 1;
  overflow-y: auto;
}

/* Footer */
.terminal-footer {
  display: flex;
  gap: 2rem;
  padding: 0.75rem 1.5rem;
  background: var(--bg-secondary);
  border-top: 1px solid var(--border-color);
  position: relative;
  z-index: 1;
  font-size: 0.75rem;
}

.footer-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.footer-label {
  color: var(--text-muted);
  font-weight: 500;
}

.footer-value {
  color: var(--text-secondary);
  font-weight: 600;
}

.footer-value.online {
  color: var(--accent-blue);
}

/* Scrollbar */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: var(--bg-secondary);
}

::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: var(--text-muted);
}
</style>
