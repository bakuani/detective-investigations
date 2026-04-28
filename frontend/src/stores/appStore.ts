import { defineStore } from 'pinia'
import { apiClient } from '../api/client'
import type { User } from '../data/mockData'

export type AppPage =
  | 'home'
  | 'catalog'
  | 'case-detail'
  | 'my-cases'
  | 'profile'
  | 'create-case'
  | 'admin'
  | 'moderation'
  | 'leaderboard'
  | 'auth'
  | 'back'

export const useAppStore = defineStore('app', {
  state: () => ({
    currentUser: null as User | null,
    currentPage: 'home' as AppPage,
    activeCaseId: null as string | null,
    authMode: 'login' as 'login' | 'register' | 'recover',
    isAppLoaded: false,
    pageHistory: [] as string[],
  }),
  actions: {
    async fetchCurrentUser() {
      const token = localStorage.getItem('token');
      if (!token) {
        this.currentUser = null;
        this.isAppLoaded = true;
        return;
      }

      try {
        const response = await apiClient.get('/users/me');
        this.currentUser = response.data;
      } catch (err) {
        localStorage.removeItem('token');
        this.currentUser = null;
      } finally {
        this.isAppLoaded = true;
      }
    },
    navigate(page: AppPage, caseId?: string) {
      if (page === 'back') {
        if (this.pageHistory.length > 0) {
          this.currentPage = this.pageHistory.pop() as AppPage;
        } else {
          this.currentPage = 'home';
        }
        return;
      }
      if (this.currentPage && this.currentPage !== page && this.currentPage !== 'auth') {
        this.pageHistory.push(this.currentPage);
      }
      if (caseId !== undefined) {
        this.activeCaseId = caseId
      }
      this.currentPage = page
      console.log(`[Navigation] → ${page}${caseId ? ` (caseId: ${caseId})` : ''}`)
    },
    setUser(user: User | null) {
      this.currentUser = user
    },
    setAuthMode(mode: 'login' | 'register' | 'recover') {
      this.authMode = mode
    },
    logout() {
      localStorage.removeItem('token');
      this.currentUser = null;
      this.navigate('auth');
    }
  }
})
