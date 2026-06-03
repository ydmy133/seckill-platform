import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
    { path: '/register', name: 'Register', component: () => import('../views/Register.vue') },
    { path: '/', redirect: '/register' }
  ]
})

// ===== 全局前置守卫：每次路由跳转前执行 =====
router.beforeEach((to, from, next) => {
  // 从 localStorage 检查是否有 token
  const token = localStorage.getItem('token')

  if (token) {
    // 有 token → 已登录，可以访问任何页面
    // 如果用户已经登录还想访问登录页，直接跳到首页（后面会改成商品列表）
    if (to.path === '/login' || to.path === '/register') {
      next('/')  // 重定向到首页
    } else {
      next()     // 正常放行
    }
  } else {
    // 没有 token → 未登录
    // 登录页和注册页不用登录就能访问
    if (to.path === '/login' || to.path === '/register') {
      next()     // 放行
    } else {
      next('/login')  // 其他页面：强制跳转到登录页
    }
  }
})

export default router