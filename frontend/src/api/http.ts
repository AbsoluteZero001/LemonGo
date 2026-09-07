import axios from 'axios'
import { reactive } from 'vue'

export interface ApiResult<T> {
  code: number
  message: string
  data: T
  requestId: string
  timestamp: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
}

export interface ApiErrorOwner {
  id: number
  name: string
  employeeNo: string
  department: string
}

export interface ApiErrorTrace {
  code: number
  message: string
  errorType: string
  requestId: string
  path: string
  method: string
  controller?: string
  controllerMethod?: string
  service?: string
  mapper?: string
  module?: string
  moduleCode?: string
  owner: ApiErrorOwner | null
  timestamp: string
}

export const errorState = reactive<{
  trace: ApiErrorTrace | null
  message: string
  key: number
}>({
  trace: null,
  message: '',
  key: 0,
})

export class LemonGoError extends Error {
  trace?: ApiErrorTrace

  constructor(message: string, trace?: ApiErrorTrace) {
    super(message)
    this.name = 'LemonGoError'
    this.trace = trace
  }
}

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api',
  timeout: 10_000,
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('lemongo_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const body = response.data as ApiResult<unknown>
    if (body.code === 200) {
      return response
    }
    const trace = body.data as ApiErrorTrace | undefined
    errorState.trace = trace ?? null
    errorState.message = body.message
    errorState.key += 1
    throw new LemonGoError(body.message, trace)
  },
  (error) => {
    const body = error?.response?.data as ApiResult<unknown> | undefined
    const trace = body?.data as ApiErrorTrace | undefined
    if (body) {
      errorState.trace = trace ?? null
      errorState.message = body.message
      errorState.key += 1
    } else {
      errorState.trace = null
      errorState.message = error?.message ?? '网络请求失败'
      errorState.key += 1
    }
    return Promise.reject(error)
  },
)

export async function get<T>(url: string, params?: Record<string, unknown>): Promise<T> {
  const response = await http.get<ApiResult<T>>(url, { params })
  if (response.data.code !== 200) {
    throw new Error(response.data.message || 'request failed')
  }
  return response.data.data
}

export async function post<T>(
  url: string,
  data?: Record<string, unknown>,
): Promise<T> {
  const response = await http.post<ApiResult<T>>(url, data)
  if (response.data.code !== 200) {
    throw new Error(response.data.message || 'request failed')
  }
  return response.data.data
}

export async function put<T>(
  url: string,
  data?: Record<string, unknown>,
): Promise<T> {
  const response = await http.put<ApiResult<T>>(url, data)
  if (response.data.code !== 200) {
    throw new Error(response.data.message || 'request failed')
  }
  return response.data.data
}

export async function del<T>(url: string): Promise<T> {
  const response = await http.delete<ApiResult<T>>(url)
  if (response.data.code !== 200) {
    throw new Error(response.data.message || 'request failed')
  }
  return response.data.data
}

export default http
