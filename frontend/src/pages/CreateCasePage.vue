<template>
  <div data-cmp="CreateCasePage" class="max-w-[1440px] mx-auto px-6 py-10">
    <!-- Not authenticated state -->
    <template v-if="!currentUser">
      <div class="px-6 py-24 text-center">
        <div class="text-5xl mb-4">🔒</div>
        <h2 class="text-2xl font-bold text-foreground mb-2">Требуется авторизация</h2>
        <p class="text-muted-foreground mb-6">Войдите чтобы создать дело</p>
        <button 
          @click="$emit('navigate', 'auth')" 
          class="px-6 py-3 bg-primary text-primary-foreground rounded-xl font-semibold hover:opacity-90 transition-opacity"
        >
          Войти
        </button>
      </div>
    </template>

    <!-- Submitted state -->
    <template v-else-if="submitted">
      <div class="flex flex-col items-center text-center py-24">
        <div class="w-20 h-20 bg-primary/10 border-2 border-primary/30 rounded-2xl flex items-center justify-center mb-6">
          <Wand2 :size="32" class="text-primary" />
        </div>
        <h2 class="text-3xl font-bold text-foreground mb-3">Запрос отправлен!</h2>
        <p class="text-muted-foreground max-w-md mb-8 leading-relaxed">
          AI начал генерацию вашего уникального дела. Это займёт от 1 до 3 минут. 
          Вы получите уведомление когда дело будет готово.
        </p>
        <div class="bg-card border border-border rounded-xl p-5 w-full max-w-sm mb-8 text-left">
          <div class="flex flex-col gap-2 text-sm">
            <div 
              v-for="item in submissionSummary" 
              :key="item.label" 
              class="flex justify-between"
            >
              <span class="text-muted-foreground">{{ item.label }}</span>
              <span class="font-medium text-foreground">{{ item.value }}</span>
            </div>
          </div>
        </div>
        <div class="flex gap-3">
          <button 
            @click="$emit('navigate', 'my-cases')" 
            class="px-6 py-3 bg-primary text-primary-foreground rounded-xl font-semibold hover:opacity-90 transition-opacity"
          >
            Мои дела
          </button>
          <button 
            @click="resetForm" 
            class="px-6 py-3 border border-border rounded-xl text-muted-foreground font-semibold hover:text-foreground hover:border-primary/40 transition-all"
          >
            Создать ещё
          </button>
        </div>
      </div>
    </template>

    <!-- Main form -->
    <template v-else>
      <!-- Header -->
      <div class="mb-8">
        <p class="text-xs font-medium text-primary uppercase tracking-widest mb-2">Новое дело</p>
        <h1 class="text-3xl font-bold text-foreground">Создать расследование</h1>
        <p class="text-muted-foreground mt-1">AI сгенерирует уникальное дело на основе ваших настроек</p>
      </div>

      <!-- Steps indicator -->
      <div class="flex items-center gap-4 mb-10">
        <template v-for="(s, i) in steps" :key="s.num">
          <div :class="['flex items-center gap-2', step >= s.num ? 'text-foreground' : 'text-muted-foreground']">
            <div 
              :class="[
                'w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold transition-all',
                step > s.num ? 'bg-primary text-primary-foreground' :
                step === s.num ? 'bg-primary text-primary-foreground' :
                'bg-muted text-muted-foreground'
              ]"
            >
              {{ s.num }}
            </div>
            <span class="text-sm font-medium">{{ s.label }}</span>
          </div>
          <div 
            v-if="i < steps.length - 1"
            :class="['flex-1 h-px transition-colors', step > s.num ? 'bg-primary' : 'bg-border']"
          />
        </template>
      </div>

      <div class="flex gap-8">
        <div class="flex-1 max-w-2xl">
          <!-- Step 1 -->
          <div v-if="step === 1" class="flex flex-col gap-6">
            <!-- Mode -->
            <div>
              <label class="block text-sm font-semibold text-foreground mb-3">Режим дела</label>
              <div class="flex gap-4">
                <button
                  v-for="opt in modeOptions"
                  :key="opt.value"
                  @click="form.mode = opt.value"
                  :class="[
                    'flex-1 p-5 rounded-xl border-2 text-left transition-all',
                    form.mode === opt.value
                      ? 'border-primary bg-primary/5'
                      : 'border-border hover:border-primary/30'
                  ]"
                >
                  <div 
                    :class="[
                      'w-10 h-10 rounded-xl flex items-center justify-center mb-3',
                      form.mode === opt.value ? 'bg-primary/20 text-primary' : 'bg-muted text-muted-foreground'
                    ]"
                  >
                    <component :is="opt.icon" :size="20" />
                  </div>
                  <p class="font-semibold text-foreground mb-1">{{ opt.label }}</p>
                  <p class="text-xs text-muted-foreground leading-relaxed">{{ opt.desc }}</p>
                </button>
              </div>
            </div>

            <!-- Genre -->
            <div>
              <label class="block text-sm font-semibold text-foreground mb-3">Жанр</label>
              <div class="flex flex-wrap gap-2">
                <button
                  v-for="g in GENRES"
                  :key="g"
                  @click="form.genre = g"
                  :class="[
                    'px-4 py-2 rounded-lg text-sm font-medium transition-all',
                    form.genre === g
                      ? 'bg-primary text-primary-foreground'
                      : 'bg-muted text-muted-foreground hover:bg-accent hover:text-foreground'
                  ]"
                >
                  {{ g }}
                </button>
              </div>
            </div>

            <!-- Difficulty -->
            <div>
              <label class="block text-sm font-semibold text-foreground mb-3">Сложность</label>
              <div class="flex flex-col gap-2">
                <button
                  v-for="d in [1, 2, 3, 4, 5]"
                  :key="d"
                  @click="form.difficulty = d as 1 | 2 | 3 | 4 | 5"
                  :class="[
                    'flex items-center gap-4 p-4 rounded-xl border-2 text-left transition-all',
                    form.difficulty === d ? 'border-primary bg-primary/5' : 'border-border hover:border-primary/20'
                  ]"
                >
                  <div class="flex gap-0.5">
                    <div 
                      v-for="i in [1, 2, 3, 4, 5]" 
                      :key="i" 
                      :class="['w-3 h-3 rounded-sm', i <= d ? 'bg-primary' : 'bg-muted']"
                    />
                  </div>
                  <div>
                    <p class="text-sm font-medium text-foreground">{{ DIFFICULTY_LABELS[d] }}</p>
                    <p class="text-xs text-muted-foreground">{{ DIFFICULTY_DESCS[d] }}</p>
                  </div>
                </button>
              </div>
            </div>

            <button 
              @click="step = 2" 
              class="flex items-center justify-center gap-2 py-3 bg-primary text-primary-foreground rounded-xl font-semibold text-sm hover:opacity-90 transition-opacity"
            >
              Далее <ArrowRight :size="16" />
            </button>
          </div>

          <!-- Step 2 -->
          <div v-if="step === 2" class="flex flex-col gap-6">
            <!-- Clue count -->
            <div>
              <label class="block text-sm font-semibold text-foreground mb-1">
                Количество улик: <span class="text-primary">{{ form.clueCount }}</span>
              </label>
              <p class="text-xs text-muted-foreground mb-3">Рекомендуется 5–10 для оптимального баланса</p>
              <input
                v-model.number="form.clueCount"
                type="range"
                min="3"
                max="15"
                class="w-full accent-primary"
              />
              <div class="flex justify-between text-xs text-muted-foreground mt-1">
                <span>3</span><span>15</span>
              </div>
            </div>

            <!-- Deadline -->
            <div>
              <label class="block text-sm font-semibold text-foreground mb-1">
                Дедлайн: <span class="text-primary">{{ form.deadlineDays }} дней</span>
              </label>
              <p class="text-xs text-muted-foreground mb-3">Время на расследование от открытия дела</p>
              <input
                v-model.number="form.deadlineDays"
                type="range"
                min="1"
                max="30"
                class="w-full accent-primary"
              />
              <div class="flex justify-between text-xs text-muted-foreground mt-1">
                <span>1 день</span><span>30 дней</span>
              </div>
            </div>

            <!-- Preferred motive -->
            <div>
              <label class="block text-sm font-semibold text-foreground mb-1">
                Предпочтительный мотив
                <span class="text-muted-foreground font-normal ml-1 text-xs">(необязательно)</span>
              </label>
              <p class="text-xs text-muted-foreground mb-2">AI учтёт при генерации, но не гарантирует точное совпадение</p>
              <div class="relative">
                <select
                  v-model="form.preferredMotive"
                  class="w-full appearance-none pl-3 pr-8 py-2.5 bg-input border border-border rounded-xl text-sm text-foreground focus:outline-none focus:border-primary/50"
                >
                  <option value="">Любой мотив</option>
                  <option v-for="m in MOTIVES" :key="m" :value="m">{{ m }}</option>
                </select>
                <ChevronDown :size="14" class="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground pointer-events-none" />
              </div>
            </div>

            <!-- Extra prompt -->
            <div>
              <label class="block text-sm font-semibold text-foreground mb-1">
                Дополнительные пожелания
                <span class="text-muted-foreground font-normal ml-1 text-xs">(необязательно)</span>
              </label>
              <p class="text-xs text-muted-foreground mb-2">Опишите особые детали сюжета, атмосферы или персонажей</p>
              <textarea
                v-model="form.extraPrompt"
                placeholder="Например: действие в 1920-х, главная подозреваемая — женщина, атмосфера нуар..."
                rows="4"
                class="w-full bg-input border border-border rounded-xl px-3 py-2.5 text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary/50 resize-none"
              />
            </div>

            <div class="flex gap-3">
              <button 
                @click="step = 1" 
                class="flex-1 py-3 border border-border rounded-xl text-sm font-medium text-muted-foreground hover:text-foreground transition-colors"
              >
                Назад
              </button>
              <button 
                @click="step = 3" 
                class="flex-1 flex items-center justify-center gap-2 py-3 bg-primary text-primary-foreground rounded-xl font-semibold text-sm hover:opacity-90 transition-opacity"
              >
                Далее <ArrowRight :size="16" />
              </button>
            </div>
          </div>

          <!-- Step 3 -->
          <div v-if="step === 3" class="flex flex-col gap-6">
            <div class="bg-card border border-border rounded-xl p-5">
              <h3 class="font-semibold text-foreground mb-4">Параметры генерации</h3>
              <div class="flex flex-col gap-2.5">
                <div 
                  v-for="item in step3Summary" 
                  :key="item.label"
                  class="flex items-start gap-3"
                >
                  <span class="text-xs text-muted-foreground w-24 shrink-0 pt-0.5">{{ item.label }}</span>
                  <span class="text-sm text-foreground">{{ item.value }}</span>
                </div>
              </div>
            </div>

            <div class="flex items-start gap-3 p-4 bg-warning/5 border border-warning/20 rounded-xl">
              <Info :size="16" class="text-warning mt-0.5 shrink-0" />
              <p class="text-sm text-muted-foreground">
                Генерация займёт <strong class="text-foreground">1–3 минуты</strong>. После завершения дело появится в каталоге (для публичных) или в разделе «Мои дела» (для solo). Все улики хранятся сразу, но раскрываются по расписанию.
              </p>
            </div>

            <div class="flex gap-3">
              <button 
                @click="step = 2" 
                class="flex-1 py-3 border border-border rounded-xl text-sm font-medium text-muted-foreground hover:text-foreground transition-colors"
              >
                Назад
              </button>
              <button
                @click="handleSubmit"
                :disabled="submitting"
                :class="[
                  'flex-1 flex items-center justify-center gap-2 py-3 bg-primary text-primary-foreground rounded-xl font-semibold text-sm hover:opacity-90 disabled:opacity-60 transition-opacity'
                ]"
              >
                <template v-if="submitting">
                  Генерация<span class="animate-pulse">...</span>
                </template>
                <template v-else>
                  <Wand2 :size="16" /> Запустить генерацию
                </template>
              </button>
            </div>
          </div>
        </div>

        <!-- Sidebar info -->
        <div class="w-72 shrink-0">
          <div class="bg-card border border-border rounded-xl p-5 sticky top-24">
            <h3 class="font-semibold text-foreground mb-4 flex items-center gap-2">
              <Info :size="14" class="text-primary" /> Как работает генерация
            </h3>
            <div class="flex flex-col gap-4 text-sm text-muted-foreground">
              <div 
                v-for="item in sidebarItems" 
                :key="item.num"
                class="flex gap-3"
              >
                <div class="w-5 h-5 rounded-full bg-primary/20 flex items-center justify-center shrink-0 mt-0.5">
                  <span class="text-[10px] font-bold text-primary">{{ item.num }}</span>
                </div>
                <p class="leading-relaxed">{{ item.text }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Wand2, Lock, Eye, ChevronDown, Info, ArrowRight } from 'lucide-vue-next'
