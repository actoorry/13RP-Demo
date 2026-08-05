import { createRouter, createWebHashHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '../stores/auth'

// hash 模式：兼容 Vite dev 无服务器配置，刷新不丢失路由
const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/admin',
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/Login.vue'),
  },
  {
    path: '/admin',
    name: 'admin',
    component: () => import('../layout/AdminLayout.vue'),
    redirect: '/admin/base/account',
    children: [
      // ---------- 基础数据域（base） ----------
      {
        path: 'base',
        redirect: '/admin/base/account',
        children: [
          {
            path: 'account',
            name: 'base-account',
            component: () => import('../views/base/AccountList.vue'),
            meta: { title: '账套管理' },
          },
          {
            path: 'product',
            name: 'base-product',
            component: () => import('../views/base/ProductList.vue'),
            meta: { title: '产品主数据' },
          },
          {
            path: 'material-element',
            name: 'base-material-element',
            component: () => import('../views/base/MaterialElementList.vue'),
            meta: { title: '材质元素' },
          },
          {
            path: 'package-standard',
            name: 'base-package-standard',
            component: () => import('../views/base/PackageStandardList.vue'),
            meta: { title: '合同包装验收标准' },
          },
          {
            path: 'mobile-config',
            name: 'base-mobile-config',
            component: () => import('../views/base/MobileConfigList.vue'),
            meta: { title: '移动端主营品种' },
          },
        ],
      },
      // ---------- 组织权限域（org） ----------
      {
        path: 'org',
        redirect: '/admin/org/dict',
        children: [
          {
            path: 'dict',
            name: 'org-dict',
            component: () => import('../views/org/DictTree.vue'),
            meta: { title: '组织/岗位字典' },
          },
          {
            path: 'group',
            name: 'org-group',
            component: () => import('../views/org/GroupManage.vue'),
            meta: { title: '组管理' },
          },
          {
            path: 'comprehensive',
            name: 'org-comprehensive',
            component: () => import('../views/org/ComprehensiveManage.vue'),
            meta: { title: '综合管理' },
          },
          {
            path: 'employee',
            name: 'org-employee',
            component: () => import('../views/org/EmployeeManage.vue'),
            meta: { title: '员工管理' },
          },
          {
            path: 'permission',
            name: 'org-permission',
            component: () => import('../views/org/PermissionView.vue'),
            meta: { title: '我的权限' },
          },
        ],
      },
      // ---------- 采购域（purchase） ----------
      {
        path: 'purchase',
        redirect: '/admin/purchase/supplier-grade',
        children: [
          {
            path: 'supplier-grade',
            name: 'purchase-supplier-grade',
            component: () => import('../views/purchase/SupplierGradeList.vue'),
            meta: { title: '供应商分级' },
          },
          {
            path: 'forecast',
            name: 'purchase-forecast',
            component: () => import('../views/purchase/ForecastPlanList.vue'),
            meta: { title: '预测预案' },
          },
          {
            path: 'inquiry',
            name: 'purchase-inquiry',
            component: () => import('../views/purchase/InquiryList.vue'),
            meta: { title: '询价管理' },
          },
          {
            path: 'apply',
            name: 'purchase-apply',
            component: () => import('../views/purchase/PurchaseApplyList.vue'),
            meta: { title: '采购申请' },
          },
          {
            path: 'order',
            name: 'purchase-order',
            component: () => import('../views/purchase/PendingOrderList.vue'),
            meta: { title: '待审批订单' },
          },
          {
            path: 'debt',
            name: 'purchase-debt',
            component: () => import('../views/purchase/DebtPayableList.vue'),
            meta: { title: '进项欠票' },
          },
          {
            path: 'payable',
            name: 'purchase-payable',
            component: () => import('../views/purchase/PayableList.vue'),
            meta: { title: '应付列表' },
          },
        ],
      },
      // ---------- 批次 2 域（sale/inventory/finance 已接真实页面；crm/flow/todo 仍占位） ----------
      {
        path: 'sale',
        name: 'sale',
        component: () => import('../views/sale/OrderList.vue'),
        meta: { title: '销售域' },
      },
      {
        path: 'inventory',
        name: 'inventory',
        component: () => import('../views/inventory/StockList.vue'),
        meta: { title: '库存域' },
      },
      {
        path: 'finance',
        name: 'finance',
        component: () => import('../views/finance/ArrivalList.vue'),
        meta: { title: '财务域' },
      },
      {
        path: 'crm',
        name: 'crm',
        component: () => import('../views/crm/ActivityList.vue'),
        meta: { title: 'CRM' },
      },
      {
        path: 'flow',
        name: 'flow',
        component: () => import('../views/flow/X5List.vue'),
        meta: { title: '流程引擎' },
      },
      {
        path: 'todo',
        name: 'todo',
        component: () => import('../views/todo/SubscriptionList.vue'),
        meta: { title: '待办事宜' },
      },
    ],
  },
  // 决策演示：原样保留，匿名可访问
  {
    path: '/demo',
    name: 'demo',
    component: () => import('../views/DecisionDemo.vue'),
  },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

// 路由守卫：未登录访问 /admin 跳 /login；已登录访问 /login 跳 /admin；/demo 匿名放行
router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.path.startsWith('/admin') && !auth.isLoggedIn) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.path === '/login' && auth.isLoggedIn) {
    return { path: '/admin' }
  }
  return true
})

export default router
