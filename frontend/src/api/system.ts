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
  role: string
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
  status?: number
  deleted?: number
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

export interface ApiRegistry {
  id: number
  apiPath: string
  httpMethod: string
  moduleId: number
  moduleName: string
  controllerName?: string
  controllerMethod?: string
  serviceName?: string
  mapperName?: string
  description?: string
  developerId: number
  developerName: string
  status: number
}

export interface ErrorLogRow {
  id: number
  requestId: string
  apiId?: number
  moduleId: number
  moduleName: string
  developerId: number
  developerName: string
  errorCode: number
  errorType?: string
  errorMessage?: string
  exceptionClass?: string
  stackTrace?: string
  occurredAt: string
}

export interface UserAdmin {
  id: number
  username: string
  nickname: string
  email?: string
  phone?: string
  avatarUrl?: string
  status: number
  role: string
  onlineStatus: number
  firstLoginTime?: string
  lastLoginTime?: string
  lastActiveTime?: string
  activityScore: number
  createdAt?: string
}

export interface OrderAdmin {
  id: number
  orderNo: string
  userId: number
  username: string
  totalAmount: number
  orderStatus: string
  payStatus: string
  paymentMethod?: string
  paidTime?: string
  cancelledTime?: string
  finishedTime?: string
  remark?: string
  createdAt: string
}

export interface SysDeveloper {
  id: number
  name: string
  employeeNo: string
  email?: string
  department: string
  status: number
}

export interface SysModule {
  id: number
  moduleName: string
  moduleCode: string
  description?: string
  developerId: number
  status: number
}

export interface SysApi {
  id: number
  apiPath: string
  httpMethod: string
  moduleId: number
  controllerName?: string
  controllerMethod?: string
  serviceName?: string
  mapperName?: string
  description?: string
  developerId: number
  status: number
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

export function fetchCategories() {
  return get<string[]>('/products/categories')
}

export function fetchMeActivity() {
  return get<UserActivity>('/users/me/activity')
}

export function fetchApiRegistry() {
  return get<ApiRegistry[]>('/monitor/apis')
}

export function fetchErrorLogs(params?: Record<string, unknown>) {
  return get<PageResult<ErrorLogRow>>('/monitor/errors', params)
}

export function adminProducts(params?: Record<string, unknown>) {
  return get<PageResult<Product>>('/admin/products', params)
}

export function adminCreateProduct(data: Record<string, unknown>) {
  return post<Product>('/admin/products', data)
}

export function adminUpdateProduct(id: number, data: Record<string, unknown>) {
  return put<Product>(`/admin/products/${id}`, data)
}

export function adminDeleteProduct(id: number) {
  return del<void>(`/admin/products/${id}`)
}

export function adminRestoreProduct(id: number) {
  return put<Product>(`/admin/products/${id}/restore`)
}

export function adminUsers(params?: Record<string, unknown>) {
  return get<PageResult<UserAdmin>>('/admin/users', params)
}

export function adminCreateUser(data: Record<string, unknown>) {
  return post<UserAdmin>('/admin/users', data)
}

export function adminUpdateUser(id: number, data: Record<string, unknown>) {
  return put<UserAdmin>(`/admin/users/${id}`, data)
}

export function adminOrders(params?: Record<string, unknown>) {
  return get<PageResult<OrderAdmin>>('/admin/orders', params)
}

export function adminUpdateOrderStatus(id: number, data: Record<string, unknown>) {
  return put<OrderAdmin>(`/admin/orders/${id}/status`, data)
}

export function adminDevelopers(params?: Record<string, unknown>) {
  return get<PageResult<SysDeveloper>>('/admin/developers', params)
}

export function adminCreateDeveloper(data: Record<string, unknown>) {
  return post<SysDeveloper>('/admin/developers', data)
}

export function adminUpdateDeveloper(id: number, data: Record<string, unknown>) {
  return put<SysDeveloper>(`/admin/developers/${id}`, data)
}

export function adminDeleteDeveloper(id: number) {
  return del<void>(`/admin/developers/${id}`)
}

export function adminModules(params?: Record<string, unknown>) {
  return get<PageResult<SysModule>>('/admin/modules', params)
}

export function adminCreateModule(data: Record<string, unknown>) {
  return post<SysModule>('/admin/modules', data)
}

export function adminUpdateModule(id: number, data: Record<string, unknown>) {
  return put<SysModule>(`/admin/modules/${id}`, data)
}

export function adminDeleteModule(id: number) {
  return del<void>(`/admin/modules/${id}`)
}

export function adminApis(params?: Record<string, unknown>) {
  return get<PageResult<SysApi>>('/admin/apis', params)
}

export function adminCreateApi(data: Record<string, unknown>) {
  return post<SysApi>('/admin/apis', data)
}

export function adminUpdateApi(id: number, data: Record<string, unknown>) {
  return put<SysApi>(`/admin/apis/${id}`, data)
}

export function adminDeleteApi(id: number) {
  return del<void>(`/admin/apis/${id}`)
}

export function adminRefreshApis() {
  return post<string>('/admin/apis/refresh')
}
