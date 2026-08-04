import { createRouter, createWebHashHistory, type RouteRecordRaw } from 'vue-router'

// hash 模式：兼容 Vite dev 无服务器配置，刷新不丢失路由
const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'home',
    redirect: '/demo',
  },
  {
    path: '/demo',
    name: 'demo',
    component: () => import('../views/DecisionDemo.vue'),
  },
  {
    path: '/company',
    name: 'company',
    component: () => import('../views/modules/CompanyArch.vue'),
  },
  {
    path: '/crm',
    name: 'crm',
    component: () => import('../views/modules/CrmModule.vue'),
  },
  {
    path: '/product',
    name: 'product',
    component: () => import('../views/modules/ProductModule.vue'),
  },
  {
    path: '/system',
    name: 'system',
    component: () => import('../views/modules/SystemModule.vue'),
  },
  {
    path: '/cms',
    name: 'cms',
    component: () => import('../views/modules/CmsModule.vue'),
  },
  {
    path: '/operation',
    name: 'operation',
    component: () => import('../views/modules/OperationModule.vue'),
  },
  {
    path: '/stats',
    name: 'stats',
    component: () => import('../views/modules/StatsModule.vue'),
  },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

export default router
