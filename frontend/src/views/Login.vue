<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>用户登录</h2>
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>

        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password
            @keyup.enter="handleLogin" />   <!-- 按回车键也可以提交 -->
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading">
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
          <router-link to="/register">
            <el-button type="text">没有账号？去注册</el-button>
          </router-link>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api/auth.js'          // 引入登录接口函数
import { useUserStore } from '../store/user.js'  // 引入 Pinia store

const router = useRouter()
const userStore = useUserStore()    // 获取 user store 实例

const loading = ref(false)
const form = reactive({
  username: '',
  password: ''
})

const handleLogin = async () => {
  if (!form.username.trim() || !form.password.trim()) {
    ElMessage.warning('用户名和密码不能为空')
    return
  }

  loading.value = true
  try {
    const res = await login(form)           // 调用登录 API
    userStore.setToken(res.data)            // 把返回的 JWT 存到 Pinia + localStorage
    userStore.setUsername(form.username)    // 保存用户名
    ElMessage.success('登录成功！')
    router.push('/')                        // 跳转到首页（目前会重定向到注册页，后面改）
  } catch (error) {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: #f5f5f5;
}
.login-card {
  width: 420px;
}
.login-card h2 {
  text-align: center;
  margin-bottom: 24px;
}
</style>