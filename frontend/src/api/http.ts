import axios from 'axios'

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
  const token = localStorage.getItem('lemongo_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

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

export default http

