import { defineStore } from 'pinia'
import { ref } from 'vue'

// defineStore('user', ...) → 创建一个叫 "user" 的 store
export const useUserStore = defineStore('user', () => {
  // ===== 状态（相当于 data）=====
  const token = ref(localStorage.getItem('token') || '')  // 从 localStorage 恢复 token（刷新不丢失）
  const username = ref('')                                  // 当前登录的用户名

  // ===== 动作（相当于 methods）=====
  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)   // 存 localStorage，刷新页面还能取到
  }

  function setUsername(name) {
    username.value = name
  }

  function logout() {
    token.value = ''
    username.value = ''
    localStorage.removeItem('token')          // 退出登录时清除 token
  }

  // ===== 计算属性（相当于 computed）=====
  // isLoggedIn 不是方法，是一个根据 token 自动计算的值
  // const isLoggedIn = computed(() => !!token)

  // 把需要给外部用的数据和方法暴露出去
  return { token, username, setToken, setUsername, logout }
})