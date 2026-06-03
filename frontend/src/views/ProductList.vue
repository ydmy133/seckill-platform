<template>
  <div class="product-page">
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="全部商品" name="products" />
      <el-tab-pane label="秒杀专场" name="seckill" />
    </el-tabs>

    <!-- 商品卡片网格 -->
    <div class="product-grid" v-loading="loading">
      <el-card v-for="item in productList" :key="item.id" class="product-card" shadow="hover">
        <img :src="item.imageUrl || 'https://via.placeholder.com/200'" class="product-img" />
        <div class="product-info">
          <h3>{{ item.name }}</h3>
          <p class="price">¥{{ item.originalPrice }}</p>
        </div>
      </el-card>
    </div>

    <!-- 分页器 -->
    <el-pagination
      v-model:current-page="page"
      :total="total"
      :page-size="size"
      @current-change="fetchProducts"
      layout="prev, pager, next"
      background
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../api/request.js'

const activeTab = ref('products')
const loading = ref(false)
const productList = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

// onMounted：Vue 3 生命周期钩子，组件挂载到页面上后自动执行（相当于 window.onload）
onMounted(() => fetchProducts())

const fetchProducts = async () => {
  loading.value = true
  try {
    // 根据当前 Tab 调用不同接口
    const url = activeTab.value === 'products' ? '/products' : '/products/seckill'
    const res = await request.get(url, { params: { page: page.value, size: size.value } })
    productList.value = res.data.records  // Page 对象：records=数据列表, total=总数
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  page.value = 1  // 切换 Tab 时重置到第1页
  fetchProducts()
}
</script>

<style scoped>
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); /* 自适应列数 */
  gap: 16px;
  margin: 20px 0;
}
.product-card { cursor: pointer; }
.product-img { width: 100%; height: 160px; object-fit: cover; }
.product-info h3 { font-size: 16px; margin: 8px 0; }
.price { color: #f56c6c; font-size: 18px; font-weight: bold; }
</style>