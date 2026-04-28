<template>
  <div data-cmp="AdminPage">
    <!-- Access denied view -->
    <div
      v-if="!currentUser || currentUser.role !== 'admin'"
      class="max-w-[1440px] mx-auto px-6 py-24 text-center"
    >
      <div class="text-5xl mb-4">⛔</div>
      <h2 class="text-2xl font-bold text-foreground mb-2">Доступ запрещён</h2>
      <p class="text-muted-foreground">Эта страница доступна только администраторам</p>
    </div>

    <!-- Admin panel -->
    <div v-else class="max-w-[1440px] mx-auto px-6 py-10">
      <!-- Header -->
      <div class="flex items-start justify-between mb-8">
        <div>
          <p class="text-xs font-medium text-primary uppercase tracking-widest mb-2">Панель администратора</p>
          <h1 class="text-3xl font-bold text-foreground">Настройки системы</h1>
        </div>
        <div class="flex items-center gap-2 bg-destructive/10 border border-destructive/20 rounded-xl px-4 py-2">
          <Settings :size="14" class="text-destructive" />
          <span class="text-sm text-destructive font-medium">Admin</span>
        </div>
      </div>

      <!-- Quick stats -->
      <div class="flex gap-4 mb-8">
        <div
          v-for="stat in stats"
          :key="stat.label"
          class="flex-1 bg-card border border-border rounded-xl p-4"
        >
          <p class="text-2xl font-bold text-primary">{{ stat.value }}</p>
          <p class="text-sm text-foreground font-medium">{{ stat.label }}</p>
          <p class="text-xs text-muted-foreground">{{ stat.sub }}</p>
        </div>
      </div>

      <div class="flex gap-8">
        <!-- Sidebar navigation -->
        <div class="w-52 shrink-0">
          <div class="flex flex-col gap-1">
            <button
              v-for="t in tabOptions"
              :key="t.id"
              @click="tab = t.id"
              :class="[
                'flex items-center gap-2.5 px-4 py-3 rounded-xl text-sm font-medium text-left transition-all',
                tab === t.id
                  ? 'bg-primary/10 text-primary border border-primary/20'
                  : 'text-muted-foreground hover:text-foreground hover:bg-muted'
              ]"
            >
              <component :is="t.icon" :size="15" />
              {{ t.label }}
            </button>
          </div>
        </div>

        <!-- Main content -->
        <div class="flex-1">
          <!-- Generation config -->
          <div v-if="tab === 'generation'" class="bg-card border border-border rounded-2xl p-6">
            <h2 class="text-lg font-bold text-foreground mb-5 flex items-center gap-2">
              <Zap :size="18" class="text-primary" /> Конфигурация AI-генерации
            </h2>
            <div class="flex flex-col gap-5">
              <!-- Model selection -->
              <div>
                <label class="block text-xs font-semibold text-muted-foreground uppercase tracking-wide mb-1.5">Модель AI</label>
                <div class="relative">
                  <select
                    v-model="genConfig.model"
                    class="w-full appearance-none pl-3 pr-8 py-2.5 bg-input border border-border rounded-xl text-sm text-foreground focus:outline-none focus:border-primary/50"
                  >
                    <option v-for="m in ['gpt-4-turbo', 'gpt-4o', 'gpt-3.5-turbo', 'claude-3-opus']" :key="m" :value="m">
                      {{ m }}
                    </option>
                  </select>
                  <ChevronDown :size="14" class="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground pointer-events-none" />
                </div>
              </div>

              <!-- Max tokens and Temperature -->
              <div class="flex gap-5">
                <div class="flex-1">
                  <label class="block text-xs font-semibold text-muted-foreground uppercase tracking-wide mb-1.5">
                    Max tokens: <span class="text-primary">{{ genConfig.maxTokens }}</span>
                  </label>
                  <input
                    v-model.number="genConfig.maxTokens"
                    type="range"
                    min="1024"
                    max="8192"
                    step="256"
                    class="w-full accent-primary"
                  />
                  <div class="flex justify-between text-xs text-muted-foreground mt-1">
                    <span>1024</span>
                    <span>8192</span>
                  </div>
                </div>
                <div class="flex-1">
                  <label class="block text-xs font-semibold text-muted-foreground uppercase tracking-wide mb-1.5">
                    Temperature: <span class="text-primary">{{ genConfig.temperature }}</span>
                  </label>
                  <input
                    v-model.number="genConfig.temperature"
                    type="range"
                    min="0"
                    max="2"
                    step="0.1"
                    class="w-full accent-primary"
                  />
                  <div class="flex justify-between text-xs text-muted-foreground mt-1">
                    <span>0 (точный)</span>
                    <span>2 (креативный)</span>
                  </div>
                </div>
              </div>

              <!-- Max per user and Cooldown -->
              <div class="flex gap-5">
                <div class="flex-1">
                  <label class="block text-xs font-semibold text-muted-foreground uppercase tracking-wide mb-1.5">
                    Макс. генераций на пользователя
                  </label>
                  <input
                    v-model.number="genConfig.maxPerUser"
                    type="number"
                    min="1"
                    max="100"
                    class="w-full px-3 py-2.5 bg-input border border-border rounded-xl text-sm text-foreground focus:outline-none focus:border-primary/50"
                  />
                </div>
                <div class="flex-1">
                  <label class="block text-xs font-semibold text-muted-foreground uppercase tracking-wide mb-1.5">
                    Cooldown между запросами (часов)
                  </label>
                  <input
                    v-model.number="genConfig.cooldownHours"
                    type="number"
                    min="0"
                    max="24"
                    class="w-full px-3 py-2.5 bg-input border border-border rounded-xl text-sm text-foreground focus:outline-none focus:border-primary/50"
                  />
                </div>
              </div>

              <!-- Toggle options -->
              <div class="flex gap-6">
                <div
                  v-for="opt in [
                    { key: 'autoModerate', label: 'Автоматическая модерация', desc: 'AI проверяет сгенерированный контент' },
                    { key: 'requireReview', label: 'Требовать ревью модератора', desc: 'Все дела проходят ручную проверку' }
                  ]"
                  :key="opt.key"
                  class="flex-1 flex items-start gap-3 p-4 bg-muted/30 border border-border rounded-xl"
                >
                  <button
                    @click="genConfig[opt.key as keyof typeof genConfig] = !genConfig[opt.key as keyof typeof genConfig]"
                    :class="[
                      'relative w-10 h-5 rounded-full transition-colors shrink-0 mt-0.5',
                      genConfig[opt.key as keyof typeof genConfig] ? 'bg-primary' : 'bg-muted'
                    ]"
                  >
                    <span
                      :class="[
                        'absolute top-0.5 w-4 h-4 rounded-full bg-primary-foreground shadow transition-transform',
                        genConfig[opt.key as keyof typeof genConfig] ? 'translate-x-5' : 'translate-x-0.5'
                      ]"
                    />
                  </button>
                  <div>
                    <p class="text-sm font-medium text-foreground">{{ opt.label }}</p>
                    <p class="text-xs text-muted-foreground">{{ opt.desc }}</p>
                  </div>
                </div>
              </div>

              <!-- Generation requests table -->
              <div>
                <h3 class="text-sm font-semibold text-foreground mb-3">Последние запросы генерации</h3>
                <div class="rounded-xl border border-border overflow-hidden">
                  <div
                    v-for="(req, i) in mockGenerationRequests.slice(0, 5)"
                    :key="req.id"
                    class="flex items-center gap-4 px-4 py-3 border-b border-border last:border-0 text-sm"
                  >
                    <span class="text-muted-foreground text-xs w-6">{{ i + 1 }}</span>
                    <div class="flex-1">
                      <span class="text-foreground">{{ req.genre }}</span>
                      <span class="text-muted-foreground ml-2">· {{ req.mode }} · сложность {{ req.difficulty }}</span>
                    </div>
                    <span
                      :class="[
                        'text-xs px-2 py-0.5 rounded-full border',
                        req.status === 'ready'
                          ? 'text-success bg-success/10 border-success/20'
                          : req.status === 'queued'
                          ? 'text-warning bg-warning/10 border-warning/20'
                          : req.status === 'generating'
                          ? 'text-primary bg-primary/10 border-primary/20'
                          : 'text-destructive bg-destructive/10 border-destructive/20'
                      ]"
                    >
                      {{ req.status }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Kafka config -->
          <div v-if="tab === 'kafka'" class="bg-card border border-border rounded-2xl p-6">
            <h2 class="text-lg font-bold text-foreground mb-5 flex items-center gap-2">
              <Clock :size="18" class="text-primary" /> Kafka Clue Scheduler
            </h2>
            <div class="flex items-start gap-3 p-4 bg-primary/5 border border-primary/20 rounded-xl mb-5">
              <Info :size="16" class="text-primary mt-0.5 shrink-0" />
              <p class="text-sm text-muted-foreground">
                Все улики генерируются и сохраняются при создании дела. Kafka публикует события раскрытия по расписанию, делая платформу масштабируемой. Изменение профиля влияет только на новые дела.
              </p>
            </div>
            <div class="flex flex-col gap-5">
              <!-- Profile selection -->
              <div>
                <label class="block text-xs font-semibold text-muted-foreground uppercase tracking-wide mb-1.5">Профиль раскрытия по умолчанию</label>
                <div class="flex gap-3">
                  <button
                    v-for="p in [
                      { id: 'uniform', label: 'Равномерный', desc: 'Улики раскрываются с одинаковым интервалом' },
                      { id: 'accelerating', label: 'Ускоряющийся', desc: 'Сначала медленнее, потом чаще' },
                      { id: 'burst', label: 'Пакетный', desc: 'Пачки улик в заданные часы суток' }
                    ]"
                    :key="p.id"
                    @click="kafkaConfig.defaultProfile = p.id"
                    :class="[
                      'flex-1 p-4 rounded-xl border-2 text-left transition-all',
                      kafkaConfig.defaultProfile === p.id
                        ? 'border-primary bg-primary/5'
                        : 'border-border hover:border-primary/20'
                    ]"
                  >
                    <p class="text-sm font-semibold text-foreground mb-1">{{ p.label }}</p>
                    <p class="text-xs text-muted-foreground">{{ p.desc }}</p>
                  </button>
                </div>
              </div>

              <!-- Initial delay and interval -->
              <div class="flex gap-5">
                <div class="flex-1">
                  <label class="block text-xs font-semibold text-muted-foreground uppercase tracking-wide mb-1.5">
                    Начальная задержка: <span class="text-primary">{{ kafkaConfig.initialDelayHours }}ч</span>
                  </label>
                  <input
                    v-model.number="kafkaConfig.initialDelayHours"
                    type="range"
                    min="0"
                    max="24"
                    class="w-full accent-primary"
                  />
                  <div class="flex justify-between text-xs text-muted-foreground mt-1">
                    <span>0ч</span>
                    <span>24ч</span>
                  </div>
                </div>
                <div class="flex-1">
                  <label class="block text-xs font-semibold text-muted-foreground uppercase tracking-wide mb-1.5">
                    Интервал между уликами: <span class="text-primary">{{ kafkaConfig.intervalHours }}ч</span>
                  </label>
                  <input
                    v-model.number="kafkaConfig.intervalHours"
                    type="range"
                    min="1"
                    max="48"
                    class="w-full accent-primary"
                  />
                  <div class="flex justify-between text-xs text-muted-foreground mt-1">
                    <span>1ч</span>
                    <span>48ч</span>
                  </div>
                </div>
              </div>

              <!-- Burst mode -->
              <div class="flex items-start gap-3 p-4 bg-muted/30 border border-border rounded-xl">
                <button
                  @click="kafkaConfig.burstEnabled = !kafkaConfig.burstEnabled"
                  :class="[
                    'relative w-10 h-5 rounded-full transition-colors shrink-0 mt-0.5',
                    kafkaConfig.burstEnabled ? 'bg-primary' : 'bg-muted'
                  ]"
                >
                  <span
                    :class="[
                      'absolute top-0.5 w-4 h-4 rounded-full bg-primary-foreground shadow transition-transform',
                      kafkaConfig.burstEnabled ? 'translate-x-5' : 'translate-x-0.5'
                    ]"
                  />
                </button>
                <div class="flex-1">
                  <p class="text-sm font-medium text-foreground mb-1">Burst mode — пиковый час</p>
                  <p class="text-xs text-muted-foreground mb-3">Удвоить количество раскрытий в заданный час суток</p>
                  <div v-if="kafkaConfig.burstEnabled" class="flex items-center gap-2">
                    <span class="text-xs text-muted-foreground">Час пика:</span>
                    <input
                      v-model.number="kafkaConfig.burstHour"
                      type="number"
                      min="0"
                      max="23"
                      class="w-16 px-2 py-1 bg-input border border-border rounded-lg text-xs text-foreground focus:outline-none focus:border-primary/50"
                    />
                    <span class="text-xs text-muted-foreground">:00</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Scoring config -->
          <div v-if="tab === 'scoring'" class="bg-card border border-border rounded-2xl p-6">
            <h2 class="text-lg font-bold text-foreground mb-5 flex items-center gap-2">
              <BarChart2 :size="18" class="text-primary" /> Формула оценки решений
            </h2>
            <div class="flex flex-col gap-5">
              <!-- Weight validation warning -->
              <div v-if="!validateWeights()" class="flex items-center gap-2 p-3 bg-warning/5 border border-warning/20 rounded-xl text-warning text-sm">
                <AlertTriangle :size="16" />
                <span>Сумма весов должна равняться 100%. Текущая: {{ totalWeights }}%</span>
              </div>

              <!-- Weights sliders -->
              <div>
                <label class="block text-xs font-semibold text-muted-foreground uppercase tracking-wide mb-3">Веса критериев (в сумме 100%)</label>
                <div class="flex flex-col gap-3">
                  <div
                    v-for="item in [
                      { key: 'accuracyWeight', label: 'Точность', desc: 'Правильно ли назван преступник и мотив' },
                      { key: 'speedWeight', label: 'Скорость', desc: 'Бонус за раннее решение' },
                      { key: 'efficiencyWeight', label: 'Эффективность', desc: 'Меньше улик использовано = выше балл' },
                      { key: 'creativityWeight', label: 'Аргументация', desc: 'AI оценивает качество рассуждений' }
                    ]"
                    :key="item.key"
                    class="flex items-center gap-4 p-3 bg-muted/30 rounded-xl"
                  >
                    <div class="flex-1">
                      <div class="flex items-center justify-between mb-1">
                        <span class="text-sm font-medium text-foreground">{{ item.label }}</span>
                        <span class="text-sm font-bold text-primary">{{ scoringConfig[item.key as keyof typeof scoringConfig] }}%</span>
                      </div>
                      <p class="text-xs text-muted-foreground">{{ item.desc }}</p>
                    </div>
                    <input
                      v-model.number="scoringConfig[item.key as keyof typeof scoringConfig]"
                      type="range"
                      min="0"
                      max="100"
                      class="w-32 accent-primary"
                    />
                  </div>
                </div>
              </div>

              <!-- Max score and modifiers -->
              <div class="flex gap-5">
                <div class="flex-1">
                  <label class="block text-xs font-semibold text-muted-foreground uppercase tracking-wide mb-1.5">Макс. балл</label>
                  <input
                    v-model.number="scoringConfig.maxScore"
                    type="number"
                    min="10"
                    max="1000"
                    class="w-full px-3 py-2.5 bg-input border border-border rounded-xl text-sm text-foreground focus:outline-none focus:border-primary/50"
                  />
                </div>
                <div class="flex-1">
                  <label class="block text-xs font-semibold text-muted-foreground uppercase tracking-wide mb-1.5">
                    Time decay за каждые 24ч (%): <span class="text-primary">{{ scoringConfig.timeDecayPercent }}%</span>
                  </label>
                  <input
                    v-model.number="scoringConfig.timeDecayPercent"
                    type="range"
                    min="0"
                    max="30"
                    class="w-full accent-primary"
                  />
                </div>
                <div class="flex-1">
                  <label class="block text-xs font-semibold text-muted-foreground uppercase tracking-wide mb-1.5">
                    Бонус за раннюю подачу (%): <span class="text-primary">{{ scoringConfig.bonusEarlySubmit }}%</span>
                  </label>
                  <input
                    v-model.number="scoringConfig.bonusEarlySubmit"
                    type="range"
                    min="0"
                    max="20"
                    class="w-full accent-primary"
                  />
                </div>
              </div>
            </div>
          </div>

          <!-- Diagnostics -->
          <div v-if="tab === 'diagnostics'" class="flex flex-col gap-4">
            <!-- System diagnostics -->
            <div class="bg-card border border-border rounded-2xl p-6">
              <h2 class="text-lg font-bold text-foreground mb-5 flex items-center gap-2">
                <Database :size="18" class="text-primary" /> Системная диагностика
              </h2>
              <div class="flex flex-col gap-3">
                <div
                  v-for="s in diagnostics"
                  :key="s.name"
                  class="flex items-center gap-4 p-4 bg-muted/30 border border-border rounded-xl"
                >
                  <div
                    :class="[
                      'w-2.5 h-2.5 rounded-full',
                      s.status === 'ok'
                        ? 'bg-success'
                        : s.status === 'warning'
                        ? 'bg-warning'
                        : 'bg-destructive'
                    ]"
                  />
                  <div class="flex-1">
                    <p class="text-sm font-semibold text-foreground">{{ s.name }}</p>
                    <p class="text-xs text-muted-foreground">{{ s.info }}</p>
                  </div>
                  <div class="text-right flex items-center gap-3">
                    <div>
                      <p class="text-xs font-medium text-foreground hover:cursor-pointer" @click="toast.info(`Пинг ${s.name}: ${s.latency}`)">{{ s.latency }}</p>
                      <p :class="['text-xs', s.status === 'ok' ? 'text-success' : 'text-warning']">{{ s.status }}</p>
                    </div>
                    <button class="p-2 bg-muted hover:bg-accent rounded-lg text-muted-foreground hover:text-foreground transition-colors" @click="toast.info(`Пинг ${s.name}: ${s.latency}`)">
                        <RefreshCw :size="13" />
                    </button>
                    </div>
                </div>
              </div>
            </div>

            <!-- Logs -->
            <div class="bg-card border border-border rounded-2xl p-6">
              <h3 class="font-semibold text-foreground mb-4 text-sm">Последние ошибки</h3>
              <div class="flex flex-col gap-2 text-xs font-mono">
                <div
                  v-for="(log, i) in logs"
                  :key="i"
                  class="flex gap-3 p-2 rounded-lg bg-muted/30"
                >
                  <span class="text-muted-foreground w-16 shrink-0">{{ log.time }}</span>
                  <span
                    :class="[
                      'w-10 shrink-0 font-bold',
                      log.level === 'WARN' ? 'text-warning' : 'text-muted-foreground'
                    ]"
                  >
                    {{ log.level }}
                  </span>
                  <span class="text-foreground">{{ log.msg }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Save button -->
          <div v-if="tab !== 'diagnostics'" class="mt-5 flex justify-end">
            <button
              @click="handleSave"
              :disabled="saving || (tab === 'scoring' && !validateWeights())"
              :class="[
                'flex items-center gap-2 px-6 py-3 bg-primary text-primary-foreground rounded-xl font-semibold text-sm hover:opacity-90 disabled:opacity-50 transition-opacity'
              ]"
            >
              <RefreshCw v-if="saving" :size="15" class="animate-spin" />
              <CheckCircle v-else :size="15" />
              {{ saving ? 'Сохранение...' : 'Сохранить настройки' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { Settings, Zap, Clock, BarChart2, Database, RefreshCw, CheckCircle, ChevronDown, Info, AlertTriangle } from 'lucide-vue-next'
import { MOCK_GENERATION_REQUESTS, MOCK_CASES, type User } from '../data/mockData'

interface Props {
  currentUser?: User | null
}

const props = withDefaults(defineProps<Props>(), {
  currentUser: null,
})

const emit = defineEmits(['navigate'])

type AdminTab = 'generation' | 'scoring' | 'kafka' | 'diagnostics'

const tab = ref<AdminTab>('generation')
const saving = ref(false)

const toast: any = { info: console.log, success: console.log } // mock toast

// Generation config state
const genConfig = reactive({
  maxTokens: 4096,
  temperature: 0.7,
  model: 'gpt-4-turbo',
  autoModerate: true,
  requireReview: false,
  maxPerUser: 10,
  cooldownHours: 1
})

// Kafka / clue schedule state
const kafkaConfig = reactive({
  defaultProfile: 'uniform',
  initialDelayHours: 2,
  intervalHours: 12,
  burstEnabled: false,
  burstHour: 18
})

// Scoring formula state
const scoringConfig = reactive({
  accuracyWeight: 40,
  speedWeight: 20,
  efficiencyWeight: 20,
  creativityWeight: 20,
  maxScore: 100,
  timeDecayPercent: 10,
  bonusEarlySubmit: 5
})

// Mock data
const mockGenerationRequests = MOCK_GENERATION_REQUESTS
const mockCases = MOCK_CASES

// Computed properties
const stats = computed(() => [
  { label: 'Запросов генерации', value: mockGenerationRequests.length, sub: 'всего' },
  { label: 'В очереди', value: mockGenerationRequests.filter(r => r.status === 'queued').length, sub: 'ожидают' },
  { label: 'Завершено', value: mockGenerationRequests.filter(r => r.status === 'ready').length, sub: 'успешно' },
  { label: 'Активных дел', value: mockCases.filter(c => c.status === 'active').length, sub: 'сейчас' }
])

const tabOptions = [
  { id: 'generation' as const, label: 'Генерация', icon: Zap },
  { id: 'kafka' as const, label: 'Kafka / Расписание', icon: Clock },
  { id: 'scoring' as const, label: 'Формула оценки', icon: BarChart2 },
  { id: 'diagnostics' as const, label: 'Диагностика', icon: Database }
]

const totalWeights = computed(() =>
  scoringConfig.accuracyWeight +
  scoringConfig.speedWeight +
  scoringConfig.efficiencyWeight +
  scoringConfig.creativityWeight
)

const diagnostics = [
  { name: 'PostgreSQL', status: 'ok', latency: '3ms', info: 'Основная БД — 4 таблицы, 127 записей' },
  { name: 'Kafka Broker', status: 'ok', latency: '8ms', info: 'Топик clue-reveal: 2 партиции' },
  { name: 'AI Service (OpenAI)', status: 'ok', latency: '312ms', info: 'GPT-4-turbo, токены: 18,440/1M' },
  { name: 'Redis Cache', status: 'ok', latency: '1ms', info: 'Session store, 23 активных ключа' },
  { name: 'S3 Storage', status: 'warning', latency: '95ms', info: 'Высокая задержка — проверьте регион' }
]

const logs = [
  { time: '09:43:12', level: 'WARN', msg: 'S3 upload timeout after 5s, retry 1/3' },
  { time: '07:15:01', level: 'INFO', msg: 'Kafka consumer lag: topic=clue-reveal, lag=0' },
  { time: '06:00:00', level: 'INFO', msg: 'Cron: scored 3 expired cases, 12 solutions processed' }
]

// Methods
function handleSave() {
  saving.value = true
  setTimeout(() => {
    saving.value = false
    toast.success('Настройки сохранены')
    console.log('[Admin] Config saved:', { genConfig, kafkaConfig, scoringConfig })
  }, 800)
}

function validateWeights() {
  return totalWeights.value === 100
}
</script>