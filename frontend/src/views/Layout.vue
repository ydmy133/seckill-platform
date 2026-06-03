<template>
  <div class="layout">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="header-left">
        <h1 @click="$router.push('/')">秒杀平台</h1>
        <el-menu mode="horizontal" :default-active="$route.path" router
                 background-color="#409eff" text-color="#fff" active-text-color="#ffd04b">
          <el-menu-item index="/products">商品列表</el-menu-item>
          <el-menu-item index="/seckill">秒杀活动</el-menu-item>
          <el-menu-item index="/orders">我的订单</el-menu-item>
        </el-menu>
      </div>
      <div class="header-right">
        <template v-if="userStore.token">
          <!-- 已登录：显示用户名和退出按钮 -->
          <span class="username">{{ userStore.username }}</span>
          <el-button type="danger" size="small" @click="handleLogout">退出</el-button>
        </template>
        <template v-else>
          <!-- 未登录：显示登录和注册按钮 -->
          <el-button size="small" @click="$router.push('/login')">登录</el-button>
          <el-button size="small" type="primary" @click="$router.push('/register')">注册</el-button>
        </template>
      </div>
    </el-header>

    <!-- 页面主体：根据 URL 显示对应的页面组件 -->
    <el-main>
      <router-view />
    </el-main>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user.js'

const router = useRouter()
const userStore = useUserStore()

const handleLogout = () => {
  userStore.logout()      // 清除 Pinia 里的 token 和 username
  router.push('/login')   // 跳回登录页
}
</script>

<style scoped>
.layout { min-height: 100vh; background: #f5f5f5; }
.header {
  display: flex; justify-content: space-between; align-items: center;
  background: #409eff; color: white; padding: 0 24px; height: 60px;
}
.header-left h1 { cursor: pointer; font-size: 20px; }
.header-right { display: flex; align-items: center; gap: 12px; }
.username { color: white; }
</style>