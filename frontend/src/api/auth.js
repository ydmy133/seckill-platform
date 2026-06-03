import request from './request.js'     // 引入刚才封装好的 axios 实例

// 注册接口：传用户名密码，返回 { code, message, data }
export function register(data) {
  return request.post('/auth/register', data)   // POST /api/auth/register
}

// 登录接口：传用户名密码，返回 { code, message, data: "jwt字符串" }
export function login(data) {
  return request.post('/auth/login', data)      // POST /api/auth/login
}

export function getProducts(params) {
  return request.get('/products', { params })        // GET /api/products?page=1&size=10
}
export function getSeckillProducts(params) {
  return request.get('/products/seckill', { params }) // GET /api/products/seckill?page=1&size=10
}
export function getSeckillDetail(seckillProductId) {
  return request.get(`/products/seckill/${seckillProductId}`) // GET /api/products/seckill/5
}