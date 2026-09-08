import axios from 'axios'
import { ElMessage } from 'element-plus'

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

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api',
  timeout: 10_000,
})

http.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('lemongo_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

function notifyError(message: string) {
  ElMessage.error(message || '请求失败')
}

function resolveErrorMessage(error: unknown): string {
  const err = error as {
    response?: { data?: ApiResult<unknown> }
    code?: string
    message?: string
  }
  const body = err?.response?.data
  if (body?.message) {
    return body.message
  }
  if (err?.code === 'ECONNABORTED') {
    return '请求超时，请稍后重试'
  }
  if (err?.message === 'Network Error') {
    return '网络连接失败，请检查网络'
  }
  return '网络请求失败'
}

http.interceptors.response.use(
  (response) => {
    const body = response.data as ApiResult<unknown>
    if (body.code === 200) {
      return response
    }
    notifyError(body.message)
    return Promise.reject(new Error(body.message))
  },
  (error) => {
    notifyError(resolveErrorMessage(error))
    return Promise.reject(error)
  },
)

export async function get<T>(url: string, params?: Record<string, unknown>): Promise<T> {
  const response = await http.get<ApiResult<T>>(url, { params })
  return response.data.data
}

export async function post<T>(
  url: string,
  data?: Record<string, unknown>,
): Promise<T> {
  const response = await http.post<ApiResult<T>>(url, data)
  return response.data.data
}

export async function put<T>(
  url: string,
  data?: Record<string, unknown>,
): Promise<T> {
  const response = await http.put<ApiResult<T>>(url, data)
  return response.data.data
}

export async function del<T>(url: string): Promise<T> {
  const response = await http.delete<ApiResult<T>>(url)
  return response.data.data
}

export default http
