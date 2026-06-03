<template>
  <div class="seckill-list" v-loading="loading">
    <el-empty v-if="!loading && list.length === 0" description="暂无秒杀活动" />
    <div class="seckill-grid">
      <el-card v-for="item in list" :key="item.id" class="seckill-card" shadow="hover"
               @click="$router.push(`/seckill/${item.id}`)">
        <img :src="item.imageUrl || 'https://via.placeholder.com/200'" />
        <div class="seckill-info">
          <h3>{{ item.productName }}</h3>
          <p class="seckill-price">¥{{ item.seckillPrice }}
            <span class="original-price">¥{{ item.originalPrice }}</span>
          </p>
          <el-tag type="danger" size="small">限量 {{ item.stock }} 件</el-tag>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../api/request.js'

const loading = ref(false)
const list = ref([])

onMounted(async () => {
  loading.value = true
  const res = await request.get('/products/seckill', { params: { page: 1, size: 20 } })
  list.value = res.data.records
  loading.value = false
})
</script>

<style scoped>
.seckill-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
  padding: 20px;
}
.seckill-card { cursor: pointer; }
.seckill-card img { width: 100%; height: 180px; object-fit: cover; border-radius: 4px; }
.seckill-price { color: #f56c6c; font-size: 20px; font-weight: bold; margin: 8px 0; }
.original-price { color: #999; text-decoration: line-through; font-size: 14px; margin-left: 8px; }
</style>