import { GENRES, MOTIVES, type User } from '../data/mockData'
import { apiClient } from '../api/client'

interface Props {
  currentUser?: User | null
}

const props = defineProps<Props>()

const emit = defineEmits<{
  navigate: [page: string, caseId?: string]
}>()

type Step = 1 | 2 | 3

const step = ref<Step>(1)
const submitting = ref(false)
const submitted = ref(false)

const form = ref({
  mode: 'public' as 'public' | 'solo',
  genre: GENRES[0],
  difficulty: 3 as 1 | 2 | 3 | 4 | 5,
  preferredMotive: '',
  extraPrompt: '',
  clueCount: 7,
  deadlineDays: 7,
})

const DIFFICULTY_LABELS = ['', 'Начинающий', 'Любитель', 'Опытный', 'Эксперт', 'Мастер']
const DIFFICULTY_DESCS = [
  '',
  'Логические связи просты, улики очевидны',
  'Несколько ложных следов',
  'Запутанный сюжет, требует анализа',
  'Сложные связи, неочевидные мотивы',
  'Максимальная сложность, минимальные подсказки',
]

const steps = [
  { num: 1, label: 'Режим и жанр' },
  { num: 2, label: 'Настройки' },
  { num: 3, label: 'Подтверждение' },
]

const modeOptions = [
  { value: 'public' as const, icon: Eye, label: 'Публичное', desc: 'Другие детективы могут присоединиться и конкурировать' },
  { value: 'solo' as const, icon: Lock, label: 'Solo', desc: 'Личное расследование, только для вас' },
]

