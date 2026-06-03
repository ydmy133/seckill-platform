import axios from 'axios'                    // 引入 axios
import { ElMessage } from 'element-plus'     // Element Plus 的消息提示组件（弹窗用）

// 创建一个配置好的 axios 实例（相当于 axios 的"分身"，带默认配置）
const request = axios.create({
  baseURL: '/api',              // 所有请求自动加上 /api 前缀（配合 Vite 代理）
  timeout: 10000                // 请求超时时间：10 秒没响应就报错
})

// ========== 请求拦截器：在请求发出之前执行 ==========
request.interceptors.request.use(
  (config) => {
    // 从 localStorage 中取出登录时保存的 token
    const token = localStorage.getItem('token')
    if (token) {
      // 如果 token 存在，把它塞到请求头里（后端 JwtInterceptor 从这读取）
      config.headers.Authorization = `Bearer ${token}`
    }
    return config  // 必须 return config，否则请求发不出去
  },
  (error) => {
    return Promise.reject(error)  // 把错误继续往下传
  }
)

// ========== 响应拦截器：在收到响应之后、交给你代码之前执行 ==========
request.interceptors.response.use(
  (response) => {
    const res = response.data          // 取后端返回的 JSON：{ code, message, data }
    if (res.code !== 200) {            // 如果状态码不是 200（成功），说明有错误
      ElMessage.error(res.message || '请求失败')  // 弹红色错误提示
      return Promise.reject(new Error(res.message))  // 把错误往下传
    }
    return res  // 成功的话，返回 { code, message, data }
  },
  (error) => {
    // HTTP 层面的错误（网络断开、超时、后端挂了等）
    if (error.response) {
      if (error.response.status === 401) {       // 401 = 未登录/Token 过期
        ElMessage.error('登录已过期，请重新登录')
        localStorage.removeItem('token')         // 清除过期的 token
        window.location.href = '/login'          // 跳转到登录页
      } else {
        ElMessage.error(error.response.data?.message || '网络错误')
      }
    } else {
      ElMessage.error('网络连接失败')
    }
    return Promise.reject(error)
  }
)

export default request