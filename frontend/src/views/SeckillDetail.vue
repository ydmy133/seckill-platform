<template>
  <div class="seckill-detail" v-loading="loading">
    <el-row :gutter="24">
      <el-col :span="8">
        <img :src="detail.imageUrl || 'https://via.placeholder.com/400'" class="detail-img" />
      </el-col>
      <el-col :span="16">
        <h1>{{ detail.productName }}</h1>
        <div class="price-area">
          <span class="seckill-price">¥{{ detail.seckillPrice }}</span>
          <span class="original-price">¥{{ detail.originalPrice }}</span>
        </div>
        <div class="stock-info">
          剩余库存：<span class="stock-num">{{ detail.stock }}</span>
        </div>

        <div class="countdown" v-if="countdownText">
          距离{{ countdownLabel }}还有：<span class="time">{{ countdownText }}</span>
        </div>

        <el-button
          type="danger"
          size="large"
          :disabled="btnDisabled"
          @click="handleSeckill"
          :loading="seckilling"
        >
          {{ btnText }}
        </el-button>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../api/request.js'

const route = useRoute()
const seckillProductId = route.params.seckillProductId

const loading = ref(false)
const seckilling = ref(false)
const detail = ref({})
const now = ref(Date.now())
let timer = null

// ===== 页面加载：查秒杀详情 + 启动倒计时 =====
onMounted(async () => {
  loading.value = true
  const res = await request.get(`/products/seckill/${seckillProductId}`)
  detail.value = res.data
  loading.value = false
  timer = setInterval(() => { now.value = Date.now() }, 1000)
})

onUnmounted(() => clearInterval(timer))

// ===== 倒计时计算 =====
const countdownInfo = computed(() => {
  const start = new Date(detail.value.startTime).getTime()
  const end = new Date(detail.value.endTime).getTime()
  if (now.value < start) {
    const diff = Math.floor((start - now.value) / 1000)
    const h = Math.floor(diff / 3600)
    const m = Math.floor((diff % 3600) / 60)
    const s = diff % 60
    return { label: '开始', text: `${h}时${m}分${s}秒`, state: 'before' }
  } else if (now.value >= start && now.value <= end) {
    const diff = Math.floor((end - now.value) / 1000)
    const h = Math.floor(diff / 3600)
    const m = Math.floor((diff % 3600) / 60)
    const s = diff % 60
    return { label: '结束', text: `${h}时${m}分${s}秒`, state: 'going' }
  } else {
    return { label: '', text: '', state: 'ended' }
  }
})

const countdownLabel = computed(() => countdownInfo.value.label)
const countdownText = computed(() => countdownInfo.value.text)

const btnDisabled = computed(() => countdownInfo.value.state !== 'going' || seckilling.value)

const btnText = computed(() => {
  if (countdownInfo.value.state === 'before') return '尚未开始'
  if (countdownInfo.value.state === 'ended') return '已结束'
  return seckilling.value ? '抢购中...' : '立即抢购'
})

// ===== 点击抢购 =====
const handleSeckill = async () => {
  seckilling.value = true
  try {
    const res = await request.post(`/seckill/${seckillProductId}/execute`)
    if (res.code === 200) {
      ElMessage.success('抢购成功！订单生成中...')
      pollResult()
    }
  } catch (error) {
    // 错误已在拦截器中提示
  } finally {
    seckilling.value = false
  }
}

// ===== 轮询秒杀结果 =====
const pollResult = () => {
  const token = localStorage.getItem('token')
  if (!token) return
  const payload = JSON.parse(atob(token.split('.')[1]))
  const userId = payload.sub

  const poll = setInterval(async () => {
    const res = await request.get(`/seckill/${seckillProductId}/result`, {
      params: { userId }
    })
    if (res.data && res.data.id) {
      clearInterval(poll)
      ElMessage.success('订单已生成！')
    }
  }, 500)
  setTimeout(() => clearInterval(poll), 30000)
}
</script>

<style scoped>
.seckill-detail { padding: 24px; max-width: 1000px; margin: 0 auto; }
.detail-img { width: 100%; border-radius: 8px; }
.seckill-price { color: #f56c6c; font-size: 32px; font-weight: bold; margin-right: 12px; }
.original-price { color: #999; text-decoration: line-through; font-size: 18px; }
.stock-info { margin: 12px 0; font-size: 16px; }
.stock-num { color: #f56c6c; font-weight: bold; }
.countdown { margin: 16px 0; font-size: 16px; }
.countdown .time { color: #f56c6c; font-size: 24px; font-weight: bold; }
</style>