const sidebarItems = [
  { num: '1', text: 'Вы задаёте жанр, сложность и параметры' },
  { num: '2', text: 'AI создаёт сюжет, персонажей, все улики и правильный ответ' },
  { num: '3', text: 'Все улики хранятся в базе сразу. Kafka-планировщик раскрывает их по расписанию' },
  { num: '4', text: 'После дедлайна AI оценивает решения по 4 критериям' },
]

const step3Summary = computed(() => [
  { label: 'Режим', value: form.value.mode === 'public' ? 'Публичное соревнование' : 'Solo расследование' },
  { label: 'Жанр', value: form.value.genre },
  { label: 'Сложность', value: `${form.value.difficulty}/5 (${DIFFICULTY_LABELS[form.value.difficulty]})` },
  { label: 'Улики', value: form.value.clueCount },
  { label: 'Дедлайн', value: `${form.value.deadlineDays} дней` },
  { label: 'Специфика', value: form.value.preferredMotive || form.value.extraPrompt ? 'С заданными пожеланиями' : 'Автоматически AI' },
])

const submissionSummary = computed(() => [
  { label: 'Жанр:', value: form.value.genre },
  { label: 'Сложность:', value: `${form.value.difficulty}/5` },
  { label: 'Режим:', value: form.value.mode },
  { label: 'Улики:', value: form.value.clueCount },
])

function resetForm() {
  submitted.value = false
  submitting.value = false
  step.value = 1
  form.value = {
    mode: 'public',
    genre: GENRES[0],
    difficulty: 3,
    preferredMotive: '',
    extraPrompt: '',
    clueCount: 7,
    deadlineDays: 7,
  }
}

function handleSubmit() {
  if (submitting.value || submitted.value) return
  submitting.value = true

  console.log('[Create] Submitting generation config:', form.value)

  apiClient.post('/cases', {
    mode: form.value.mode.toUpperCase(),
    theme: form.value.genre,
    difficulty: String(form.value.difficulty),
    cluesCount: form.value.clueCount,
    deadlineDays: form.value.deadlineDays
  }).then(res => {
    submitting.value = false;
    submitted.value = true;
    console.log('[Create] Case successfully created', res.data);
  }).catch(e => {
    console.error('Failed to create case', e);
    submitting.value = false;
  });
}
</script>