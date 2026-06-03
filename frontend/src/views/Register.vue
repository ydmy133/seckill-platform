<template>
  <div class="register-container">
    <!-- el-card：Element Plus 的卡片组件，自带边框和阴影 -->
    <el-card class="register-card" title="用户注册">
      <h2>用户注册</h2>

      <!-- el-form：表单组件 -->
      <!-- :model="form" → 表单数据绑定到 form 对象 -->
      <!-- label-width="80px" → 标签宽度 80 像素 -->
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名">
          <!-- v-model：双向绑定，输入框的值和 form.username 自动同步 -->
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>

        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>

        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号（选填）" />
        </el-form-item>

        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱（选填）" />
        </el-form-item>

        <el-form-item>
          <!-- type="primary" → 蓝色按钮 -->
          <!-- @click="handleRegister" → 点击时调用 handleRegister 方法 -->
          <el-button type="primary" @click="handleRegister" :loading="loading">
            {{ loading ? '注册中...' : '注册' }}
          </el-button>
          <!-- router-link：用 router 跳转，不会刷新页面 -->
          <router-link to="/login">
            <el-button type="text">已有账号？去登录</el-button>
          </router-link>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
// ===== script setup：Vue 3 Composition API 的语法糖 =====
import { ref, reactive } from 'vue'       // ref: 单一响应式数据, reactive: 对象响应式
import { useRouter } from 'vue-router'    // 编程式路由跳转
import { ElMessage } from 'element-plus'  // 消息弹窗
import { register } from '../api/auth.js' // 引入注册接口函数

const router = useRouter()                // 获取路由实例（用来跳转页面）

const loading = ref(false)                // ref(false) = 创建一个初始值为 false 的响应式变量
                                          // 为 true 时按钮显示"注册中..."并禁用

// reactive({...}) = 创建一个响应式对象，里面的任何字段变化，页面自动更新
const form = reactive({
  username: '',     // 绑定到输入框
  password: '',
  phone: '',
  email: ''
})

// async：异步函数，里面可以用 await 等待
const handleRegister = async () => {
  // 简单校验：用户名和密码不能为空
  if (!form.username.trim() || !form.password.trim()) {
    ElMessage.warning('用户名和密码不能为空')
    return   // 不满足条件就不往下执行
  }

  loading.value = true          // 显示加载状态
  try {
    await register(form)        // 调用 API：发 POST 请求到后端注册接口
    ElMessage.success('注册成功！')  // 绿色成功提示
    router.push('/login')       // 跳转到登录页
  } catch (error) {
    // 错误已经在 request.js 的拦截器里提示过了，这里可选加额外处理
  } finally {
    loading.value = false       // 无论成功失败，都关闭加载状态
  }
}
</script>

<style scoped>
/* scoped：这个样式只对当前组件生效，不会影响其他页面 */
.register-container {
  display: flex;
  justify-content: center;    /* 水平居中 */
  align-items: center;        /* 垂直居中 */
  height: 100vh;              /* 占满整个视口高度 */
  background: #f5f5f5;
}
.register-card {
  width: 420px;               /* 卡片宽度 */
}
.register-card h2 {
  text-align: center;
  margin-bottom: 24px;
}
</style>