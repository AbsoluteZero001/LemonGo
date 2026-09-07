import { del, get, post, put } from './http'
import type { PageResult } from './http'

export interface HealthInfo {
  serviceName: string
  status: string
  timestamp: string
}

export interface Profile {
  id: number
  username: string
  nickname: string
  email?: string
  phone?: string
  avatarUrl?: string
  lastLoginTime?: string
  lastActiveTime?: string
  activityScore: number
}

export interface LoginResult {
  token: string
  profile: Profile
}

export interface Product {
  id: number
  productName: string
  category: string
  price: number
  stock: number
  sales: number
  imageUrl?: string
  detailText?: string
}

export interface CartItem {
  id: number
  productId: number
  productName: string
  category: string
  price: number
  imageUrl?: string
  quantity: number
  stock: number
  checked: number
}

export interface OrderSummary {
  id: number
  orderNo: string
  totalAmount: number
  orderStatus: string
  payStatus: string
  paymentMethod?: string
  createdAt: string
}

export interface OrderItem {
  id: number
  productId: number
  productName: string
  productImageUrl?: string
  unitPrice: number
  quantity: number
  subtotal: number
}

export interface OrderDetail extends OrderSummary {
  paidTime?: string
  remark?: string
  items: OrderItem[]
}

export interface RequestLogRow {
  requestId: string
  userId?: number
  username?: string
  requestTime: string
  clientIp?: string
  httpMethod: string
  uri: string
  controllerName?: string
  controllerMethod?: string
  serviceName?: string
  mapperName?: string
  moduleName?: string
  developerName?: string
  httpStatus: number
  success: number
  errorType?: string
  errorMessage?: string
  durationMs: number
}

export interface ErrorLog {
  errorCode: number
  errorType?: string
  errorMessage?: string
  exceptionClass?: string
  stackTrace?: string
  occurredAt: string
}

export interface TraceLayer {
  layerType: string
  layerName: string
  layerMethod: string
  description: string
}

export interface RequestDetail {
  requestLog: RequestLogRow
  layers: TraceLayer[]
  errorLog?: ErrorLog | null
}

export interface DashboardVo {
  totalRequests: number
  todayRequests: number
  todayErrors: number
  onlineUsers: number
  trend: Array<{ date: string; requestCount: number; errorCount: number }>
  moduleMetrics: Array<{
    moduleId: number
    moduleName: string
    developerId: number
    developerName: string
    requestCount: number
    errorCount: number
    avgDurationMs: number
  }>
  apiMetrics: Array<{
    apiId: number
    apiPath: string
    httpMethod: string
    requestCount: number
    errorCount: number
    avgDurationMs: number
  }>
}

export interface UserActivity {
  userId: number
  username: string
  nickname: string
  lastLoginTime?: string
  lastActiveTime?: string
  lastVisitTime?: string
  requestCountToday: number
  requestCountTotal: number
  activeSecondsToday: number
  activeSecondsTotal: number
  activityScore: number
  onlineStatus: number
}

export interface ModuleMonitor {
  moduleId: number
  moduleName: string
  moduleCode: string
  description?: string
  developerId: number
  developerName: string
  employeeNo: string
  requestCount: number
  successCount: number
  errorCount: number
  avgDurationMs: number
  lastActiveTime?: string
}

export interface DeveloperMonitor {
  developerId: number
  name: string
  employeeNo: string
  department: string
  requestCount: number
  errorCount: number
  totalDurationMs: number
  errorRate: number
}

export function fetchHealth() {
  return get<HealthInfo>('/health')
}

export function login(username: string, password: string) {
  return post<LoginResult>('/auth/login', { username, password })
}

export function fetchMe() {
  return get<Profile>('/users/me')
}

export function fetchProducts(params?: Record<string, unknown>) {
  return get<PageResult<Product>>('/products', params)
}

export function fetchProduct(id: number | string) {
  return get<Product>(`/products/${id}`)
}

export function fetchCart() {
  return get<CartItem[]>('/cart/items')
}

export function addToCart(productId: number, quantity = 1) {
  return post<CartItem>('/cart/items', { productId, quantity })
}

export function updateCartQuantity(id: number, quantity: number) {
  return put<void>(`/cart/items/${id}`, { quantity })
}

export function removeCartItem(id: number) {
  return del<void>(`/cart/items/${id}`)
}

export function createOrder(cartItemIds: number[], remark?: string) {
  return post<{ orderId: number; orderNo: string; totalAmount: number }>('/orders', {
    cartItemIds,
    remark,
  })
}

export function fetchOrders(params?: Record<string, unknown>) {
  return get<PageResult<OrderSummary>>('/orders', params)
}

export function fetchOrder(id: number | string) {
  return get<OrderDetail>(`/orders/${id}`)
}

export function payOrder(id: number | string) {
  return post<OrderDetail>(`/orders/${id}/pay`, {})
}

export function fetchDashboard() {
  return get<DashboardVo>('/monitor/dashboard')
}

export function fetchRequestLogs(params?: Record<string, unknown>) {
  return get<PageResult<RequestLogRow>>('/monitor/request-logs', params)
}

export function fetchRequestDetail(requestId: string) {
  return get<RequestDetail>(`/monitor/request-logs/${requestId}`)
}

export function fetchUserActivity(date?: string) {
  return get<UserActivity[]>('/monitor/users/activity', date ? { date } : {})
}

export function fetchModules() {
  return get<ModuleMonitor[]>('/monitor/modules')
}

export function fetchDevelopers() {
  return get<DeveloperMonitor[]>('/monitor/developers')
}

export function simulateError(path: string) {
  return get<void>(`/test/error/${path}`)
}
