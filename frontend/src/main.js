import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'      // Element Plus 的样式文件
import * as ElementPlusIconsVue from '@element-plus/icons-vue'  // 所有图标
import App from './App.vue'
import router from './router/index.js'

const app = createApp(App)

app.use(createPinia())                    // 注册 Pinia（状态管理）
app.use(router)                           // 注册 Router（路由跳转）
app.use(ElementPlus)                      // 注册 Element Plus（UI 组件库）

// 注册所有图标组件（这样在 .vue 文件里可以直接用 <el-icon><UserFilled /></el-icon>）
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')