import { request } from './request'
import type { UserInfo } from '@/stores/user'

export interface LoginPayload {
  username: string
  password: string
}

export interface LoginResult {
  token: string
  user: UserInfo
}

/** 登录（第二阶段后端实现 /api/auth/login）。 */
export function login(payload: LoginPayload) {
  return request<LoginResult>({ url: '/auth/login', method: 'post', data: payload })
}

/** 获取当前用户（/api/auth/me）。 */
export function getCurrentUser() {
  return request<UserInfo>({ url: '/auth/me', method: 'get' })
}
