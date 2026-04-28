<template>
  <div data-cmp="LeaderboardPage" class="max-w-[1440px] mx-auto px-6 py-10">
    <!-- Header -->
    <div class="mb-8">
      <p class="text-xs font-medium text-primary uppercase tracking-widest mb-2">Рейтинг</p>
      <h1 class="text-3xl font-bold text-foreground">Таблица лидеров</h1>
      <p class="text-muted-foreground mt-1">Лучшие детективы платформы</p>
    </div>

    <!-- Top 3 podium -->
    <div class="flex gap-4 mb-10">
      <div
        v-for="(user, idx) in sorted.slice(0, 3)"
        :key="user.id"
        :class="`flex-1 border-2 rounded-2xl p-5 text-center transition-all ${
          idx === 0 ? 'border-primary/50 bg-primary/5 card-glow-primary' : RANK_STYLES[idx]
        }`"
      >
        <div class="text-3xl mb-2">{{ RANK_ICONS[idx] }}</div>
        <div class="w-12 h-12 rounded-xl bg-primary/10 border border-primary/20 flex items-center justify-center mx-auto mb-2">
          <span class="text-lg font-bold text-primary">{{ (user.username || user.nick || 'Аноним')[0] }}</span>
        </div>
        <p :class="`font-bold text-sm mb-1 ${idx === 0 ? 'text-primary' : 'text-foreground'}`">
          {{ (user.username || user.nick || 'Аноним') }}
        </p>
        <p :class="`text-xl font-bold ${idx === 0 ? 'text-primary' : 'text-foreground'}`">
          {{
            sortKey === 'rating'
              ? user.rating.toLocaleString()
              : sortKey === 'xp'
                ? `${user.xp.toLocaleString()} XP`
                : sortKey === 'solved'
                  ? `${user.casesSolved} дел`
                  : `${(user as any).avgScore} б`
          }}
        </p>
        <div class="flex items-center justify-center gap-3 mt-2 text-xs text-muted-foreground">
          <span>{{ user.casesSolved }} раскрыто</span>
          <span>·</span>
          <span>{{ (user as any).wins }} побед</span>
        </div>
      </div>
    </div>

    <!-- Filters -->
    <div class="flex items-center gap-4 mb-6 flex-wrap">
      <!-- Search -->
      <div class="relative">
        <Search :size="14" class="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
        <input
          v-model="search"
          placeholder="Поиск детектива..."
          class="pl-9 pr-3 py-2 bg-input border border-border rounded-xl text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary/50 w-48"
        />
      </div>

      <!-- Period -->
      <div class="flex gap-1 bg-muted rounded-xl p-1">
        <button
          v-for="p in PERIOD_OPTIONS"
          :key="p.key"
          @click="period = p.key as Period"
          :class="`px-3 py-1.5 rounded-lg text-xs font-medium transition-all ${
            period === p.key ? 'bg-card text-foreground' : 'text-muted-foreground hover:text-foreground'
          }`"
        >
          {{ p.label }}
        </button>
      </div>

      <!-- Sort -->
      <div class="flex items-center gap-2 ml-auto">
        <Filter :size="14" class="text-muted-foreground" />
        <span class="text-xs text-muted-foreground">Сортировка:</span>
        <div class="flex gap-1 bg-muted rounded-xl p-1">
          <button
            v-for="s in SORT_OPTIONS"
            :key="s.key"
            @click="sortKey = s.key as SortKey"
            :class="`px-3 py-1.5 rounded-lg text-xs font-medium transition-all ${
              sortKey === s.key ? 'bg-card text-foreground' : 'text-muted-foreground hover:text-foreground'
            }`"
          >
            {{ s.label }}
          </button>
        </div>
      </div>
    </div>

    <!-- My rank banner (if logged in) -->
    <div
      v-if="currentUser && currentUserRank > 0"
      class="bg-primary/5 border border-primary/20 rounded-xl px-5 py-3 mb-4 flex items-center justify-between"
    >
      <div class="flex items-center gap-3">
        <TrendingUp :size="16" class="text-primary" />
        <span class="text-sm font-medium text-foreground">Ваша позиция: #{{ currentUserRank }}</span>
      </div>
      <span class="text-sm text-muted-foreground">
        {{
          sortKey === 'rating'
            ? `${currentUser.rating.toLocaleString()} рейтинга`
            : sortKey === 'xp'
              ? `${currentUser.xp.toLocaleString()} XP`
              : sortKey === 'solved'
                ? `${currentUser.casesSolved} дел`
                : ''
        }}
      </span>
    </div>

    <!-- Full leaderboard -->
    <div class="bg-card border border-border rounded-2xl overflow-hidden">
      <!-- Table header -->
      <div class="flex items-center gap-4 px-5 py-3 border-b border-border bg-muted/30 text-xs font-medium text-muted-foreground uppercase tracking-wide">
        <div class="w-10 text-center">#</div>
        <div class="flex-1">Детектив</div>
        <div class="w-24 text-right">Рейтинг</div>
        <div class="w-20 text-right">XP</div>
        <div class="w-24 text-right">Раскрыто</div>
        <div class="w-20 text-right">Побед</div>
        <div class="w-24 text-right">Ср. балл</div>
      </div>

      <!-- Table body -->
      <div v-if="sorted.length === 0" class="text-center py-16 text-muted-foreground">
        <Search :size="24" class="mx-auto mb-3 opacity-40" />
        <p>Детективы не найдены</p>
      </div>
      <div v-else>
        <!-- Use template or normal div for the loop to avoid nesting errors -->
        <div
          v-for="(user, idx) in sorted"
          :key="user.id"
          @click="$emit('navigate', 'profile')"
          :class="`flex items-center gap-4 px-5 py-4 border-b border-border last:border-0 hover:bg-muted/20 cursor-pointer transition-colors ${
            currentUser?.id === user.id ? 'bg-primary/5' : ''
          }`"
        >
          <div :class="`w-10 text-center shrink-0 ${idx < 3 ? 'text-lg' : 'text-sm font-bold text-muted-foreground'}`">
            {{ idx < 3 ? RANK_ICONS[idx] : idx + 1 }}
          </div>
          <div class="flex-1 flex items-center gap-3 min-w-0">
            <div class="w-8 h-8 rounded-lg bg-primary/10 border border-primary/20 flex items-center justify-center shrink-0">
              <span class="text-xs font-bold text-primary">{{ (user.username || user.nick || 'Аноним')[0] }}</span>
            </div>
            <div>
              <div class="flex items-center gap-2">
                <span :class="`text-sm font-semibold ${currentUser?.id === user.id ? 'text-primary' : 'text-foreground'}`">
                  {{ (user.username || user.nick || 'Аноним') }}
                </span>
                <span v-if="currentUser?.id === user.id" class="text-[10px] px-1.5 py-0.5 bg-primary/20 text-primary rounded-full">
                  Вы
                </span>
              </div>
              <span class="text-xs text-muted-foreground capitalize">{{ user.role }}</span>
            </div>
          </div>
          <div class="w-24 text-right">
            <span :class="`text-sm font-bold ${idx < 3 && idx === 0 ? 'text-primary' : 'text-foreground'}`">
              {{ user.rating.toLocaleString() }}
            </span>
          </div>
          <div class="w-20 text-right">
            <span class="text-sm text-muted-foreground">{{ user.xp.toLocaleString() }}</span>
          </div>
          <div class="w-24 text-right">
            <span class="text-sm text-foreground">{{ user.casesSolved }}</span>
          </div>
          <div class="w-20 text-right">
            <span class="text-sm text-foreground">{{ (user as any).wins }}</span>
          </div>
          <div class="w-24 text-right">
            <span :class="`text-sm font-medium ${(user as any).avgScore > 70 ? 'text-success' : 'text-foreground'}`">
              {{ (user as any).avgScore || '—' }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- Footer stats -->
    <div class="flex gap-4 mt-6">
      <div
        v-for="s in footerStats"
        :key="s.label"
        class="flex-1 bg-card border border-border rounded-xl p-4 text-center"
      >
        <p class="text-2xl font-bold text-primary">{{ s.value }}</p>
        <p class="text-xs text-muted-foreground mt-1">{{ s.label }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { Trophy, TrendingUp, Search, Filter, Medal } from 'lucide-vue-next'
import { MOCK_USERS, MOCK_SOLUTIONS, MOCK_CASES } from '../data/mockData'
import type { User } from '../data/mockData'
import { apiClient } from '../api/client'

const users = ref<any[]>([])
const solutions = ref<any[]>([])
const isLoading = ref(true)

onMounted(async () => {
  try {
    const res = await apiClient.get('/users')
    users.value = res.data
    
    // Подгружаем решения для подсчета статистики (для рейтинговой таблицы)
    for (const u of users.value) {
      try {
        const solRes = await apiClient.get(`/users/${u.id}/solutions`)
        if (solRes.data && solRes.data.length > 0) {
          solutions.value.push(...solRes.data)
        }
      } catch (err) {
        // Игнорируем ошибки для отдельных пользователей
      }
    }
  } catch (e) {
    console.error('Failed to load users', e)
  } finally {
    isLoading.value = false
  }
})

interface Props {
  currentUser?: User | null
}

const props = withDefaults(defineProps<Props>(), {
  currentUser: null,
})

const emit = defineEmits(['navigate'])

type SortKey = 'rating' | 'xp' | 'solved' | 'avgScore'
type Period = 'all' | 'month' | 'week'

const sortKey = ref<SortKey>('rating')
const period = ref<Period>('all')
const search = ref('')

const SORT_OPTIONS = [
  { key: 'rating', label: 'Рейтинг' },
  { key: 'xp', label: 'Опыт (XP)' },
  { key: 'solved', label: 'Раскрыто дел' },
  { key: 'avgScore', label: 'Средний балл' },
]

const PERIOD_OPTIONS = [
  { key: 'all', label: 'Всё время' },
  { key: 'month', label: 'Месяц' },
  { key: 'week', label: 'Неделя' },
]

const RANK_ICONS = ['🥇', '🥈', '🥉']
const RANK_STYLES = [
  'border-muted bg-muted/30',
  'border-muted bg-muted/20',
]

const userStats = computed(() => {
    const list = users.value.length > 0 ? users.value : MOCK_USERS;
    return list.map(user => {
      const isMockUser = !user.id || typeof user.id === 'string';
      const sols = isMockUser 
        ? MOCK_SOLUTIONS.filter(s => s.userId === user.id && s.score !== undefined)
        : solutions.value.filter(s => s.user?.id === user.id && s.score !== null);
      
      const avgScore = sols.length > 0 ? Math.round(sols.reduce((sum, s) => sum + (s.score ?? 0), 0) / sols.length) : 0;
      const wins = sols.filter(s => s.score >= 90).length; // Более 90 баллов = победа
      const casesSolvedCount = sols.length;
      
      return { 
        ...user, 
        xp: user.experience || user.xp || 0, 
        casesSolved: casesSolvedCount || user.casesSolved || 0, 
        avgScore, 
        wins 
      };
    });
  })

const filtered = computed(() =>
    userStats.value.filter(u => {
      const name = u.username || u.nick || 'Аноним';
      return name.toLowerCase().includes(search.value.toLowerCase());
    })
)

const sorted = computed(() => {
  const arr = [...filtered.value]
  if (sortKey.value === 'rating') return arr.sort((a, b) => b.rating - a.rating)
  if (sortKey.value === 'xp') return arr.sort((a, b) => b.xp - a.xp)
  if (sortKey.value === 'solved') return arr.sort((a, b) => b.casesSolved - a.casesSolved)
  if (sortKey.value === 'avgScore') return arr.sort((a, b) => b.avgScore - a.avgScore)
  return arr
})

const currentUserRank = computed(() => {
  if (!props.currentUser) return 0
  return sorted.value.findIndex(u => u.id === props.currentUser!.id) + 1
})

const footerStats = computed(() => [
  { label: 'Всего детективов', value: MOCK_USERS.length },
  { label: 'Всего решений', value: MOCK_SOLUTIONS.length },
  { label: 'Активных дел', value: MOCK_CASES.filter(c => c.status === 'active').length },
])
</script>

