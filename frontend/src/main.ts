import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import './styles/global.css'
import App from './App.vue'

// 启用 Element Plus 深色主题
document.documentElement.classList.add('dark')

const app = createApp(App)

app.use(createPinia())
app.use(ElementPlus)

app.mount('#app')
