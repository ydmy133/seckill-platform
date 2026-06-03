<template>
  <div class="order-list" v-loading="loading">
    <h2>我的订单</h2>
    <el-table :data="orders" stripe>
      <el-table-column prop="orderNo" label="订单编号" width="220" />
      <el-table-column prop="productName" label="商品" />
      <el-table-column label="秒杀价格" width="120">
        <template #default="{ row }"> ¥{{ row.seckillPrice }} </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.status]?.type">
            {{ statusMap[row.status]?.text }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="下单时间" width="170">
        <template #default="{ row }"> {{ row.createTime }} </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="page"
      :total="total" :page-size="size"
      @current-change="fetchOrders"
      layout="prev, pager, next" background
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../api/request.js'

const loading = ref(false)
const orders = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

const statusMap = {
  0: { text: '未支付', type: 'warning' },
  1: { text: '已支付', type: 'success' },
  2: { text: '已取消', type: 'info' },
  3: { text: '已退款', type: 'danger' }
}

onMounted(() => fetchOrders())

const fetchOrders = async () => {
  loading.value = true
  const res = await request.get('/orders', { params: { page: page.value, size: size.value } })
  orders.value = res.data.records
  total.value = res.data.total
  loading.value = false
}
</script>

<style scoped>
.order-list { padding: 20px; }
</style>
