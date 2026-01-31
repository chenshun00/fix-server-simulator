<template>
  <div>
    <h2>会话列表</h2>
    <el-table :data="sessionStore.sessions" stripe>
      <el-table-column prop="sessionId" label="会话ID" />
      <el-table-column prop="senderCompId" label="SenderCompID" />
      <el-table-column prop="targetCompId" label="TargetCompID" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="port" label="端口" />
      <el-table-column prop="createdAt" label="连接时间" />
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'
import { useSessionStore } from '../stores/session'

const sessionStore = useSessionStore()

function getStatusType(status: string) {
  return status === 'CONNECTED' ? 'success' : 'info'
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
