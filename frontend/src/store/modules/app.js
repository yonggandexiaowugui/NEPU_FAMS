import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
  state: () => ({
    sidebarCollapsed: false,
    theme: 'light'
  }),

  getters: {
    sidebarWidth: (state) => state.sidebarCollapsed ? '64px' : '220px'
  },

  actions: {
    toggleSidebar() {
      this.sidebarCollapsed = !this.sidebarCollapsed
    },
    setTheme(theme) {
      this.theme = theme
    }
  }
})
