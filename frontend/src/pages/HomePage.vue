<template>
  <div data-cmp="HomePage" class="w-full">
    <!-- Hero -->
    <section class="relative overflow-hidden noise-overlay">
      <div class="max-w-[1440px] mx-auto px-6 py-24 flex flex-col items-center text-center">
        <!-- Badge -->
        <div class="inline-flex items-center gap-2 px-4 py-1.5 bg-primary/10 border border-primary/30 rounded-full mb-6">
          <span class="w-1.5 h-1.5 rounded-full bg-primary animate-pulse-slow"></span>
          <span class="text-xs font-medium text-primary tracking-wide">AI-POWERED DETECTIVE PLATFORM</span>
        </div>

        <h1 class="text-5xl font-bold text-foreground mb-4 leading-tight max-w-3xl">
          Раскрывайте преступления. <span class="text-gradient-gold">Вместе.</span>
        </h1>

        <p class="text-lg text-muted-foreground max-w-2xl mb-10 leading-relaxed">
          Платформа детективных расследований — AI генерирует уникальные дела, вы анализируете улики, строите версии и первым называете имя виновного.
        </p>

        <div class="flex items-center gap-4">
          <button
            @click="$emit('navigate', 'catalog')"
            class="flex items-center gap-2 px-8 py-3.5 bg-primary text-primary-foreground rounded-xl font-semibold text-base hover:opacity-90 transition-opacity"
          >
            Смотреть дела
            <ArrowRight :size="18" />
          </button>
          
          <button
            v-if="!currentUser"
            @click="$emit('navigate', 'auth')"
            class="flex items-center gap-2 px-8 py-3.5 border border-border rounded-xl font-semibold text-base text-muted-foreground hover:text-foreground hover:border-primary/40 transition-all"
          >
            Стать детективом
          </button>
          
          <button
            v-if="currentUser"
            @click="$emit('navigate', 'create-case')"
            class="flex items-center gap-2 px-8 py-3.5 border border-border rounded-xl font-semibold text-base text-muted-foreground hover:text-foreground hover:border-primary/40 transition-all"
          >
            Создать дело
          </button>
        </div>
      </div>

      <!-- Decorative grid -->
      <div 
        class="absolute inset-0 -z-10 opacity-5"
        style="background-image: linear-gradient(var(--border) 1px, transparent 1px), linear-gradient(90deg, var(--border) 1px, transparent 1px); background-size: 60px 60px"
      />
    </section>

    <!-- Stats -->
    <section class="border-y border-border bg-card/40">
      <div class="max-w-[1440px] mx-auto px-6 py-8">
        <div class="flex items-center justify-around">
          <div v-for="(stat, i) in stats" :key="i" class="flex items-center gap-4">
            <div :class="['w-10 h-10 rounded-xl bg-muted flex items-center justify-center', stat.color]">
              <component :is="stat.icon" :size="20" />
            </div>
            <div>
              <p class="text-2xl font-bold text-foreground">{{ stat.value }}</p>
              <p class="text-xs text-muted-foreground">{{ stat.label }}</p>
            </div>
            <div v-if="i < stats.length - 1" class="w-px h-10 bg-border ml-8"></div>
          </div>
        </div>
      </div>
    </section>

    <!-- Active cases preview -->
    <section class="max-w-[1440px] mx-auto px-6 py-16">
      <div class="flex items-end justify-between mb-8">
        <div>
          <p class="text-xs font-medium text-primary uppercase tracking-widest mb-2">Актуальные дела</p>
          <h2 class="text-2xl font-bold text-foreground">Открытые расследования</h2>
        </div>
        <button
          @click="$emit('navigate', 'catalog')"
          class="flex items-center gap-2 text-sm text-primary hover:text-primary/80 font-medium transition-colors"
        >
          Все дела <ArrowRight :size="14" />
        </button>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div v-for="c in activeCases" :key="c.id">
          <CaseCard :caseData="c" @click="$emit('navigate', 'case-detail', c.id)" />
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ArrowRight, BookOpen, Users, Trophy, Zap } from 'lucide-vue-next'
import CaseCard from '../components/CaseCard.vue'
import { apiClient } from '../api/client'

defineProps<{
  currentUser: any
}>()

defineEmits(['navigate'])

const activeCases = ref<any[]>([])

const stats = ref([
  { label: 'Активных дел', value: '0', icon: BookOpen, color: 'text-primary' },
  { label: 'Детективов', value: '0', icon: Users, color: 'text-chart-5' },
  { label: 'Раскрыто дел', value: '0', icon: Trophy, color: 'text-chart-1' },
  { label: 'Улик сегодня', value: '0', icon: Zap, color: 'text-chart-2' },
])

onMounted(async () => {
  try {
    const res = await apiClient.get('/cases')
    const allCases = res.data
    activeCases.value = allCases.filter((c: any) => 
      ['active', 'ready', 'scoring'].includes((c.status || '').toLowerCase()) && 
      (c.mode || '').toLowerCase() !== 'solo'
    ).slice(0, 3)
  } catch (e) {
    console.error('Failed to load active cases', e)
  }

  try {
    const statsRes = await apiClient.get('/cases/stats')
    const data = statsRes.data

    stats.value = [
      { label: 'Активных дел', value: data.activeCases.toString(), icon: BookOpen, color: 'text-primary' },
      { label: 'Детективов', value: data.detectives.toString(), icon: Users, color: 'text-chart-5' },
      { label: 'Раскрыто дел', value: data.solvedCases.toString(), icon: Trophy, color: 'text-chart-1' },
      { label: 'Улик сегодня', value: data.cluesToday.toString(), icon: Zap, color: 'text-chart-2' },
    ]
  } catch (e) {
    console.error('Failed to load stats', e)
  }
})
</script>
