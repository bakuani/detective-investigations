<template>
  <div data-cmp="ModerationPage">
    <!-- No access - not logged in -->
    <div v-if="!currentUser" class="max-w-[1440px] mx-auto px-6 py-24 text-center">
      <div class="text-5xl mb-4">🔒</div>
      <h2 class="text-2xl font-bold text-foreground mb-2">Доступ запрещён</h2>
      <p class="text-muted-foreground mb-6">Войдите с правами модератора</p>
      <button
        @click="() => $emit('navigate', 'auth')"
        class="px-6 py-3 bg-primary text-primary-foreground rounded-xl font-semibold hover:opacity-90 transition-opacity"
      >
        Войти
      </button>
    </div>

    <!-- No access - insufficient rights -->
    <div v-else-if="!hasAccess" class="max-w-[1440px] mx-auto px-6 py-24 text-center">
      <div class="text-5xl mb-4">⛔</div>
      <h2 class="text-2xl font-bold text-foreground mb-2">Недостаточно прав</h2>
      <p class="text-muted-foreground">Эта страница доступна только модераторам и администраторам</p>
    </div>

    <!-- Main content -->
    <div v-else class="max-w-[1440px] mx-auto px-6 py-10">
      <!-- Header -->
      <div class="flex items-start justify-between mb-8">
        <div>
          <p class="text-xs font-medium text-primary uppercase tracking-widest mb-2">Модерация</p>
          <h1 class="text-3xl font-bold text-foreground">Панель модерации</h1>
        </div>
        <div class="flex items-center gap-2 bg-primary/10 border border-primary/20 rounded-xl px-4 py-2">
          <Shield :size="16" class="text-primary" />
          <span class="text-sm font-medium text-primary capitalize">{{ currentUser.role }}</span>
        </div>
      </div>

      <!-- Stats -->
      <div class="flex gap-4 mb-8">
        <div
          v-for="(stat, i) in stats"
          :key="i"
          :class="[
            'flex-1 bg-card border rounded-xl p-4 flex items-center gap-3',
            stat.alert ? 'border-warning/30' : 'border-border'
          ]"
        >
          <div
            :class="[
              'w-8 h-8 rounded-lg flex items-center justify-center',
              stat.alert ? 'bg-warning/10 text-warning' : 'bg-muted text-muted-foreground'
            ]"
          >
            <component :is="stat.icon" :size="14" />
          </div>
          <div>
            <p :class="['text-xl font-bold', stat.alert ? 'text-warning' : 'text-foreground']">
              {{ stat.value }}
            </p>
            <p class="text-xs text-muted-foreground">{{ stat.label }}</p>
          </div>
        </div>
      </div>

      <!-- Tabs -->
      <div class="flex gap-1 mb-6 bg-muted rounded-xl p-1 w-fit">
        <button
          v-for="t in tabOptions"
          :key="t.id"
          @click="tab = t.id"
          :class="[
            'flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium transition-all',
            tab === t.id
              ? 'bg-card text-foreground shadow-custom'
              : 'text-muted-foreground hover:text-foreground'
          ]"
        >
          {{ t.label }}
          <span
            v-if="t.count > 0"
            :class="[
              'text-[10px] px-1.5 py-0.5 rounded-full font-bold',
              t.id === 'pending' && t.count > 0
                ? 'bg-warning/20 text-warning'
                : 'bg-muted-foreground/20 text-muted-foreground'
            ]"
          >
            {{ t.count }}
          </span>
        </button>
      </div>

      <div class="flex gap-6">
        <!-- Reports list -->
        <div class="flex-1">
          <!-- Pending/Resolved tabs content -->
          <div v-if="tab === 'pending' || tab === 'resolved'" class="flex flex-col gap-3">
            <div
              v-if="displayedReports.length === 0"
              class="bg-card border border-border rounded-xl p-12 text-center text-muted-foreground"
            >
              <CheckCircle :size="32" class="mx-auto mb-3 opacity-40" />
              <p class="font-medium text-foreground">
                {{ tab === 'pending' ? 'Нет активных жалоб' : 'Нет рассмотренных жалоб' }}
              </p>
            </div>

            <div v-else>
              <div v-for="report in displayedReports" :key="report.id">
                <div
                  @click="selectedReport?.id === report.id ? (selectedReport = null) : (selectedReport = report)"
                  :class="[
                    'bg-card border rounded-xl p-4 cursor-pointer transition-all',
                    selectedReport?.id === report.id ? 'border-primary/40' : 'border-border hover:border-primary/20'
                  ]"
                >
                  <!-- Report header -->
                  <div class="flex items-center justify-between mb-3">
                    <div class="flex items-center gap-2">
                      <span
                        :class="[
                          'text-xs px-2.5 py-1 rounded-full border font-medium',
                          reportTypeColors[report.reason] || 'text-muted-foreground bg-muted'
                        ]"
                      >
                        {{ reportTypeLabels[report.reason] || report.reason }}
                      </span>
                      <span
                        v-if="report.status === 'resolved'"
                        class="text-xs px-2.5 py-1 rounded-full border border-success/30 bg-success/10 text-success"
                      >
                        Решено
                      </span>
                    </div>
                    <div class="flex items-center gap-2 text-xs text-muted-foreground">
                      <Clock :size="12" />
                      {{ formatDate(report.createdAt) }}
                    </div>
                  </div>

                  <!-- Report details -->
                  <div class="flex items-center gap-6 text-sm">
                    <div class="flex items-center gap-2">
                      <User :size="12" class="text-muted-foreground" />
                      <span class="text-muted-foreground">От:</span>
                      <span class="text-foreground font-medium">{{ getReporter(report)?.nick ?? '?' }}</span>
                    </div>
                    <ChevronRight :size="12" class="text-muted-foreground" />
                    <div class="flex items-center gap-2">
                      <span class="text-muted-foreground">На:</span>
                      <span class="text-foreground font-medium">{{ getReported(report)?.nick ?? '?' }}</span>
                    </div>
                    <template v-if="getReportedCase(report)">
                      <ChevronRight :size="12" class="text-muted-foreground" />
                      <span class="text-muted-foreground text-xs">в деле: {{ getReportedCase(report)!.title }}</span>
                    </template>
                  </div>

                  <!-- Report reason -->
                  <p v-if="report.reason" class="text-xs text-muted-foreground mt-2 bg-muted/50 rounded-lg px-3 py-2">
                    {{ report.reason }}
                  </p>

                  <!-- Action buttons (pending reports only) -->
                  <div
                    v-if="selectedReport?.id === report.id && report.status === 'pending'"
                    @click.stop
                    class="mt-4 border-t border-border pt-4"
                  >
                    <textarea
                      v-model="actionNote"
                      placeholder="Заметка к действию (необязательно)..."
                      rows="2"
                      class="w-full bg-input border border-border rounded-lg px-3 py-2 text-xs text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary/50 resize-none mb-3"
                    />
                    <div class="flex gap-2">
                      <button
                        @click="handleAction(report.id, 'approve')"
                        class="flex-1 flex items-center justify-center gap-1.5 py-2 bg-success/10 border border-success/30 text-success rounded-lg text-xs font-medium hover:bg-success/20 transition-colors"
                      >
                        <CheckCircle :size="13" /> Подтвердить
                      </button>
                      <button
                        @click="handleAction(report.id, 'warn')"
                        class="flex-1 flex items-center justify-center gap-1.5 py-2 bg-warning/10 border border-warning/30 text-warning rounded-lg text-xs font-medium hover:bg-warning/20 transition-colors"
                      >
                        <AlertTriangle :size="13" /> Предупредить
                      </button>
                      <button
                        @click="handleAction(report.id, 'reject')"
                        class="flex-1 flex items-center justify-center gap-1.5 py-2 bg-destructive/10 border border-destructive/30 text-destructive rounded-lg text-xs font-medium hover:bg-destructive/20 transition-colors"
                      >
                        <XCircle :size="13" /> Отклонить
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Log tab content -->
          <div v-else-if="tab === 'log'" class="bg-card border border-border rounded-xl overflow-hidden">
            <div class="flex items-center gap-4 px-5 py-3 border-b border-border bg-muted/30 text-xs font-medium text-muted-foreground uppercase tracking-wide">
              <div class="w-32">Дата</div>
              <div class="w-28">Модератор</div>
              <div class="flex-1">Действие</div>
              <div class="w-32">Цель</div>
            </div>
            <div v-for="entry in MOCK_MODERATION_ACTIONS" :key="entry.id">
              <div class="flex items-center gap-4 px-5 py-3.5 border-b border-border last:border-0 hover:bg-muted/10 transition-colors">
                <div class="w-32 text-xs text-muted-foreground">{{ formatDate(entry.timestamp) }}</div>
                <div class="w-28 text-sm font-medium text-foreground">{{ getModerator(entry)?.nick ?? '?' }}</div>
                <div class="flex-1">
                  <span
                    :class="[
                      'text-xs px-2 py-0.5 rounded-full border',
                      entry.action === 'ban'
                        ? 'text-destructive bg-destructive/10 border-destructive/20'
                        : entry.action === 'warn'
                          ? 'text-warning bg-warning/10 border-warning/20'
                          : 'text-muted-foreground bg-muted border-border'
                    ]"
                  >
                    {{ actionTypeLabels[entry.action] || entry.action }}
                  </span>
                  <span v-if="entry.reason" class="text-xs text-muted-foreground ml-2">{{ entry.reason }}</span>
                </div>
                <div class="w-32 text-sm text-muted-foreground">{{ getTarget(entry)?.nick ?? '?' }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- Sidebar: guidelines -->
        <div class="w-64 shrink-0">
          <div class="bg-card border border-border rounded-xl p-5 sticky top-24">
            <h3 class="font-semibold text-foreground mb-4 text-sm flex items-center gap-2">
              <Shield :size="14" class="text-primary" /> Правила модерации
            </h3>
            <div class="flex flex-col gap-3 text-xs text-muted-foreground">
              <div v-for="(rule, i) in moderationRules" :key="i" class="flex gap-2">
                <span class="shrink-0">{{ rule.icon }}</span>
                <span>{{ rule.text }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import {
  Shield,
  AlertTriangle,
  CheckCircle,
  XCircle,
  Eye,
  Clock,
  User,
  MessageSquare,
  ChevronRight,
} from 'lucide-vue-next'
import { MOCK_REPORTS, MOCK_MODERATION_ACTIONS, MOCK_USERS, MOCK_CASES, type User as UserType } from '../data/mockData'

interface Props {
  currentUser?: UserType | null
}

const props = withDefaults(defineProps<Props>(), {
  currentUser: null,
})

const emit = defineEmits(['navigate'])

// Use string literals to avoid import issues for types
type ReportTab = 'pending' | 'resolved' | 'log'

const tab = ref<ReportTab>('pending')
const reports = ref([...MOCK_REPORTS])
const selectedReport = ref<any | null>(null)
const actionNote = ref('')

const hasAccess = computed(
  () => props.currentUser && (props.currentUser.role === 'moderator' || props.currentUser.role === 'admin')
)

const pendingReports = computed(() => reports.value.filter((r) => r.status === 'pending'))
const resolvedReports = computed(() => reports.value.filter((r) => r.status !== 'pending'))

const displayedReports = computed(() => {
  if (tab.value === 'pending') return pendingReports.value
  if (tab.value === 'resolved') return resolvedReports.value
  return []
})

const stats = computed(() => [
  {
    label: 'В ожидании',
    value: pendingReports.value.length,
    icon: Clock,
    alert: pendingReports.value.length > 0,
  },
  {
    label: 'Рассмотрено',
    value: resolvedReports.value.length,
    icon: CheckCircle,
    alert: false,
  },
  {
    label: 'Действий сегодня',
    value: MOCK_MODERATION_ACTIONS.filter((l) => new Date(l.timestamp).toDateString() === new Date().toDateString()).length,
    icon: Shield,
    alert: false,
  },
  {
    label: 'Всего жалоб',
    value: reports.value.length,
    icon: AlertTriangle,
    alert: false,
  },
])

const tabOptions = computed(() => [
  { id: 'pending' as const, label: 'Ожидают', count: pendingReports.value.length },
  { id: 'resolved' as const, label: 'Рассмотрено', count: resolvedReports.value.length },
  { id: 'log' as const, label: 'Журнал действий', count: MOCK_MODERATION_ACTIONS.length },
])

const reportTypeLabels: Record<string, string> = {
  spam: 'Спам',
  offensive: 'Оскорбления',
  spoiler: 'Спойлер',
  cheating: 'Читерство',
  inappropriate: 'Неприемлемый контент',
}

const reportTypeColors: Record<string, string> = {
  spam: 'text-chart-2 bg-chart-2/10 border-chart-2/20',
  offensive: 'text-destructive bg-destructive/10 border-destructive/20',
  spoiler: 'text-warning bg-warning/10 border-warning/20',
  cheating: 'text-chart-5 bg-chart-5/10 border-chart-5/20',
  inappropriate: 'text-destructive bg-destructive/10 border-destructive/20',
}

const actionTypeLabels: Record<string, string> = {
  warn: 'Предупреждение',
  ban: 'Бан',
  delete_message: 'Удалено сообщение',
  approve_report: 'Жалоба одобрена',
  reject_report: 'Жалоба отклонена',
}

const moderationRules = [
  { icon: '✅', text: 'Спам: блокировка без предупреждения' },
  { icon: '⚠️', text: 'Оскорбления: 2 предупреждения, затем бан' },
  { icon: '📢', text: 'Спойлеры: удалить сообщение, предупреждение' },
  { icon: '🚫', text: 'Читерство: вечный бан после проверки' },
]

function handleAction(reportId: string, action: 'approve' | 'reject' | 'warn') {
  reports.value = reports.value.map((r) => (r.id === reportId ? { ...r, status: 'resolved' as any } : r))
  const labels = { approve: 'одобрена', reject: 'отклонена', warn: 'выдано предупреждение' }
  console.log(`[Moderation] Жалоба ${labels[action]} (ID: ${reportId}) пользователем ${props.currentUser?.nick}`)
  selectedReport.value = null
  actionNote.value = ''
}

function formatDate(dateStr: string) {
  return new Date(dateStr).toLocaleString('ru-RU', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function getReporter(report: any) {
  return MOCK_USERS.find((u) => u.id === report.reporterId)
}

function getReported(report: any) {
  return MOCK_USERS.find((u) => u.id === report.targetId)
}

function getReportedCase(report: any) {
  return report.caseId ? MOCK_CASES.find((c) => c.id === report.caseId) : null
}

function getModerator(entry: any) {
  return MOCK_USERS.find((u) => u.id === entry.moderatorId)
}

function getTarget(entry: any) {
  return MOCK_USERS.find((u) => u.id === entry.targetId)
}
</script>