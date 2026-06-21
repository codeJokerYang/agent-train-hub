import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'

/** 后端统一响应结构 Result<T>。 */
export interface ApiResult<T = unknown> {
  code: number
  message: string
  data: T
}

const service: AxiosInstance = axios.create({
  baseURL: (import.meta.env.VITE_API_BASE_URL as string | undefined) ?? '/api',
  timeout: 15000
})

// 请求拦截：自动携带 token
service.interceptors.request.use((config) => {
  const token = localStorage.getItem('ath_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截：统一处理业务错误码与 401
service.interceptors.response.use(
  (response: AxiosResponse<ApiResult>) => {
    const body = response.data
    if (body && typeof body.code === 'number' && body.code !== 0) {
      ElMessage.error(body.message || '请求失败')
      return Promise.reject(new Error(body.message || 'request failed'))
    }
    return response
  },
  (error) => {
    const status = error?.response?.status
    if (status === 401) {
      localStorage.removeItem('ath_token')
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    const message = error?.response?.data?.message || error.message || '网络错误'
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

/** 通用请求，自动解包 Result.data。 */
export function request<T = unknown>(config: AxiosRequestConfig): Promise<T> {
  return service.request<ApiResult<T>>(config).then((res) => res.data.data)
}

export default service
