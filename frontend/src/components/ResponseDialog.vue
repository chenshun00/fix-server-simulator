<template>
  <el-dialog
    v-model="dialogVisible"
    title="手动回报"
    width="650px"
    @close="handleClose"
  >
    <el-form :model="form" label-width="120px" class="response-form">
      <!-- 固定字段：只读 -->
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="ClOrdID">
            <el-input v-model="form.clOrdId" disabled />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="Symbol">
            <el-input v-model="form.symbol" disabled />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="Side">
            <el-input v-model="form.side" disabled />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="OrderQty">
            <el-input-number v-model="form.orderQty" :min="0" disabled class="full-width" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="Price">
            <el-input-number v-model="form.price" :min="0" :precision="4" disabled class="full-width" />
          </el-form-item>
        </el-col>
        <el-col :span="12"></el-col>
      </el-row>

      <el-divider />

      <!-- 回报类型选择 -->
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="ExecType">
            <el-select v-model="form.execType" @change="handleExecTypeChange" class="full-width">
              <el-option label="NEW (0) - 新订单" value="0" />
              <el-option label="PARTIAL_FILL (1) - 部分成交" value="1" />
              <el-option label="FILL (2) - 完全成交" value="2" />
              <el-option label="CANCEL (4) - 撤单确认" value="4" />
              <el-option label="REPLACE (5) - 改单确认" value="5" />
              <el-option label="REJECTED (8) - 业务拒绝" value="8" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="OrdStatus">
            <el-select v-model="form.ordStatus" class="full-width">
              <el-option label="NEW (0)" value="0" />
              <el-option label="PARTIALLY_FILLED (1)" value="1" />
              <el-option label="FILLED (2)" value="2" />
              <el-option label="CANCELED (4)" value="4" />
              <el-option label="REPLACED (5)" value="5" />
              <el-option label="REJECTED (8)" value="8" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 条件字段：根据 ExecType 动态显示 -->
      <!-- PARTIAL_FILL / FILL: LastQty, LastPx -->
      <div v-if="showLastFields">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="LastQty">
              <el-input-number v-model="form.lastQty" :min="0" class="full-width" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="LastPx">
              <el-input-number v-model="form.lastPx" :min="0" :precision="4" class="full-width" />
            </el-form-item>
          </el-col>
        </el-row>
      </div>

      <!-- FILL: CumQty, AvgPx (只读显示) -->
      <div v-if="form.execType === '2'">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="CumQty">
              <el-input :value="calcCumQty" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="AvgPx">
              <el-input :value="calcAvgPx" disabled />
            </el-form-item>
          </el-col>
        </el-row>
      </div>

      <!-- CANCEL / REPLACE: OrigClOrdID -->
      <div v-if="showOrigClOrdId">
        <el-form-item label="OrigClOrdID">
          <el-input v-model="form.origClOrdId" placeholder="原始客户订单ID" />
        </el-form-item>
      </div>

      <!-- REPLACE: 新的 OrderQty 和 Price -->
      <div v-if="form.execType === '5'">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="新 OrderQty">
              <el-input-number v-model="form.newOrderQty" :min="0" class="full-width" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="新 Price">
              <el-input-number v-model="form.newPrice" :min="0" :precision="4" class="full-width" />
            </el-form-item>
          </el-col>
        </el-row>
      </div>

      <!-- REJECTED: Text -->
      <div v-if="form.execType === '8'">
        <el-form-item label="Text">
          <el-input
            v-model="form.text"
            type="textarea"
            :rows="2"
            placeholder="拒绝原因"
          />
        </el-form-item>
      </div>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSend" :loading="sending">
        发送回报
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { sendManualResponse, type ManualResponseRequest } from '../api/response'

interface Message {
  id: number
  sessionId: string
  clOrdId?: string
  symbol: string
  side?: string
  orderQty?: number
  price?: number
}

const props = defineProps<{
  visible: boolean
  message: Message | null
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'success'): void
}>()

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const sending = ref(false)

const form = ref<ManualResponseRequest>({
  sessionId: '',
  clOrdId: '',
  symbol: '',
  side: '',
  orderQty: 0,
  price: 0,
  execType: '0',
  ordStatus: '0',
  lastQty: undefined,
  lastPx: undefined,
  cumQty: undefined,
  avgPx: undefined,
  origClOrdId: undefined,
  text: undefined
})

// REPLACE 类型时的额外字段
const newOrderQty = ref<number>()
const newPrice = ref<number>()

// 计算属性：动态字段显示
const showLastFields = computed(() => ['1', '2'].includes(form.value.execType))
const showOrigClOrdId = computed(() => ['4', '5'].includes(form.value.execType))

// 计算属性：CumQty
const calcCumQty = computed(() => {
  if (form.value.execType === '2') {
    return form.value.orderQty // FILL: CumQty = OrderQty
  }
  if (form.value.execType === '1' && form.value.lastQty) {
    return form.value.lastQty // PARTIAL_FILL: CumQty = LastQty
  }
  return 0
})

// 计算属性：AvgPx
const calcAvgPx = computed(() => {
  return form.value.lastPx || form.value.price || 0
})

// 监听 props.message 变化，更新表单
watch(() => props.message, (newMessage) => {
  if (newMessage) {
    form.value = {
      sessionId: newMessage.sessionId,
      clOrdId: newMessage.clOrdId || '',
      symbol: newMessage.symbol,
      side: newMessage.side || '',
      orderQty: newMessage.orderQty || 0,
      price: newMessage.price,
      execType: '0',
      ordStatus: '0',
      lastQty: undefined,
      lastPx: undefined,
      cumQty: undefined,
      avgPx: undefined,
      origClOrdId: undefined,
      text: undefined
    }
  }
}, { immediate: true })

// ExecType 变化时联动 OrdStatus
function handleExecTypeChange() {
  const execType = form.value.execType
  // 默认联动
  const ordStatusMap: Record<string, string> = {
    '0': '0',
    '1': '1',
    '2': '2',
    '4': '4',
    '5': '5',
    '8': '8'
  }
  form.value.ordStatus = ordStatusMap[execType] || '0'

  // 清空条件字段
  form.value.lastQty = undefined
  form.value.lastPx = undefined
  form.value.origClOrdId = undefined
  form.value.text = undefined
}

function handleClose() {
  dialogVisible.value = false
}

async function handleSend() {
  sending.value = true
  try {
    const request: ManualResponseRequest = {
      ...form.value,
      // REPLACE 类型时使用新的 OrderQty 和 Price
      orderQty: form.value.execType === '5' ? (newOrderQty.value || form.value.orderQty) : form.value.orderQty,
      price: form.value.execType === '5' ? newPrice.value : form.value.price
    }

    // 设置计算的 CumQty 和 AvgPx
    if (form.value.execType === '2') {
      request.cumQty = form.value.orderQty
      request.avgPx = form.value.lastPx || form.value.price
    }

    await sendManualResponse(request)
    ElMessage.success('回报发送成功')
    emit('success')
    handleClose()
  } catch (error: any) {
    const message = error.response?.data?.message || '发送失败'
    ElMessage.error(message)
  } finally {
    sending.value = false
  }
}
</script>

<style scoped>
.response-form .full-width {
  width: 100%;
}

.el-divider {
  margin: 1.5rem 0;
}

:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-input-number .el-input__inner) {
  text-align: left;
}
</style>
