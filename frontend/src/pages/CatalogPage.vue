<template>
  <div data-cmp="CatalogPage" class="max-w-[1440px] mx-auto px-6 py-10">
    <!-- Header -->
    <div class="mb-8">
      <p class="text-xs font-medium text-primary uppercase tracking-widest mb-2">Каталог</p>
      <h1 class="text-3xl font-bold text-foreground">Дела для расследования</h1>
      <p class="text-muted-foreground mt-2">
        {{ filtered.length }} дел найдено
        <span v-if="!currentUser" class="ml-2 text-xs">·
          <button @click="$emit('navigate', 'auth')" class="text-primary hover:underline">Войдите</button>,
          чтобы участвовать
        </span>
      </p>
    </div>

    <div class="flex gap-6 border-b border-border mb-6">
      <button
        v-for="o in statusOptions"
        :key="o.value"
        @click="filterStatus = o.value"
        :class="`pb-3 text-sm font-medium transition-all relative ${
          filterStatus === o.value
            ? 'text-primary'
            : 'text-muted-foreground hover:text-foreground'
        }`"
      >
        {{ o.label }}
        <div v-if="filterStatus === o.value" class="absolute bottom-0 left-0 w-full h-0.5 bg-primary rounded-t-full" />
      </button>
    </div>

    <!-- Search & filters -->
    <div class="flex items-center gap-3 mb-6">
      <div class="flex-1 relative">
        <Search :size="16" class="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
        <input
          v-model="search"
          placeholder="Поиск по названию или описанию..."
          class="w-full pl-9 pr-4 py-2.5 bg-input border border-border rounded-xl text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary/50 transition-colors"
        />
        <button
          v-if="search"
          @click="search = ''"
          class="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
        >
          <X :size="14" />
        </button>
      </div>

      <!-- Sort -->
      <div class="relative">
        <select
          v-model="sort"
          class="appearance-none pl-3 pr-8 py-2.5 bg-input border border-border rounded-xl text-sm text-foreground focus:outline-none focus:border-primary/50 transition-colors cursor-pointer"
        >
          <option v-for="o in sortOptions" :key="o.value" :value="o.value">{{ o.label }}</option>
        </select>
        <ChevronDown :size="14" class="absolute right-2.5 top-1/2 -translate-y-1/2 text-muted-foreground pointer-events-none" />
      </div>

      <!-- Filter toggle -->
      <button
        @click="filtersOpen = !filtersOpen"
        :class="`flex items-center gap-2 px-4 py-2.5 rounded-xl border text-sm font-medium transition-all ${
          filtersOpen || activeFiltersCount > 0
            ? 'border-primary/50 bg-primary/10 text-primary'
            : 'border-border text-muted-foreground hover:text-foreground hover:border-primary/30'
        }`"
      >
        <SlidersHorizontal :size="16" />
        Фильтры
        <span v-if="activeFiltersCount > 0" class="w-5 h-5 rounded-full bg-primary text-primary-foreground text-[10px] flex items-center justify-center font-bold">
          {{ activeFiltersCount }}
        </span>
      </button>
    </div>

    <!-- Filter panel -->
    <div v-if="filtersOpen" class="bg-card border border-border rounded-xl p-5 mb-6 flex items-center gap-6 flex-wrap">

      <div class="flex flex-col gap-1.5">
        <label class="text-xs font-medium text-muted-foreground uppercase tracking-wide">Сложность</label>
        <div class="flex items-center gap-2">
          <button
            @click="filterDifficulty = 'all'"
            :class="`px-3 py-1.5 rounded-lg text-sm transition-all ${filterDifficulty === 'all' ? 'bg-primary text-primary-foreground' : 'bg-muted text-muted-foreground hover:bg-accent'}`"
          >
            Все
          </button>
          <button
            v-for="d in [1, 2, 3, 4, 5]"
            :key="d"
            @click="filterDifficulty = d"
            :class="`px-3 py-1.5 rounded-lg text-sm transition-all ${filterDifficulty === d ? 'bg-primary text-primary-foreground' : 'bg-muted text-muted-foreground hover:bg-accent'}`"
          >
            {{ '?'.repeat(d) }}
          </button>
        </div>
      </div>

      <div class="w-px h-10 bg-border" />

      <div class="flex flex-col gap-1.5">
        <label class="text-xs font-medium text-muted-foreground uppercase tracking-wide">Жанр</label>
        <div class="relative">
          <select
            v-model="filterGenre"
            class="appearance-none pl-3 pr-8 py-1.5 bg-muted border border-border rounded-lg text-sm text-foreground focus:outline-none focus:border-primary/50 cursor-pointer"
          >
            <option value="all">Все жанры</option>
            <option v-for="g in genres" :key="g" :value="g">{{ g }}</option>
          </select>
          <ChevronDown :size="12" class="absolute right-2.5 top-1/2 -translate-y-1/2 text-muted-foreground pointer-events-none" />
        </div>
      </div>

      <div class="ml-auto">
        <button
          @click="resetFilters"
          class="text-xs text-muted-foreground hover:text-destructive flex items-center gap-1 transition-colors"
        >
          <X :size="12" /> Сбросить
        </button>
      </div>
    </div>

    <!-- Cases grid -->
    <div v-if="filtered.length === 0" class="flex flex-col items-center justify-center py-24 text-muted-foreground">
      <div class="text-5xl mb-4">??</div>
      <p class="text-lg font-medium text-foreground mb-2">Дела не найдены</p>
      <p class="text-sm">Попробуйте изменить параметры поиска или фильтры</p>
    </div>
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div v-for="c in filtered" :key="c.id">
        <CaseCard :case-data="c" @click="$emit('navigate', 'case-detail', c.id)" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { Search, ChevronDown, X, SlidersHorizontal } from 'lucide-vue-next'
import { apiClient } from '../api/client'
import { stompClient, onStompConnect } from '../api/websocket'
import CaseCard from '../components/CaseCard.vue'

const props = defineProps<{
  currentUser: any
}>()

const emit = defineEmits(['navigate'])

const cases = ref<any[]>([])

onMounted(async () => {
  try {
    const res = await apiClient.get('/cases')
    cases.value = res.data
  } catch (e) {
    console.error('Failed to load cases', e)
  }
})

let catalogSubscription: any = null;

onStompConnect(() => {
  catalogSubscription = stompClient.subscribe('/topic/cases', (message) => {
    const updatedCase = JSON.parse(message.body);
    const index = cases.value.findIndex(c => c.id === updatedCase.id);
    if (index !== -1) {
      cases.value[index] = updatedCase;
    } else {
      cases.value.unshift(updatedCase);
    }
  });
});

onUnmounted(() => {
  if (catalogSubscription) {
    catalogSubscription.unsubscribe();
  }
});

const search = ref('')
const filterStatus = ref('all')
const filterDifficulty = ref<any>('all')
const filterGenre = ref('all')
const sort = ref('newest')
const filtersOpen = ref(false)

const sortOptions = [
  { value: 'newest', label: 'Сначала новые' },
  { value: 'deadline', label: 'Скоро дедлайн' },
  { value: 'difficulty-asc', label: 'Сначала простые' },
  { value: 'difficulty-desc', label: 'Сначала сложные' },
  { value: 'genre', label: 'По жанру' },
  { value: 'participants', label: 'По популярности' },
]

const statusOptions = [
    { value: 'all', label: 'Все' },
    { value: 'active', label: 'Активные' },
    { value: 'closed', label: 'Закрытые' }
]

const genres = ['Классический', 'Нуар', 'Мистика', 'Киберпанк', 'Исторический']

const activeFiltersCount = computed(() => {
  let count = 0
  if (filterDifficulty.value !== 'all') count++
  if (filterGenre.value !== 'all') count++
  return count
})

const resetFilters = () => {
    filterDifficulty.value = 'all'
    filterGenre.value = 'all'
}

const filtered = computed(() => {
  const result = cases.value.filter(c => {
    // 1. Difficulty parsing
    let diffObj = c.difficulty;
    let computedDiff = 3;
    if (typeof diffObj === 'number') {
      computedDiff = diffObj;
    } else if (typeof diffObj === 'string') {
      const lower = diffObj.toLowerCase();
      if (lower.includes('easy') || lower.includes('1')) computedDiff = 1;
      else if (lower.includes('2')) computedDiff = 2;
      else if (lower.includes('3')) computedDiff = 3;
      else if (lower.includes('hard') || lower.includes('4')) computedDiff = 4;
      else if (lower.includes('expert') || lower.includes('5')) computedDiff = 5;
    }

    if (filterDifficulty.value !== 'all' && computedDiff !== filterDifficulty.value) return false;

    // 2. Search parsing
    if (search.value) {
      const q = search.value.toLowerCase();
      const matchTitle = c.title && c.title.toLowerCase().includes(q);
      const matchIntro = c.intro && c.intro.toLowerCase().includes(q);
      const matchTags = c.tags && c.tags.some((t: string) => t.toLowerCase().includes(q));
      if (!matchTitle && !matchIntro && !matchTags) return false;
    }

    const isSolo = c.mode === 'solo' || c.mode === 'SOLO';
    const cStatus = (c.status || '').toLowerCase();

    if (isSolo) {
      // Solo cases are excluded from the main catalog
      return false;
    }
    
    if (filterStatus.value === 'active' && !['active', 'ready', 'scoring'].includes(cStatus)) return false;
    if (filterStatus.value === 'closed' && !['closed', 'scored'].includes(cStatus)) return false;

    // 4. Genre filtering
    const cGenre = c.genre || c.theme;
    if (filterGenre.value !== 'all' && cGenre !== filterGenre.value) return false;

    return true;
  })

  return result.sort((a, b) => {
    // Shared difficulty parsing function for sorting
    const getDiff = (d: any) => {
      let r = 3;
      if (typeof d === 'number') r = d;
      else if (typeof d === 'string') {
        const dl = d.toLowerCase();
        if (dl.includes('easy') || dl.includes('1')) r = 1;
        else if (dl.includes('2')) r = 2;
        else if (dl.includes('3')) r = 3;
        else if (dl.includes('hard') || dl.includes('4')) r = 4;
        else if (dl.includes('expert') || dl.includes('5')) r = 5;
      }
      return r;
    };

    switch (sort.value) {
      case 'newest':
        return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      case 'deadline':
        return new Date(a.deadline || 0).getTime() - new Date(b.deadline || 0).getTime()
      case 'difficulty-asc':
        return getDiff(a.difficulty) - getDiff(b.difficulty)
      case 'difficulty-desc':
        return getDiff(b.difficulty) - getDiff(a.difficulty)
      case 'genre':
          return (a.genre || a.theme || '').localeCompare(b.genre || b.theme || '')
        case 'participants':
        const aLen = a.participants?.length || 0;
        const bLen = b.participants?.length || 0;
        return bLen - aLen
      default:
        return 0
    }
  })
})
</script>


