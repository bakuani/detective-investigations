<template>
  <div data-cmp="ProfilePage" class="max-w-[1440px] mx-auto px-6 py-10">
    <div class="flex gap-8">
      <!-- Left — profile -->
      <div class="w-80 shrink-0 flex flex-col gap-4">
        <!-- Avatar & info -->
        <div class="bg-card border border-border rounded-xl p-6">
          <div v-if="!user" class="text-center py-10">
            <p class="text-muted-foreground">Пользователь не найден</p>
          </div>
          
          <template v-else>
            <div class="flex flex-col items-center text-center mb-4">
              <div class="w-20 h-20 rounded-2xl bg-primary/10 border-2 border-primary/30 flex items-center justify-center mb-3">
                <span class="text-3xl font-bold text-primary">{{ user.nick[0] }}</span>
              </div>
              <h1 class="text-xl font-bold text-foreground">{{ user.nick }}</h1>
              <div class="flex items-center gap-2 mt-2">
                <span :class="`text-xs px-2.5 py-1 rounded-full border ${
                  user.role === 'admin' ? 'border-destructive/40 bg-destructive/10 text-destructive' :
                  user.role === 'moderator' ? 'border-warning/40 bg-warning/10 text-warning' :
                  'border-primary/30 bg-primary/10 text-primary'
                }`">
                  {{ user.role === 'admin' ? 'Администратор' : user.role === 'moderator' ? 'Модератор' : 'Детектив' }}
                </span>
              </div>
            </div>

            <!-- Rank progress -->
            <div class="bg-muted/50 rounded-xl p-3 mb-4">
              <div class="flex items-center justify-between mb-1.5">
                <span class="text-xs font-medium text-foreground">{{ currentRank.title }}</span>
                <span v-if="nextRank" class="text-xs text-muted-foreground">{{ nextRank.title }}</span>
              </div>
              <div class="h-2 bg-muted rounded-full overflow-hidden mb-1.5">
                <div class="h-full bg-primary rounded-full transition-all" :style="{ width: `${xpProgress}%` }" />
              </div>
              <div class="flex justify-between text-[10px] text-muted-foreground">
                <span>{{ user.xp }} XP</span>
                <span v-if="nextRank">ещё {{ xpToNext }} XP</span>
              </div>
            </div>

            <div class="flex flex-col gap-2 text-sm">
              <div class="flex justify-between">
                <span class="text-muted-foreground">Рейтинг</span>
                <span class="font-bold text-primary">{{ user.rating.toLocaleString() }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-muted-foreground">XP</span>
                <span class="font-medium text-foreground">{{ user.xp.toLocaleString() }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-muted-foreground">На платформе с</span>
                <span class="text-foreground">{{ formatDate(user.joinedAt) }}</span>
              </div>
            </div>
          </template>
        </div>

        <!-- Stats -->
        <div v-if="user" class="bg-card border border-border rounded-xl p-5">
          <h3 class="font-semibold text-foreground mb-4 text-sm">Статистика</h3>
          <div class="flex flex-col gap-3">
            <div v-for="(s, i) in stats" :key="i" class="flex items-center gap-3">
              <div :class="`w-7 h-7 rounded-lg bg-muted flex items-center justify-center ${s.color}`">
                <component :is="s.icon" :size="14" />
              </div>
              <span class="flex-1 text-sm text-muted-foreground">{{ s.label }}</span>
              <span class="font-semibold text-foreground">{{ s.value }}</span>
            </div>
          </div>
        </div>

        <button v-if="isOwnProfile && user" class="w-full py-2.5 border border-border rounded-xl text-sm font-medium text-muted-foreground hover:text-foreground hover:border-primary/40 transition-all">
          Редактировать профиль
        </button>
      </div>

      <!-- Right — history + cases -->
      <div v-if="user" class="flex-1 flex flex-col gap-6">
        <!-- Recent solutions -->
        <div class="bg-card border border-border rounded-xl p-5">
          <div class="flex items-center justify-between mb-4">
            <h2 class="font-semibold text-foreground flex items-center gap-2">
              <Trophy :size="16" class="text-primary" /> История решений
            </h2>
          </div>
          <p v-if="userSolutions.length === 0" class="text-sm text-muted-foreground text-center py-6">
            Нет сданных решений
          </p>
          <div v-else class="flex flex-col gap-3">
            <div
              v-for="sol in userSolutions"
              :key="sol.id"
              @click="onNavigate('case-detail', sol.caseId)"
              class="flex items-center gap-4 p-4 bg-muted/30 border border-border rounded-xl hover:border-primary/30 cursor-pointer transition-colors"
            >
              <div :class="`w-8 h-8 rounded-lg flex items-center justify-center font-bold text-sm shrink-0 ${
                sol.place === 1 ? 'bg-primary/20 text-primary' : 'bg-muted text-muted-foreground'
              }`">
                {{ sol.place === 1 ? '🏆' : sol.place ?? '—' }}
              </div>
              <div class="flex-1 min-w-0">
                <p class="text-sm font-medium text-foreground truncate">{{ findCaseTitle(sol.caseId) ?? 'Неизвестное дело' }}</p>
                <p class="text-xs text-muted-foreground">{{ sol.culprit }} · {{ sol.motive }}</p>
              </div>
              <div v-if="sol.score !== undefined" class="text-right">
                <p :class="`text-base font-bold ${sol.place === 1 ? 'text-primary' : 'text-foreground'}`">
                  {{ sol.score }}
                </p>
                <p class="text-[10px] text-muted-foreground">баллов</p>
              </div>
              <span v-else class="text-xs text-warning bg-warning/10 border border-warning/20 px-2 py-0.5 rounded-full">
                Ожидает
              </span>
              <ChevronRight :size="14" class="text-muted-foreground" />
            </div>
          </div>
        </div>

        <!-- XP History -->
        <div class="bg-card border border-border rounded-xl p-5">
          <div class="flex items-center justify-between mb-4">
            <h2 class="font-semibold text-foreground flex items-center gap-2">
              <Star :size="16" class="text-primary" /> История XP
            </h2>
            <div class="flex gap-1 bg-muted rounded-lg p-0.5">
              <button
                v-for="t in [{ id: 'all', label: 'Все' }, { id: 'bonus', label: 'Бонусы' }]"
                :key="t.id"
                @click="xpTab = t.id as 'all' | 'bonus'"
                :class="`px-3 py-1 rounded-md text-xs font-medium transition-all ${
                  xpTab === t.id ? 'bg-card text-foreground' : 'text-muted-foreground hover:text-foreground'
                }`"
              >
                {{ t.label }}
              </button>
            </div>
          </div>
          <p v-if="filteredXP.length === 0" class="text-sm text-muted-foreground text-center py-6">
            Нет записей
          </p>
          <div v-else class="flex flex-col gap-2">
            <div
              v-for="entry in filteredXP"
              :key="entry.id"
              class="flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-muted/30 transition-colors"
            >
              <div :class="`w-2 h-2 rounded-full shrink-0 ${XP_TYPE_COLORS[entry.type] || 'text-muted-foreground'} bg-current`" />
              <div class="flex-1 min-w-0">
                <p class="text-sm text-foreground">{{ XP_TYPE_LABELS[entry.type] || entry.type }}</p>
                <p v-if="entry.caseId" class="text-xs text-muted-foreground truncate">
                  {{ findCaseTitle(entry.caseId) }}
                </p>
              </div>
              <span :class="`text-sm font-bold ${XP_TYPE_COLORS[entry.type] || 'text-foreground'}`">
                +{{ entry.xpGained }}
              </span>
              <span class="text-[10px] text-muted-foreground ml-2">{{ formatDate(entry.date) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { Trophy, TrendingUp, BookOpen, Star, ChevronRight, BarChart2, Award } from 'lucide-vue-next'
import { MOCK_USERS, MOCK_CASES, MOCK_SOLUTIONS, MOCK_XP_HISTORY } from '../data/mockData'
import type { User } from '../data/mockData'

interface Props {
  profileUserId?: string
  currentUser?: User | null
}

const props = withDefaults(defineProps<Props>(), {
  profileUserId: '',
  currentUser: null,
})

const emit = defineEmits(['navigate'])

const xpTab = ref<'all' | 'bonus'>('all')

const userId = computed(() => props.profileUserId || props.currentUser?.id || MOCK_USERS[0].id)

const user = computed(() => MOCK_USERS.find(u => u.id === userId.value) ?? (userId.value ? null : MOCK_USERS[0]))

const isOwnProfile = computed(() => props.currentUser?.id === user.value?.id)

const userSolutions = computed(() => 
  user.value ? MOCK_SOLUTIONS.filter(s => s.userId === user.value!.id) : []
)

const userXP = computed(() => 
  user.value ? MOCK_XP_HISTORY.filter(x => x.userId === user.value!.id) : []
)

const filteredXP = computed(() => 
  xpTab.value === 'bonus' ? userXP.value.filter(x => x.type === 'bonus') : userXP.value
)

const solvedCases = computed(() => 
  userSolutions.value.filter(s => s.place === 1)
)

const avgScore = computed(() => {
  const scored = userSolutions.value.filter(s => s.score)
  if (scored.length === 0) return 0
  return Math.round(scored.reduce((sum, s) => sum + (s.score ?? 0), 0) / scored.length)
})

const RANK_THRESHOLDS = [
  { title: 'Новичок', minXP: 0 },
  { title: 'Детектив', minXP: 200 },
  { title: 'Инспектор', minXP: 600 },
  { title: 'Следователь', minXP: 1400 },
  { title: 'Суперинтендант', minXP: 3000 },
]

const currentRankIdx = computed(() => {
  if (!user.value) return 0
  return RANK_THRESHOLDS.findIndex((r, i) => {
    const next = RANK_THRESHOLDS[i + 1]
    return user.value!.xp >= r.minXP && (!next || user.value!.xp < next.minXP)
  })
})

const currentRank = computed(() => 
  RANK_THRESHOLDS[currentRankIdx.value >= 0 ? currentRankIdx.value : 0]
)

const nextRank = computed(() => 
  RANK_THRESHOLDS[currentRankIdx.value + 1]
)

const xpToNext = computed(() => 
  nextRank.value ? nextRank.value.minXP - (user.value?.xp ?? 0) : 0
)

const xpProgress = computed(() => {
  if (!nextRank.value || !user.value) return 100
  return ((user.value.xp - currentRank.value.minXP) / (nextRank.value.minXP - currentRank.value.minXP)) * 100
})

const XP_TYPE_LABELS: Record<string, string> = {
  case_solved: 'Раскрыто дело',
  first_place: '1-е место',
  bonus: 'Бонус',
  participation: 'Участие',
  daily: 'Ежедневный бонус',
}

const XP_TYPE_COLORS: Record<string, string> = {
  case_solved: 'text-chart-1',
  first_place: 'text-primary',
  bonus: 'text-chart-5',
  participation: 'text-chart-2',
  daily: 'text-chart-3',
}

const stats = computed(() => {
  if (!user.value) return []
  return [
    { label: 'Всего дел', value: user.value.casesPlayed, icon: BookOpen, color: 'text-chart-2' },
    { label: 'Раскрыто', value: user.value.casesSolved, icon: Trophy, color: 'text-primary' },
    { label: 'Побед (1 место)', value: solvedCases.value.length, icon: Award, color: 'text-chart-1' },
    { label: 'Средний балл', value: avgScore.value || '—', icon: BarChart2, color: 'text-chart-5' },
  ]
})

function formatDate(dateStr: string): string {
  return new Date(dateStr).toLocaleDateString('ru-RU', { day: '2-digit', month: 'short', year: 'numeric' })
}

function findCaseTitle(caseId: string): string | undefined {
  return MOCK_CASES.find(c => c.id === caseId)?.title
}

function onNavigate(page: string, caseId?: string): void {
  emit('navigate', page, caseId)
}
</script>