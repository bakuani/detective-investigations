<template>
  <header class="sticky top-0 z-50 w-full border-b border-border bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
    <div class="max-w-[1440px] mx-auto px-6 h-16 flex items-center justify-between">
      <!-- Logo -->
      <div 
        @click="$emit('navigate', 'home')"
        class="flex items-center gap-3 cursor-pointer group"
      >
        <div class="w-8 h-8 bg-primary rounded-lg flex items-center justify-center text-primary-foreground font-black text-xl shadow-custom transform group-hover:scale-105 transition-transform">
          D
        </div>
        <span class="font-bold text-lg tracking-tight text-foreground hidden sm:inline-block">
          Detective<span class="text-primary tracking-normal">Net</span>
        </span>
      </div>

      <!-- Navigation -->
      <nav class="hidden md:flex flex-1 items-center justify-center gap-1">
        <button
          v-for="nav in navItems"
          :key="nav.page"
          @click="$emit('navigate', nav.page)"
          :class="`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
            currentPage === nav.page 
              ? 'bg-accent/50 text-foreground' 
              : 'text-muted-foreground hover:bg-muted hover:text-foreground'
          }`"
        >
          <div class="flex items-center gap-2 relative">
            <component :is="nav.icon" :size="16" :class="currentPage === nav.page ? 'text-primary' : ''" />
            {{ nav.label }}
            <span v-if="nav.badge && nav.badge > 0" class="absolute -top-1 -right-4 w-4 h-4 rounded-full bg-primary flex items-center justify-center text-[9px] font-bold text-primary-foreground">
              {{ nav.badge > 99 ? '99+' : nav.badge }}
            </span>
          </div>
        </button>
      </nav>

      <!-- Right Section -->
      <div class="flex items-center gap-4">
        <!-- Create button (desktop) -->
        <button
          v-if="currentUser"
          @click="$emit('navigate', 'create-case')"
          class="hidden sm:flex items-center gap-2 px-3 py-1.5 bg-primary/10 text-primary border border-primary/20 hover:bg-primary hover:text-primary-foreground hover:border-primary rounded-lg text-sm font-medium transition-all"
        >
          <Plus :size="16" />
          <span>Новое дело</span>
        </button>

        <div class="h-6 w-px bg-border mx-2 hidden sm:block"></div>

        <!-- User Menu & Notifications -->
        <div v-if="currentUser" class="flex items-center gap-4">
            <!-- Notifications Overlay -->
            <div class="relative">
              <button @click="showNotifications = !showNotifications" class="relative p-2 text-muted-foreground hover:text-foreground transition-colors rounded-full hover:bg-secondary">
                <BellIcon :size="20" />
                <span v-if="unreadCount > 0" class="absolute top-0 right-0 h-4 w-4 bg-destructive text-[10px] font-bold text-destructive-foreground rounded-full flex items-center justify-center border-2 border-background">{{ unreadCount }}</span>
              </button>
              
              <!-- Dropdown -->
              <div v-if="showNotifications" class="absolute right-0 mt-2 w-80 bg-background border border-border rounded-xl shadow-lg z-50 overflow-hidden">
                <div class="p-3 border-b border-border flex justify-between items-center bg-muted/30">
                  <h3 class="font-semibold text-sm">Уведомления</h3>
                  <button @click="markAllRead" v-if="unreadCount > 0" class="text-xs text-primary hover:underline">Прочитать все</button>
                </div>
                <div class="max-h-[300px] overflow-y-auto">
                  <div v-if="notifications.length === 0" class="p-4 text-center text-sm text-muted-foreground">
                    Нет новых уведомлений
                  </div>
                  <div v-for="notif in notifications" :key="notif.id" 
                       class="p-3 border-b border-border last:border-b-0 hover:bg-muted/50 transition-colors flex gap-3 items-start"
                       :class="{ 'bg-primary/5': !notif.isRead }">
                    <div class="shrink-0 mt-1" :class="getNotificationColor(notif.type)">
                      <component :is="getNotificationIcon(notif.type)" :size="16" />
                    </div>
                    <div class="flex-1 min-w-0">
                      <p class="text-sm font-medium leading-none mb-1">{{ notif.title }}</p>
                      <p class="text-xs text-muted-foreground break-words line-clamp-2">{{ notif.message }}</p>
                      <p class="text-[10px] text-muted-foreground mt-2">{{ notif.date }}</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Profile & Logout -->
            <div class="flex gap-2">
              <button @click="$emit('navigate', 'profile')" class="text-sm font-medium hover:text-primary transition-colors flex gap-2 items-center">
                  <UserIcon :size="16" />
                  {{ currentUser.nick }}
              </button>
              <button @click="$emit('setUser', null)" class="text-xs text-muted-foreground hover:text-destructive">
                  Выйти
              </button>
            </div>
        </div>

        <button 
          v-else
          @click="$emit('navigate', 'auth')"
          class="px-4 py-2 bg-primary text-primary-foreground hover:opacity-90 rounded-lg text-sm font-medium transition-opacity"
        >
          Войти
        </button>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted, watch } from 'vue'
import { Search, FolderOpen, Users, LogIn, LogOut, Shield, Crown, User as UserIcon, Settings, Plus, Bell as BellIcon, CheckCircle, AlertCircle, Info, MessageSquare } from 'lucide-vue-next'
import { MOCK_CASES, type User } from '../../data/mockData'
import { stompClient, onStompConnect } from '../../api/websocket'

const props = defineProps<{
  currentUser: User | null
  currentPage: string
}>()

defineEmits(['navigate', 'setUser'])

// --- Notifications Logic ---
const showNotifications = ref(false)

interface Notification {
  id: string
  title: string
  message: string
  type: 'info' | 'success' | 'warning' | 'message'
  date: string
  isRead: boolean
}

// Starting with an empty array. Server will push real-time updates.
const notifications = ref<Notification[]>([])

const subscribeToNotifications = () => {
  if (!props.currentUser?.id || !stompClient.connected) return;
  stompClient.subscribe(`/topic/user/${props.currentUser.id}`, (message) => {
    const notifData = JSON.parse(message.body);
    notifications.value.unshift({
      id: notifData.id,
      title: notifData.title,
      message: notifData.message,
      type: notifData.type as any,
      date: notifData.date,
      isRead: false
    });
  });
}

onStompConnect(() => {
    if (props.currentUser?.id) {
      subscribeToNotifications();
    }
})

watch(() => props.currentUser, (newUser) => {
  if (newUser?.id) {
    if (!stompClient.active) stompClient.activate();
    else subscribeToNotifications();
  } else {
    if (stompClient.active) stompClient.deactivate();
    notifications.value = [];
  }
}, { immediate: true })

onUnmounted(() => {
  if (stompClient.active) stompClient.deactivate();
})

const unreadCount = computed(() => notifications.value.filter(n => !n.isRead).length)

const markAllRead = () => {
  notifications.value.forEach(n => n.isRead = true)
}

const getNotificationIcon = (type: string) => {
  switch (type) {
    case 'success': return CheckCircle
    case 'warning': return AlertCircle
    case 'message': return MessageSquare
    default: return Info
  }
}

const getNotificationColor = (type: string) => {
  switch (type) {
    case 'success': return 'text-green-500'
    case 'warning': return 'text-yellow-500'
    case 'message': return 'text-blue-500'
    default: return 'text-primary'
  }
}

const closeNotificationsMenu = (e: MouseEvent) => {
  const target = e.target as HTMLElement
  if (showNotifications.value && !target.closest('.relative')) {
    showNotifications.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', closeNotificationsMenu)
})

onUnmounted(() => { if(stompClient.active)stompClient.deactivate(); document.removeEventListener('click', closeNotificationsMenu); });
// ---------------------------

const urgentCasesCount = computed(() => {
  return MOCK_CASES.filter(c => c.status === 'active' && new Date(c.deadline).getTime() < Date.now() + 24 * 60 * 60 * 1000).length
})

const navItems = computed(() => {
  const items = [
    { page: 'catalog', label: 'Каталог', icon: Search, badge: 0 },
    { page: 'leaderboard', label: 'Рейтинг', icon: Crown, badge: 0 },
  ]
  if (props.currentUser) {
    items.splice(1, 0, { page: 'my-cases', label: 'Мои дела', icon: FolderOpen, badge: urgentCasesCount.value })
  }
  if (props.currentUser?.role === 'moderator' || props.currentUser?.role === 'admin') {
    items.push({ page: 'moderation', label: 'Модерация', icon: Shield, badge: 3 })
  }
  if (props.currentUser?.role === 'admin') {
    items.push({ page: 'admin', label: 'Панель', icon: Settings, badge: 0 })
  }
  return items
})
</script>