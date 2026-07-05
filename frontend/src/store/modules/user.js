import { defineStore } from 'pinia'
import { login, logout, getCurrentUser } from '@/api/auth'
import { getToken, setToken, removeToken, getUserInfo, setUserInfo, removeUserInfo, clearAuth } from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    userInfo: getUserInfo() || null,
    roles: []
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    userName: (state) => state.userInfo?.username || '',
    userRole: (state) => state.userInfo?.role || '',
    userId: (state) => state.userInfo?.id || null
  },

  actions: {
    async login(loginData) {
      try {
        const res = await login(loginData)
        this.token = res.token
        this.userInfo = res.userInfo || res
        this.roles = res.roles || [res.userInfo?.role || 'USER']
        setToken(this.token)
        setUserInfo(this.userInfo)
        return res
      } catch (error) {
        throw error
      }
    },

    async fetchUserInfo() {
      try {
        const res = await getCurrentUser()
        this.userInfo = res
        this.roles = [res.role || 'USER']
        setUserInfo(res)
        return res
      } catch (error) {
        throw error
      }
    },

    async logout() {
      try {
        await logout()
      } catch (error) {
        console.error('Logout error:', error)
      } finally {
        this.token = ''
        this.userInfo = null
        this.roles = []
        clearAuth()
      }
    },

    updateUserInfo(info) {
      this.userInfo = { ...this.userInfo, ...info }
      setUserInfo(this.userInfo)
    },

    resetState() {
      this.token = ''
      this.userInfo = null
      this.roles = []
      clearAuth()
    }
  }
})
