import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../api/client'

export interface Message {
  id: number
  sessionId: string
  msgType: string
  symbol: string
  clOrdId?: string
  origClOrdId?: string
  price?: string
  orderQty?: string
  side?: string
  ordType?: string
  receivedAt: string
}

export const useMessageStore = defineStore('message', () => {
  const messages = ref<Message[]>([])
  const total = ref(0)

  async function searchMessages(params: { symbol?: string; clOrdId?: string; page?: number; size?: number }) {
    const response = await api.get('/messages', { params })
    messages.value = response.data.content
    total.value = response.data.totalElements
  }

  return { messages, total, searchMessages }
})
