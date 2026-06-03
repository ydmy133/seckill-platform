import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../views/Layout.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: Layout,          // 所有页面都套在 Layout 里（有公共导航栏）
      redirect: '/products',      // 访问 / 自动跳转到商品列表
      children: [                 // children = 嵌套路由，内容显示在 Layout 的 <router-view> 里
        {
          path: 'products',
          name: 'ProductList',
          component: () => import('../views/ProductList.vue')
        },
        {
          path: 'seckill',
          name: 'SeckillList',
          component: () => import('../views/SeckillList.vue')
        },
        {
          path: 'seckill/:seckillProductId',
          name: 'SeckillDetail',
          component: () => import('../views/SeckillDetail.vue')
        },
        {
          path: 'orders',
          name: 'OrderList',
          component: () => import('../views/OrderList.vue')
        }
      ]
    },
    { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
    { path: '/register', name: 'Register', component: () => import('../views/Register.vue') }
  ]
})

// 路由守卫：未登录 → 跳登录页
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (token) {
    if (to.path === '/login' || to.path === '/register') next('/products')
    else next()
  } else {
    if (to.path === '/login' || to.path === '/register') next()
    else next('/login')
  }
})

export default router