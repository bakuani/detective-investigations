<template>
  <div data-cmp="App" class="min-h-screen bg-background">
    <Header
      :current-user="currentUser"
      :current-page="currentPage"
      @navigate="navigate"
      @set-user="setUser"
    />

    <main>
      <HomePage 
        v-if="currentPage === 'home'" 
        :current-user="currentUser" 
        @navigate="navigate" 
      />
      
      <CatalogPage 
        v-else-if="currentPage === 'catalog'"
        :current-user="currentUser" 
        @navigate="navigate" 
      />

      <AuthPage 
        v-else-if="currentPage === 'auth'"
        @navigate="navigate"
        @login="handleLogin"
      />

      <MyCasesPage 
        v-else-if="currentPage === 'my-cases'"
        :current-user="currentUser"
        @navigate="navigate"
      />

      <CaseDetailPage
        v-else-if="currentPage === 'case-detail'"
        :current-user="currentUser"
        :case-id="currentCaseId"
        @navigate="navigate"
      />
      <CreateCasePage
        v-else-if="currentPage === 'create-case'"
        :current-user="currentUser"
        @navigate="navigate"
      />
      <ProfilePage
        v-else-if="currentPage === 'profile'"
        :current-user="currentUser"
        :profile-user-id="profileUserId"
        @navigate="navigate"
      />
      <LeaderboardPage
        v-else-if="currentPage === 'leaderboard'"
        :current-user="currentUser"
        @navigate="navigate"
      />
      <ModerationPage
        v-else-if="currentPage === 'moderation'"
        :current-user="currentUser"
        @navigate="navigate"
      />
      <AdminPage
        v-else-if="currentPage === 'admin'"
        :current-user="currentUser"
        @navigate="navigate"
      />
      <NotFound
        v-else
        @navigate="navigate"
      />
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAppStore } from './stores/appStore'
import { MOCK_USERS } from './data/mockData'
import Header from './components/Layout/Header.vue'
import HomePage from './pages/HomePage.vue'
import CatalogPage from './pages/CatalogPage.vue'
import AuthPage from './pages/AuthPage.vue'
import MyCasesPage from './pages/MyCasesPage.vue'
import CaseDetailPage from './pages/CaseDetailPage.vue';
import CreateCasePage from './pages/CreateCasePage.vue';
import ProfilePage from './pages/ProfilePage.vue';
import LeaderboardPage from './pages/LeaderboardPage.vue';
import ModerationPage from './pages/ModerationPage.vue';
import AdminPage from './pages/AdminPage.vue';
import NotFound from './pages/NotFound.vue';
const appStore = useAppStore()

const currentUser = computed(() => appStore.currentUser)
const currentPage = computed(() => appStore.currentPage)

const navigate = (page: string, caseId?: string) => {
  appStore.navigate(page as any, caseId)
}

const setUser = (user: any) => {
  appStore.setUser(user)
}

const handleLogin = () => {
  appStore.setUser(MOCK_USERS[0]) // mock login action as per your React original design
}
</script> 
