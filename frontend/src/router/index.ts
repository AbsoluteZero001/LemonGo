import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const ProductListView = () => import('@/views/user/ProductListView.vue')
const ProductDetailView = () => import('@/views/user/ProductDetailView.vue')
const CartView = () => import('@/views/user/CartView.vue')
const OrderView = () => import('@/views/user/OrderView.vue')
const ProfileView = () => import('@/views/user/ProfileView.vue')
const DashboardView = () => import('@/views/monitor/DashboardView.vue')
const LiveTraceView = () => import('@/views/monitor/LiveTraceView.vue')
const RequestLogListView = () => import('@/views/request-log/RequestLogListView.vue')
const RequestDetailView = () => import('@/views/request-log/RequestDetailView.vue')
const UserActivityView = () => import('@/views/users/UserActivityView.vue')
const ModuleMonitorView = () => import('@/views/modules/ModuleMonitorView.vue')
const DeveloperMonitorView = () => import('@/views/developers/DeveloperMonitorView.vue')
const ApiRegistryView = () => import('@/views/monitor/ApiRegistryView.vue')
const ErrorLogView = () => import('@/views/monitor/ErrorLogView.vue')
const ProductAdminView = () => import('@/views/admin/ProductAdminView.vue')
const UserAdminView = () => import('@/views/admin/UserAdminView.vue')
const OrderAdminView = () => import('@/views/admin/OrderAdminView.vue')
const DeveloperAdminView = () => import('@/views/admin/DeveloperAdminView.vue')
const ModuleAdminView = () => import('@/views/admin/ModuleAdminView.vue')
const ApiAdminView = () => import('@/views/admin/ApiAdminView.vue')
const LoginView = () => import('@/views/LoginView.vue')

function homeForRole(role: string) {
  if (role === 'ADMIN') {
    return '/admin'
  }
  if (role === 'MONITOR') {
    return '/monitor'
  }
  return '/products'
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/products' },
    {
      path: '/login',
      name: 'Login',
      component: LoginView,
      meta: { title: '登录' },
    },
    {
      path: '/products',
      name: 'ProductList',
      component: ProductListView,
      meta: { title: '商品列表', role: 'USER' },
    },
    {
      path: '/products/:id',
      name: 'ProductDetail',
      component: ProductDetailView,
      meta: { title: '商品详情', role: 'USER' },
    },
    {
      path: '/cart',
      name: 'Cart',
      component: CartView,
      meta: { title: '购物车', role: 'USER' },
    },
    {
      path: '/orders',
      name: 'Orders',
      component: OrderView,
      meta: { title: '我的订单', role: 'USER' },
    },
    {
      path: '/profile',
      name: 'Profile',
      component: ProfileView,
      meta: { title: '个人中心', role: 'USER' },
    },
    {
      path: '/admin',
      redirect: '/admin/products',
    },
    {
      path: '/admin/products',
      name: 'ProductAdmin',
      component: ProductAdminView,
      meta: { title: '商品管理', role: 'ADMIN' },
    },
    {
      path: '/admin/users',
      name: 'UserAdmin',
      component: UserAdminView,
      meta: { title: '用户管理', role: 'ADMIN' },
    },
    {
      path: '/admin/orders',
      name: 'OrderAdmin',
      component: OrderAdminView,
      meta: { title: '订单管理', role: 'ADMIN' },
    },
    {
      path: '/admin/developers',
      name: 'DeveloperAdmin',
      component: DeveloperAdminView,
      meta: { title: '开发者管理', role: 'ADMIN' },
    },
    {
      path: '/admin/modules',
      name: 'ModuleAdmin',
      component: ModuleAdminView,
      meta: { title: '模块管理', role: 'ADMIN' },
    },
    {
      path: '/admin/apis',
      name: 'ApiAdmin',
      component: ApiAdminView,
      meta: { title: '接口管理', role: 'ADMIN' },
    },
    {
      path: '/monitor',
      name: 'MonitorDashboard',
      component: DashboardView,
      meta: { title: '监控总览', role: 'MONITOR' },
    },
    {
      path: '/monitor/live',
      name: 'LiveTrace',
      component: LiveTraceView,
      meta: { title: '实时链路', role: 'MONITOR' },
    },
    {
      path: '/monitor/requests',
      name: 'RequestLogs',
      component: RequestLogListView,
      meta: { title: '请求日志', role: 'MONITOR' },
    },
    {
      path: '/monitor/requests/:requestId',
      name: 'RequestDetail',
      component: RequestDetailView,
      meta: { title: '请求详情', role: 'MONITOR' },
    },
    {
      path: '/monitor/apis',
      name: 'ApiRegistry',
      component: ApiRegistryView,
      meta: { title: '接口注册表', role: 'MONITOR' },
    },
    {
      path: '/monitor/errors',
      name: 'ErrorLogs',
      component: ErrorLogView,
      meta: { title: '异常日志', role: 'MONITOR' },
    },
    {
      path: '/monitor/users',
      name: 'UserActivity',
      component: UserActivityView,
      meta: { title: '用户活跃', role: 'MONITOR' },
    },
    {
      path: '/monitor/modules',
      name: 'ModuleMonitor',
      component: ModuleMonitorView,
      meta: { title: '模块监控', role: 'MONITOR' },
    },
    {
      path: '/monitor/developers',
      name: 'DeveloperMonitor',
      component: DeveloperMonitorView,
      meta: { title: '开发者监控', role: 'MONITOR' },
    },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.path === '/login') {
    if (auth.isLoggedIn) {
      return homeForRole(auth.role)
    }
    return true
  }
  if (!auth.isLoggedIn) {
    return '/login'
  }
  const required = to.meta.role as string | undefined
  if (required && required !== auth.role) {
    return homeForRole(auth.role)
  }
  return true
})

export default router
