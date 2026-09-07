import { createRouter, createWebHistory } from 'vue-router'

const ProductListView = () => import('@/views/user/ProductListView.vue')
const ProductDetailView = () => import('@/views/user/ProductDetailView.vue')
const CartView = () => import('@/views/user/CartView.vue')
const OrderView = () => import('@/views/user/OrderView.vue')
const ProfileView = () => import('@/views/user/ProfileView.vue')
const DashboardView = () => import('@/views/monitor/DashboardView.vue')
const RequestLogListView = () => import('@/views/request-log/RequestLogListView.vue')
const RequestDetailView = () => import('@/views/request-log/RequestDetailView.vue')
const UserActivityView = () => import('@/views/users/UserActivityView.vue')
const ModuleMonitorView = () => import('@/views/modules/ModuleMonitorView.vue')
const DeveloperMonitorView = () => import('@/views/developers/DeveloperMonitorView.vue')
const LoginView = () => import('@/views/LoginView.vue')

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
      meta: { title: '商品列表' },
    },
    {
      path: '/products/:id',
      name: 'ProductDetail',
      component: ProductDetailView,
      meta: { title: '商品详情' },
    },
    {
      path: '/cart',
      name: 'Cart',
      component: CartView,
      meta: { title: '购物车' },
    },
    {
      path: '/orders',
      name: 'Orders',
      component: OrderView,
      meta: { title: '我的订单' },
    },
    {
      path: '/profile',
      name: 'Profile',
      component: ProfileView,
      meta: { title: '个人中心' },
    },
    {
      path: '/monitor',
      name: 'MonitorDashboard',
      component: DashboardView,
      meta: { title: '监控总览' },
    },
    {
      path: '/monitor/requests',
      name: 'RequestLogs',
      component: RequestLogListView,
      meta: { title: '请求日志' },
    },
    {
      path: '/monitor/requests/:requestId',
      name: 'RequestDetail',
      component: RequestDetailView,
      meta: { title: '请求详情' },
    },
    {
      path: '/monitor/users',
      name: 'UserActivity',
      component: UserActivityView,
      meta: { title: '用户活跃' },
    },
    {
      path: '/monitor/modules',
      name: 'ModuleMonitor',
      component: ModuleMonitorView,
      meta: { title: '模块监控' },
    },
    {
      path: '/monitor/developers',
      name: 'DeveloperMonitor',
      component: DeveloperMonitorView,
      meta: { title: '开发者监控' },
    },
  ],
})

export default router
