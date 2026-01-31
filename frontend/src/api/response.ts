import api from './client'

export interface ManualResponseRequest {
  sessionId: string
  clOrdId: string
  symbol: string
  side: string
  orderQty: number
  price?: number
  execType: string
  ordStatus: string
  lastQty?: number
  lastPx?: number
  cumQty?: number
  avgPx?: number
  origClOrdId?: string
  text?: string
}

export interface ApiResponse<T = any> {
  success: boolean
  message: string
  data?: T
}

export async function sendManualResponse(request: ManualResponseRequest): Promise<ApiResponse> {
  const response = await api.post('/responses/manual', request)
  return response.data
}
