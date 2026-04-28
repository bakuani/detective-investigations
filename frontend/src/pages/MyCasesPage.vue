<template>
  <div data-cmp="MyCasesPage" class="max-w-[1440px] mx-auto px-6 py-10">
    <div class="mb-8">
      <p class="text-xs font-medium text-primary uppercase tracking-widest mb-2">Кабинет</p>
      <h1 class="text-3xl font-bold text-foreground">Мои расследования</h1>
    </div>

    <!-- Tabs -->
    <div class="flex gap-4 border-b border-border mb-8">
      <button
        v-for="t in [
          { id: 'active', label: 'Активные' },
          { id: 'solo', label: 'Соло' },
          { id: 'closed', label: 'Завершенные' }
        ]"
        :key="t.id"
        @click="tab = t.id as any"
        :class="`px-4 py-3 text-sm font-medium transition-colors border-b-2 ${
          tab === t.id 
            ? 'border-primary text-primary' 
            : 'border-transparent text-muted-foreground hover:text-foreground hover:border-border'
        }`"
      >
        {{ t.label }}
        <span v-if="t.id === 'active' && activeCases.length > 0" class="ml-2 px-2 py-0.5 rounded-full bg-primary/20 text-primary text-xs">
          {{ activeCases.length }}
        </span>
      </button>
    </div>

    <!-- Content -->
    <div v-if="displayedCases.length === 0" class="flex flex-col items-center justify-center py-24 text-muted-foreground border border-dashed border-border rounded-xl bg-card/30">
      <FolderOpen :size="48" class="mb-4 opacity-50" />
      <p class="text-lg font-medium text-foreground mb-2">Пока ничего нет</p>
      <p class="text-sm mb-6 max-w-md text-center">
        Вы еще не участвуете в делах такого типа. Перейдите в каталог, чтобы найти интересное расследование.
      </p>
      <button
        @click="$emit('navigate', 'catalog')"
        class="px-6 py-2 bg-primary text-primary-foreground rounded-lg text-sm font-medium hover:opacity-90"
      >
        В каталог
      </button>
    </div>
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div v-for="c in displayedCases" :key="c.id">
        <CaseCard
          :case-data="c"
          @click="$emit('navigate', 'case-detail', c.id)"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { FolderOpen } from 'lucide-vue-next'
import { apiClient } from '../api/client'
import { stompClient, onStompConnect } from '../api/websocket'
  import CaseCard from '../components/CaseCard.vue'

const props = defineProps<{
  currentUser: any | null
}>()

defineEmits(['navigate'])

const tab = ref<'active' | 'solo' | 'closed'>('active')

const cases = ref<any[]>([])
const loading = ref(true)

let casesSubscription: any = null;
onStompConnect(() => {
  casesSubscription = stompClient.subscribe('/topic/cases', (message) => {
    const updatedCase = JSON.parse(message.body);
    const index = cases.value.findIndex(c => c.id === updatedCase.id);
    if (index !== -1) {
      cases.value[index] = updatedCase;
    } else if (
        updatedCase.creator?.id === props.currentUser?.id ||
        updatedCase.participants?.some((p: any) => p.id === props.currentUser?.id)
    ) {
      cases.value.unshift(updatedCase);
    }
  });
});

onUnmounted(() => {
  if (casesSubscription) casesSubscription.unsubscribe();
});

onMounted(async () => {
    try {
        const res = await apiClient.get('/cases')
        cases.value = res.data.filter((c: any) =>
            c.creator?.id === props.currentUser?.id ||
            c.participants?.some((p: any) => p.id === props.currentUser?.id)
        );
    } catch {
    } finally {
        loading.value = false;
    }
})

const activeCases = computed(() => cases.value.filter(c => {
    const status = (c.status || '').toLowerCase()
    const mode = (c.mode || '').toLowerCase()
    return mode !== 'solo' && ['active', 'ready', 'scoring'].includes(status)
}))

const closedCases = computed(() => cases.value.filter(c => {
    const status = (c.status || '').toLowerCase()
    return ['closed', 'scored'].includes(status)
}))

const soloCases = computed(() => cases.value.filter(c => {
    const mode = (c.mode || '').toLowerCase()
    return mode === 'solo'
}))

const displayedCases = computed(() => {
  if (tab.value === 'active') return activeCases.value
  if (tab.value === 'closed') return closedCases.value
  return soloCases.value
})
</script>


