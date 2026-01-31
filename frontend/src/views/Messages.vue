<template>
  <div>
    <h2>消息查询</h2>
    <el-form :inline="true" @submit.prevent="handleSearch">
      <el-form-item label="股票代码">
        <el-input v-model="searchForm.symbol" placeholder="如: 600519" clearable />
      </el-form-item>
      <el-form-item label="ClOrdID">
        <el-input v-model="searchForm.clOrdId" placeholder="客户订单ID" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="messageStore.messages" stripe>
      <el-table-column prop="msgType" label="消息类型" width="80" />
      <el-table-column prop="symbol" label="股票代码" width="100" />
      <el-table-column prop="clOrdId" label="ClOrdID" width="150" />
      <el-table-column prop="origClOrdId" label="OrigClOrdID" width="150" />
      <el-table-column prop="price" label="价格" width="100" />
      <el-table-column prop="orderQty" label="数量" width="100" />
      <el-table-column prop="side" label="方向" width="80" />
      <el-table-column prop="ordType" label="类型" width="100" />
      <el-table-column prop="receivedAt" label="接收时间" />
    </el-table>

    <el-pagination
      v-model:current-page="pagination.page"
      v-model:page-size="pagination.size"
      :total="messageStore.total"
      layout="total, prev, pager, next"
      @current-change="handleSearch"
    />
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useMessageStore } from '../stores/message'

const messageStore = useMessageStore()
const searchForm = reactive({ symbol: '', clOrdId: '' })
const pagination = reactive({ page: 0, size: 20 })

function handleSearch() {
  messageStore.searchMessages({
    symbol: searchForm.symbol || undefined,
    clOrdId: searchForm.clOrdId || undefined,
    page: pagination.page,
    size: pagination.size
  })
}

handleSearch()
</script>
