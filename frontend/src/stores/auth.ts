import { defineStore } from 'pinia'
import { fetchMe, login as loginRequest } from '@/api/system'
import type { Profile } from '@/api/system'

const TOKEN_KEY = 'lemongo_token'
const PROFILE_KEY = 'lemongo_profile'

function readProfile(): Profile | null {
  try {
    const raw = localStorage.getItem(PROFILE_KEY)
    return raw ? (JSON.parse(raw) as Profile) : null
  } catch {
    return null
  }
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) ?? '',
    profile: readProfile(),
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token && state.profile),
    role: (state) => state.profile?.role ?? 'USER',
    isAdmin: (state) => state.profile?.role === 'ADMIN',
    isMonitor: (state) => state.profile?.role === 'MONITOR',
  },
  actions: {
    async login(username: string, password: string) {
      const result = await loginRequest(username, password)
      this.token = result.token
      this.profile = result.profile
      localStorage.setItem(TOKEN_KEY, result.token)
      localStorage.setItem(PROFILE_KEY, JSON.stringify(result.profile))
    },
    async refreshProfile() {
      if (!this.token) {
        return
      }
      const profile = await fetchMe()
      this.profile = profile
      localStorage.setItem(PROFILE_KEY, JSON.stringify(profile))
    },
    logout() {
      this.token = ''
      this.profile = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(PROFILE_KEY)
    },
  },
})
