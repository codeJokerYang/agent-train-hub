import { defineStore } from 'pinia'
import { ref } from 'vue'

export type UserRole = 'ADMIN' | 'TEACHER' | 'STUDENT'

export interface UserInfo {
  id: number
  username: string
  realName?: string
  role: UserRole
}

const TOKEN_KEY = 'ath_token'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem(TOKEN_KEY) ?? '')
  const userInfo = ref<UserInfo | null>(null)

  function setToken(value: string) {
    token.value = value
    localStorage.setItem(TOKEN_KEY, value)
  }

  function setUserInfo(info: UserInfo | null) {
    userInfo.value = info
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem(TOKEN_KEY)
  }

  return { token, userInfo, setToken, setUserInfo, logout }
})
