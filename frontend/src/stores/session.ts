import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../api/client'

export interface Session {
  id: number
  sessionId: string
  senderCompId: string
  targetCompId: string
  status: string
  port?: number
  createdAt: string
  updatedAt: string
}

export const useSessionStore = defineStore('session', () => {
  const sessions = ref<Session[]>([])

  async function fetchSessions() {
    const response = await api.get('/sessions')
    sessions.value = response.data
  }

  return { sessions, fetchSessions }
})
