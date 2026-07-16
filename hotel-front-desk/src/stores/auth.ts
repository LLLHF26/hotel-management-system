import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { LoginVO } from '@/types'
import { TOKEN_KEY, USER_KEY } from '@/utils/constants'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const user = ref<LoginVO | null>(loadUser())

  function loadUser(): LoginVO | null {
    const raw = localStorage.getItem(USER_KEY)
    if (!raw) return null
    try {
      return JSON.parse(raw) as LoginVO
    } catch {
      return null
    }
  }

  const isLoggedIn = computed(() => !!token.value)
  const displayName = computed(() => user.value?.realName || user.value?.username || '前台')

  function setAuth(data: LoginVO) {
    token.value = data.token
    user.value = data
    localStorage.setItem(TOKEN_KEY, data.token)
    localStorage.setItem(USER_KEY, JSON.stringify(data))
  }

  function clearAuth() {
    token.value = ''
    user.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  return { token, user, isLoggedIn, displayName, setAuth, clearAuth }
})
