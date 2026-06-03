import request from './request.js'     // 引入刚才封装好的 axios 实例

// 注册接口：传用户名密码，返回 { code, message, data }
export function register(data) {
  return request.post('/auth/register', data)   // POST /api/auth/register
}

// 登录接口：传用户名密码，返回 { code, message, data: "jwt字符串" }
export function login(data) {
  return request.post('/auth/login', data)      // POST /api/auth/login
}