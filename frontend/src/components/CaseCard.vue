<template>
  <div 
    class="bg-card border border-border rounded-[20px] p-6 flex flex-col justify-between h-full hover:border-primary/50 transition-colors cursor-pointer group"
    @click="$emit('click')"
  >
    <!-- Top row -->
    <div class="flex justify-between items-start mb-5">
      <div class="flex items-center gap-3">
        <!-- Public/Solo Badge -->
        <div class="flex items-center gap-1.5 px-3 py-1 rounded-full border border-primary/30 text-primary bg-primary/5 text-xs font-medium">
          <Eye :size="14" v-if="(caseData.mode === 'public' || caseData.mode === 'PUBLIC')" />
          <User :size="14" v-else />
          <span>{{ (caseData.mode === 'public' || caseData.mode === 'PUBLIC') ? 'Public' : 'Solo' }}</span>
        </div>
        <!-- Genre/Category -->
        <span class="text-xs text-muted-foreground font-medium">{{ caseData.genre || caseData.theme || 'Классический детектив' }}</span>
      </div>

      <!-- Difficulty Dots & Status -->
      <div class="flex flex-col items-end gap-1.5">
        <div class="flex gap-1" :title="`Сложность: ${caseData.difficulty} из 5`">
          <span 
            v-for="i in 5" 
            :key="i"
            class="w-[6px] h-[6px] rounded-full"
            :class="i <= (difficultyLevel) ? 'bg-primary' : 'bg-muted-foreground/30'"
          ></span>
        </div>
        <span 
          class="text-xs font-medium"
          :class="displayStatus.color"
        >
          {{ displayStatus.label }}
        </span>
      </div>
    </div>

    <!-- Title & Description -->
    <div class="mb-6 flex-1">
      <h3 class="text-[17px] leading-tight font-bold text-foreground mb-3 font-serif group-hover:text-primary transition-colors line-clamp-2">
        {{ (caseData.title || caseData.theme) }}
      </h3>
      <p class="text-sm text-muted-foreground line-clamp-2 leading-relaxed">
        {{ caseData.intro || caseData.description }}
      </p>
    </div>

    <!-- Tags -->
    <div class="flex flex-wrap gap-2 mb-[14px]">
      <span
        v-for="tag in getTags.slice(0, 3)"
        :key="tag"
        class="px-2.5 py-1 rounded-[8px] bg-muted/40 text-muted-foreground text-xs"
      >
        #{{ tag }}
      </span>
    </div>

    <!-- Footer -->
    <div class="flex justify-between items-center pt-4 border-t border-border/40">
      <div class="flex items-center gap-4 text-xs font-medium">
        <div class="flex items-center gap-1.5" :class="difficultyColor">
          <Zap :size="14" />
          <span>{{ difficultyLabel }}</span>
        </div>
        <div class="flex items-center gap-1.5 text-muted-foreground">
          <Users :size="14" />
          <span>{{ caseData.participants?.length || 0 }}</span>
        </div>
        <div class="flex items-center gap-1.5 text-muted-foreground">
          <BookOpen :size="14" />
          <span>{{ resolvedCluesCount }}/{{ caseData.clues?.length || 0 }}</span>
        </div>
      </div>
      
      <div class="flex items-center gap-1.5 text-xs font-medium opacity-80" :class="displayStatus.color">
        <Clock :size="14" />
        <span>{{ displayStatus.label }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Eye, User, Zap, Users, BookOpen, Clock } from 'lucide-vue-next'
import { MOCK_CASES, type Case } from '../data/mockData'

const props = defineProps<{
  caseData: any
}>()

const getTags = computed(() => {
  if (props.caseData.tags && props.caseData.tags.length > 0) return props.caseData.tags;
  const mockMatch = MOCK_CASES.find(c => String(c.id) === String(props.caseData.id));
  if (mockMatch && mockMatch.tags && mockMatch.tags.length > 0) return mockMatch.tags;
  return ['детектив', 'расследование'];
})
const displayStatus = computed(() => {
  const mode = (props.caseData.mode || '').toLowerCase()
  if (mode === 'solo') {
    return { label: 'Соло', color: 'text-[#3b82f6]' }
  }
  const currentStatus = (props.caseData.status || '').toLowerCase()
  if (['active', 'ready'].includes(currentStatus)) return { label: 'Активно', color: 'text-success' }
  if (['queued', 'generating'].includes(currentStatus)) return { label: 'Активно', color: 'text-success' }
  if (['scoring', 'scored'].includes(currentStatus)) return { label: 'Оценка', color: 'text-warning' }
  return { label: 'Закрыто', color: 'text-destructive' }
})

const difficultyLevel = computed(() => {
  const d = props.caseData.difficulty;
  if (typeof d === 'number') return d;
  if (typeof d === 'string') {
    const dl = d.toLowerCase();
    if (dl.includes('easy') || dl.includes('1')) return 1;
    if (dl.includes('2')) return 2;
    if (dl.includes('3')) return 3;
    if (dl.includes('hard') || dl.includes('4')) return 4;
    if (dl.includes('expert') || dl.includes('5')) return 5;
    return 3;
  }
  return 1;
})

const difficultyLabel = computed(() => {
  switch (difficultyLevel.value) {
    case 1: return 'Новичок'
    case 2: return 'Стажер'
    case 3: return 'Опытный'
    case 4: return 'Эксперт'
    case 5: return 'Мастер'
    default: return 'Неизвестно'
  }
})

const difficultyColor = computed(() => {
  switch (difficultyLevel.value) {
    case 1: return 'text-success'
    case 2: return 'text-success'
    case 3: return 'text-primary'
    case 4: return 'text-destructive'
    case 5: return 'text-destructive'
    default: return 'text-primary'
  }
})

const resolvedCluesCount = computed(() => {
  if (!props.caseData.clues) return 0
  return props.caseData.clues.filter((c: any) => {
    if (c.status === 'published' || c.isPublished === true || c.published === true || c.published) return true;
    if (c.status === 'hidden' && !c.revealAt) return false;
    if (!c.revealAt) return true; // Start clues have no revealAt and are immediately published
    return new Date(c.revealAt).getTime() <= Date.now();
  }).length
})
</script>