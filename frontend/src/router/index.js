import { createRouter, createWebHistory } from 'vue-router'

// createWebHistory() → 使用 HTML5 History 模式（URL 里没有 # 号，看起来像正常 URL）
const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',                    // URL 路径
      name: 'Login',                     // 路由名字（编程式跳转时用）
      component: () => import('../views/Login.vue')    // 懒加载：访问时才加载这个页面的代码
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('../views/Register.vue')
    },
    {
      path: '/',                         // 首页（暂时先重定向到注册页）
      redirect: '/register'
    }
  ]
})

export default router