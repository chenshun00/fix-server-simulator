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
  --bg-primary: #0a0e0a;
  --bg-secondary: #111611;
  --bg-tertiary: #1a211a;
  --border-color: #2a3a2a;
  --text-primary: #e8f5e8;
  --text-secondary: #8fa88f;
  --text-muted: #5a6a5a;
  --accent-green: #00ff9f;
  --accent-green-dim: rgba(0, 255, 159, 0.1);
  --accent-red: #ff4757;
  --accent-amber: #ffa502;
  --accent-blue: #2ed573;
  --grid-color: rgba(0, 255, 159, 0.03);
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
    radial-gradient(ellipse at top, rgba(0, 255, 159, 0.05) 0%, transparent 50%),
    radial-gradient(ellipse at bottom right, rgba(46, 213, 115, 0.03) 0%, transparent 50%);
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
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
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
  color: var(--accent-green);
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
  background: var(--accent-green);
  border-radius: 50%;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; box-shadow: 0 0 8px var(--accent-green); }
  50% { opacity: 0.5; box-shadow: 0 0 4px var(--accent-green); }
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
  background: var(--accent-green-dim);
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
  color: var(--accent-green);
  border-color: var(--accent-green);
  background: var(--accent-green-dim);
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
  color: var(--accent-green);
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
