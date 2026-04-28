<template>
  <div data-cmp="CaseDetailPage" class="max-w-[1440px] mx-auto px-6 py-8">
    <!-- Back button -->
    <button
      @click="$emit('navigate', 'back')"
      class="flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground mb-6 transition-colors"
    >
      <ArrowLeft :size="16" /> Назад к каталогу
    </button>

    <div v-if="caseData" class="flex gap-8">
      <!-- Main content -->
      <div class="flex-1 min-w-0">
        <!-- Case header -->
        <div class="bg-card border border-border rounded-xl p-6 mb-6">
          <div class="flex items-start justify-between gap-4 mb-4">
            <div class="flex-1">
              <div class="flex items-center gap-3 mb-3 flex-wrap">
                <span
                  :class="`text-xs font-medium px-2.5 py-1 rounded-full border ${
                    isActive
                      ? 'border-success/40 bg-success/10 text-success'
                      : isScored
                        ? 'border-muted text-muted-foreground bg-muted/50'
                        : 'border-warning/40 bg-warning/10 text-warning'
                  }`"
                >
                  {{ isActive ? '● Активно' : isScored ? '✓ Завершено' : '⏳ Подсчёт' }}
                </span>
                <span class="text-xs text-muted-foreground bg-muted px-2.5 py-1 rounded-full">
                  {{ caseData.genre || caseData.theme }}
                </span>
                <span class="text-xs text-muted-foreground bg-muted px-2.5 py-1 rounded-full capitalize">
                  {{ caseData.mode }}
                </span>
                <div class="flex items-center gap-1">
                  <div
                    v-for="d in 5"
                    :key="d"
                    :class="`w-2 h-2 rounded-full ${d <= caseData.difficulty ? 'bg-primary' : 'bg-muted'}`"
                  />
                </div>
              </div>
              <h1 class="text-2xl font-bold text-foreground mb-2">{{ caseData.title }}</h1>
              <p class="text-muted-foreground text-sm leading-relaxed">{{ caseData.intro }}</p>
            </div>
          </div>

          <div class="flex items-center gap-6 pt-4 border-t border-border flex-wrap">
            <div class="flex items-center gap-2 text-sm text-muted-foreground">
              <Clock :size="14" class="text-primary" />
              <span>Дедлайн: </span>
              <span class="font-medium text-foreground">{{ formatDate(caseData.deadline) }}</span>
            </div>
            <div v-if="caseData.mode === 'public'" class="flex items-center gap-2 text-sm text-muted-foreground">
              <Users :size="14" />
              <span>{{ caseData.participants?.length || 0 }} участников</span>
            </div>
            <div class="flex items-center gap-2 text-sm text-muted-foreground">
              <BookOpen :size="14" />
              <span>{{ publishedClues.length }} из {{ (caseData.clues?.length || publishedClues.length || 0) }} улик раскрыто</span>
            </div>
          </div>
        </div>

        <!-- Tabs -->
        <div class="flex gap-1 mb-6 bg-muted rounded-xl p-1">
          <button
            v-for="tab in tabs"
            :key="tab.id"
            @click="activeTab = tab.id as any"
            :class="`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium transition-all ${
              activeTab === tab.id
                ? 'bg-card text-foreground shadow-custom'
                : 'text-muted-foreground hover:text-foreground'
            }`"
          >
            <component :is="tab.icon" :size="14" />
            {{ tab.label }}
          </button>
        </div>

        <!-- Tab: Overview -->
        <div v-if="activeTab === 'overview'" class="flex flex-col gap-6">
          <div v-if="caseData.characters?.length > 0" class="bg-card border border-border rounded-xl p-6">
            <h2 class="font-semibold text-foreground mb-4 flex items-center gap-2">
              <Users :size="16" class="text-primary" /> Персонажи
            </h2>
            <div class="flex flex-col gap-3">
              <div
                v-for="char in caseData.characters"
                :key="char.id"
                class="flex items-start gap-3 p-3 bg-muted/50 rounded-lg cursor-pointer hover:bg-muted transition-colors"
                @click="expandedCharacterId = expandedCharacterId === char.id ? null : char.id"
              >
                <div class="w-9 h-9 rounded-full bg-primary/10 border border-primary/20 flex items-center justify-center shrink-0">
                  <span class="text-sm font-bold text-primary">{{ char.name[0] }}</span>
                </div>
                <div class="flex-1">
                  <div class="flex items-center justify-between gap-2">
                    <div class="flex items-center gap-2">
                      <span class="font-medium text-foreground text-sm">{{ char.name }}</span>
                      <span v-if="char.role" class="text-xs text-muted-foreground bg-muted px-2 py-0.5 rounded-full">
                        {{ char.role }}
                      </span>
                    </div>
                    <component :is="expandedCharacterId === char.id ? EyeOff : Eye" :size="14" class="text-muted-foreground" />
                  </div>
                  <p class="text-xs text-muted-foreground mt-0.5">{{ char.description }}</p>
                  <div v-if="expandedCharacterId === char.id && char.alibi" class="mt-3 p-3 bg-card border border-border rounded-lg text-sm text-foreground whitespace-pre-wrap">
                    <span class="text-xs font-semibold text-primary mb-1 block uppercase tracking-wider">Показания / Алиби</span>
                    {{ char.alibi }}
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Tags -->
          <div v-if="caseData.tags?.length > 0" class="flex items-center gap-2">
            <span class="text-xs text-muted-foreground">Теги:</span>
            <span v-for="tag in caseData.tags" :key="tag" class="text-xs px-2.5 py-1 bg-muted rounded-full text-muted-foreground">
              #{{ tag }}
            </span>
          </div>

          <!-- Canonical Resolution (Ground Truth) -->
          <div v-if="caseData.groundTruth" class="border border-warning/30 rounded-xl p-6 relative overflow-hidden mt-2">
            <div class="absolute top-0 left-0 w-1 h-full bg-warning/50"></div>
            <div class="flex justify-between items-center mb-4">
              <h2 class="text-warning font-semibold flex items-center gap-2">
                <Trophy :size="18" /> Каноническая развязка
              </h2>
              <button v-if="currentUser" @click="showGroundTruth = !showGroundTruth" class="text-xs text-muted-foreground hover:text-foreground flex items-center gap-1 transition-colors">
                <component :is="showGroundTruth ? EyeOff : Eye" :size="14" />
                {{ showGroundTruth ? 'Скрыть' : 'Показать' }}
              </button>
            </div>
            
            <template v-if="currentUser">
              <div v-show="showGroundTruth" class="flex flex-col gap-4">
                <div class="text-sm text-foreground bg-muted/30 rounded-lg p-4 whitespace-pre-wrap leading-relaxed">
                  {{ caseData.groundTruth }}
                </div>
              </div>
              <div v-show="!showGroundTruth" class="text-sm text-muted-foreground italic">
                Развязка скрыта. Нажмите "Показать", чтобы увидеть подробности.
              </div>
            </template>
            <div v-else class="text-sm text-muted-foreground italic mt-2">
              Вам нужно <button @click="$emit('navigate', 'auth')" class="text-primary hover:underline">войти/зарегистрироваться</button>, чтобы посмотреть развязку.
            </div>
          </div>
        </div>

        <!-- Tab: Clues -->
        <div v-if="activeTab === 'clues'" class="flex flex-col gap-4">
          <div v-if="!currentUser" class="text-center py-12 text-muted-foreground border rounded-xl bg-card">
            <Lock :size="32" class="mx-auto mb-3 opacity-40 text-primary" />
            <p class="font-medium mb-1">Доступ к уликам ограничен</p>
            <p class="text-sm">Вам нужно <button @click="$emit('navigate', 'auth')" class="text-primary hover:underline font-medium">войти/зарегистрироваться</button>, чтобы посмотреть улики и развязку.</p>
          </div>
          <template v-else>
            <div v-if="publishedClues.length === 0" class="text-center py-12 text-muted-foreground">
              <BookOpen :size="32" class="mx-auto mb-3 opacity-40" />
              <p>Улики пока не раскрыты</p>
            </div>
            <div
              v-for="clue in publishedCluesSorted"
              :key="clue.id"
              :class="`border rounded-xl p-5 transition-colors ${clueTypeColors[clue.type] || 'border-border'} mb-4`"
            >
            <div class="flex items-start justify-between gap-3 mb-2">
              <div class="flex items-center gap-2">
                <span class="text-xs font-medium text-muted-foreground bg-muted px-2 py-0.5 rounded-full">
                  {{ clueTypeLabels[clue.type] }}
                </span>
                <span
                  v-if="clue.isRedHerring"
                  class="text-xs text-warning bg-warning/10 border border-warning/20 px-2 py-0.5 rounded-full"
                >
                  ? Ложный след
                </span>
              </div>
              <div class="flex items-center gap-1 shrink-0">
                <span class="text-xs text-muted-foreground">Важность:</span>
                <div
                  v-for="d in 5"
                  :key="d"
                  :class="`w-1.5 h-1.5 rounded-full ${d <= Math.ceil((clue.importance || clue.importanceRank || 0) / 2) ? 'bg-primary' : 'bg-muted'}`"
                ></div>
              </div>
            </div>
            <p class="text-sm text-foreground leading-relaxed">{{ clue.content }}</p>
            <p class="text-xs text-muted-foreground mt-2">Раскрыта: {{ formatDate(clue.revealAt) }}</p>
          </div>

          <div v-if="hiddenCluesCount > 0" class="border border-border border-dashed rounded-xl p-5 flex items-center gap-3 text-muted-foreground">
            <Lock :size="16" />
            <div>
              <p class="text-sm font-medium text-foreground">
                Ещё {{ hiddenCluesCount }} улик будут раскрыты
              </p>
              <p class="text-xs mt-0.5">Ключевые улики появятся ближе к дедлайну</p>
            </div>
          </div>
          </template>
        </div>

        <!-- Tab: Chat -->
        <div v-if="activeTab === 'chat'" class="bg-card border border-border rounded-xl flex flex-col h-[500px] overflow-hidden">
          <template v-if="isClosed">
            <div class="flex-1 flex flex-col items-center justify-center p-8 text-center text-muted-foreground">
              <Lock :size="48" class="mb-4 opacity-40" />
              <p class="text-xl font-bold text-foreground mb-2">Дело закрыто</p>
              <p>дело закрыто, чат недоступен. Вы можете только просматривать старые сообщения.</p>
            </div>
            <div class="h-64 overflow-y-auto p-4 flex flex-col gap-4 border-t border-border opacity-60">
              <div v-if="chatMessages.length === 0" class="text-center py-6 text-muted-foreground">
                <p>Нет сообщений.</p>
              </div>
              <div v-else v-for="msg in chatMessages" :key="msg.id" :class="['flex', (msg.sender?.id || msg.user?.id) === currentUser?.id ? 'justify-end' : 'justify-start']">
                <div :class="['max-w-[80%] rounded-xl p-3 text-sm flex flex-col', (msg.sender?.id || msg.user?.id) === currentUser?.id ? 'bg-primary text-primary-foreground rounded-br-sm' : 'bg-muted text-foreground rounded-bl-sm']">
                  <span class="font-bold text-xs opacity-70 mb-1">{{ msg.sender?.username || msg.sender?.nick || msg.user?.username || msg.user?.nick || 'Неизвестный' }} <span class="ml-2 font-normal">{{ new Date(msg.createdAt).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'}) }}</span></span>
                  <span class="whitespace-pre-wrap">{{ msg.content }}</span>
                </div>
              </div>
            </div>
          </template>
          <template v-else>
            <div class="flex-1 overflow-y-auto p-4 flex flex-col gap-4">
              <div v-if="chatMessages.length === 0" class="text-center py-20 text-muted-foreground flex flex-col items-center">
                <MessageCircle :size="32" class="mb-3 opacity-40" />
                <p>Нет сообщений. Напишите первым!</p>
              </div>
              <div v-else v-for="msg in chatMessages" :key="msg.id" :class="['flex', (msg.sender?.id || msg.user?.id) === currentUser?.id ? 'justify-end' : 'justify-start']">
                <div :class="['max-w-[80%] rounded-xl p-3 text-sm flex flex-col', (msg.sender?.id || msg.user?.id) === currentUser?.id ? 'bg-primary text-primary-foreground rounded-br-sm' : 'bg-muted text-foreground rounded-bl-sm']">
                  <span class="font-bold text-xs opacity-70 mb-1">{{ msg.sender?.username || msg.sender?.nick || msg.user?.username || msg.user?.nick || 'Неизвестный' }} <span class="ml-2 font-normal">{{ new Date(msg.createdAt).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'}) }}</span></span>
                  <span class="whitespace-pre-wrap">{{ msg.content }}</span>
                </div>
              </div>
            </div>
            <div class="p-3 border-t border-border flex gap-2 bg-muted/20">
              <input 
                v-model="newChatMessage" 
                @keyup.enter="sendChatMessage" 
                :disabled="isClosed" 
                placeholder="Написать в чат..." 
                class="flex-1 bg-input border border-border rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-primary/50 disabled:opacity-50" 
              />
              <button 
                @click="sendChatMessage" 
                :disabled="isClosed || !newChatMessage.trim()" 
                class="px-4 py-2 bg-primary text-primary-foreground rounded-lg font-medium text-sm disabled:opacity-50 flex items-center justify-center shrink-0 hover:opacity-90"
              >
                Отправить
              </button>
            </div>
          </template>
        </div>

        <!-- Tab: Results -->
        <div v-if="activeTab === 'results'" class="bg-card border border-border rounded-xl overflow-hidden p-6 text-muted-foreground">
          <div v-if="!isScored" class="text-center py-20">
            <Clock :size="32" class="mx-auto mb-3 opacity-40" />
            <p class="font-medium text-foreground">Результаты появятся после закрытия дела</p>
            <p class="text-sm mt-1" v-if="caseData.deadline">Дедлайн: {{ formatDate(caseData.deadline) }}</p>
          </div>

          <div v-if="isScored && allSolutions.length > 0" class="flex flex-col gap-4">
            <div class="bg-card border border-border rounded-xl p-5">
              <h3 class="font-semibold text-foreground mb-4 flex items-center gap-2">
                <Trophy :size="16" class="text-primary" /> Таблица результатов
              </h3>
              <div class="flex flex-col gap-3">
                <div
                  v-for="(sol, idx) in sortedSolutions"
                  :key="sol.id"
                  :class="['flex items-center gap-4 p-4 rounded-xl border transition-colors', isWinner(sol) ? 'border-primary/40 bg-primary/5' : 'border-border bg-muted/30']"
                >
                  <div :class="['w-8 h-8 rounded-lg flex items-center justify-center font-bold text-sm shrink-0', isWinner(sol) ? 'bg-primary/20 text-primary' : 'bg-muted text-muted-foreground']">
                    {{ isWinner(sol) ? '🏆' : idx + 1 }}
                  </div>
                  <div class="w-8 h-8 rounded-full bg-primary/10 flex items-center justify-center shrink-0">
                    <span class="text-xs font-bold text-primary">{{ getSolUserInitial(sol) }}</span>
                  </div>
                  <div class="flex-1">
                    <p class="font-medium text-sm text-foreground">{{ getSolUserNick(sol) }}</p>
                    <p class="text-xs text-muted-foreground">
                      {{ sol.culprit }} · {{ sol.motive }}
                    </p>
                  </div>
                  <div class="text-right">
                    <p :class="['text-lg font-bold', isWinner(sol) ? 'text-primary' : 'text-foreground']">
                      {{ sol.score ?? 0 }}
                    </p>
                    <p class="text-xs text-muted-foreground">баллов</p>
                  </div>
                </div>
              </div>
            </div>

            <!-- Score breakdown for current user -->
            <div v-if="currentUser && userSolution && userSolution.breakdown" class="bg-card border border-border rounded-xl p-5">
              <h3 class="font-semibold text-foreground mb-4">Ваш разбор</h3>
              <div class="flex flex-col gap-2 mb-4">
                <div v-for="item in [
                  { label: 'Виновный', value: userSolution.breakdown.culprit, max: 25 },
                  { label: 'Мотив', value: userSolution.breakdown.motive, max: 25 },
                  { label: 'Способ', value: userSolution.breakdown.method, max: 25 },
                  { label: 'Логика', value: userSolution.breakdown.logic, max: 25 }
                ]" :key="item.label" class="flex items-center gap-3">
                  <span class="text-xs text-muted-foreground w-20 shrink-0">{{ item.label }}</span>
                  <div class="flex-1 h-2 bg-muted rounded-full overflow-hidden">
                    <div
                      class="h-full bg-primary rounded-full transition-all"
                      :style="{ width: `${(item.value / item.max) * 100}%` }"
                    ></div>
                  </div>
                  <span class="text-xs font-medium text-foreground w-12 text-right">
                    {{ item.value || 0 }}/{{ item.max }}
                  </span>
                </div>
              </div>
              <p class="text-sm text-muted-foreground bg-muted/50 rounded-lg p-3">
                {{ userSolution.breakdown.explanation }}
              </p>
            </div>

            <div class="px-6 mx-auto max-w-2xl text-left mt-6">
              <h3 class="font-semibold text-lg text-foreground mb-4 text-center">Официальная развязка</h3>
              <div v-if="currentUser" class="text-sm text-foreground bg-muted/30 rounded-lg p-4 whitespace-pre-wrap leading-relaxed">
                {{ caseData?.groundTruth || 'Развязка не указана' }}
              </div>
              <div v-else class="text-sm text-muted-foreground bg-muted/30 rounded-lg p-4 text-center">
                Вам нужно <button @click="$emit('navigate', 'auth')" class="text-primary hover:underline">войти/зарегистрироваться</button>, чтобы посмотреть развязку.
              </div>
            </div>
          </div>

          <div v-if="isScored && allSolutions.length === 0" class="text-center py-20">
             <ListChecks :size="32" class="mx-auto mb-3 opacity-40" />
             <p>Результаты расследования пока недоступны</p>
             <div class="px-6 mx-auto max-w-2xl text-left mt-6">
               <h3 class="font-semibold text-lg text-foreground mb-4 text-center">Официальная развязка</h3>
               <div v-if="currentUser" class="text-sm text-foreground bg-muted/30 rounded-lg p-4 whitespace-pre-wrap leading-relaxed">
                 {{ caseData?.groundTruth || 'Развязка не указана' }}
               </div>
               <div v-else class="text-sm text-muted-foreground bg-muted/30 rounded-lg p-4 text-center">
                 Вам нужно <button @click="$emit('navigate', 'auth')" class="text-primary hover:underline">войти/зарегистрироваться</button>, чтобы посмотреть развязку.
               </div>
             </div>
          </div>
        </div>
      </div>

      <!-- Sidebar -->
      <div class="w-80 shrink-0 flex flex-col gap-4">
        <!-- Action card -->
        <div class="bg-card border border-border rounded-xl p-5">
          <div v-if="!currentUser" class="text-center">
            <p class="text-sm text-muted-foreground mb-3">Войдите чтобы участвовать в расследовании</p>
            <button
              @click="$emit('navigate', 'auth')"
              class="w-full py-2.5 bg-primary text-primary-foreground rounded-lg text-sm font-semibold hover:opacity-90 transition-opacity"
            >
              Войти / Регистрация
            </button>
          </div>
          <div v-else-if="!hasJoined && caseData.mode === 'public' && isActive" class="text-center">
            <p class="text-sm text-muted-foreground mb-3">
              Присоединитесь к расследованию чтобы обсуждать улики и сдать решение
            </p>
            <button
              @click="handleJoin"
              class="w-full py-2.5 bg-primary text-primary-foreground rounded-lg text-sm font-semibold hover:opacity-90 transition-opacity"
            >
              Присоединиться
            </button>
          </div>
          <div v-else-if="hasJoined" class="flex flex-col gap-3">
            <div v-if="userSolution" class="text-center p-3 bg-success/10 border border-success/20 rounded-lg">
              <CheckCircle :size="20" class="text-success mx-auto mb-2" />
              <span class="text-sm font-medium text-success block">Решение сдано</span>
            </div>
            <button
              v-else-if="isActive"
              @click="showSolutionForm = true"
              class="w-full py-2.5 bg-primary text-primary-foreground rounded-lg text-sm font-semibold hover:opacity-90 transition-opacity flex items-center justify-center gap-2"
            >
              <CheckCircle :size="16" />
              Сдать решение
            </button>
            <div v-else class="text-center p-3 bg-muted/30 border border-border rounded-lg text-muted-foreground">
              <span class="text-sm font-medium block">Решение не сдано</span>
            </div>

            <button
              v-if="caseData.mode === 'public' && isActive && !userSolution"
              @click="handleLeave"
              class="w-full py-2 border border-border rounded-lg text-xs text-muted-foreground hover:text-destructive hover:border-destructive/40 transition-all"
            >
              Покинуть дело
            </button>
          </div>
        </div>

        <!-- Clue progress -->
        <div class="bg-card border border-border rounded-xl p-5">
          <h3 class="font-semibold text-foreground mb-3 text-sm flex items-center gap-2">
            <BookOpen :size="14" class="text-primary" /> Прогресс улик
          </h3>
          <div class="flex items-center gap-3 mb-2">
            <div class="flex-1 h-2 bg-muted rounded-full overflow-hidden">
              <div
                class="h-full bg-primary rounded-full transition-all"
                :style="{ width: `${(publishedClues.length / (caseData.clues?.length || publishedClues.length || 0)) * 100}%` }"
              />
            </div>
            <span class="text-xs text-muted-foreground shrink-0">
              {{ publishedClues.length }}/{{ (caseData.clues?.length || publishedClues.length || 0) }}
            </span>
          </div>
        </div>

        <!-- Case meta -->
        <div class="bg-card border border-border rounded-xl p-5">
          <h3 class="font-semibold text-foreground mb-3 text-sm">Метаданные</h3>
          <div class="flex flex-col gap-2 text-xs text-muted-foreground">
            <div class="flex justify-between">
              <span>Создано</span>
              <span class="text-foreground">{{ formatDate(caseData.createdAt) }}</span>
            </div>
            <div class="flex justify-between">
              <span>Режим</span>
              <span class="text-foreground capitalize">{{ caseData.mode }}</span>
            </div>
            <div class="flex justify-between">
              <span>Игроки</span>
              <span class="text-foreground flex items-center gap-1"><Users :size="12" /> {{ caseData.participants?.length || 0 }}{{ caseData.maxParticipants ? ` / ${caseData.maxParticipants}` : '' }}</span>
            </div>
          </div>
        </div>

        <!-- Participants -->
        <div v-if="caseData.mode === 'public'" class="bg-card border border-border rounded-xl p-5">
          <h3 class="font-semibold text-foreground mb-4 text-sm flex items-center justify-between">
             <span class="flex items-center gap-2"><Users :size="14" /> Участники ({{ Array.isArray(caseData.participants) ? caseData.participants.length : 0 }})</span>
          </h3>
          <div class="flex flex-col gap-3">
            <div v-if="!Array.isArray(caseData.participants) || caseData.participants.length === 0" class="text-sm text-muted-foreground text-center py-2">Нет участников. Присоединитесь первым!</div>
            <div v-else v-for="p in caseData.participants" :key="p.id || p.name" class="flex items-center justify-between text-sm">
              <div class="flex items-center gap-3">
                 <div class="w-6 h-6 rounded bg-muted flex items-center justify-center text-xs font-bold text-muted-foreground">{{ (p.username || p.nick || p.name || 'U')[0].toUpperCase() }}</div>
                 <span class="text-foreground">{{ p.username || p.nick || p.name || 'Аноним' }}</span>
              </div>
              <CheckCircle :size="14" class="text-primary opacity-50" />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Submit Solution Modal -->
    <div v-if="showSolutionForm" class="fixed inset-0 z-50 flex items-center justify-center bg-background/80 backdrop-blur-sm p-4">
      <div class="bg-card border border-border rounded-2xl p-6 w-full max-w-lg shadow-custom">
        <h2 class="text-xl font-bold text-foreground mb-2">Сдать решение</h2>
        <p class="text-sm text-muted-foreground mb-6">
          Внимательно заполните все поля. Решение можно сдать только один раз.
        </p>
        <form @submit.prevent="submitSolution" class="flex flex-col gap-4 text-left">
          <div>
            <label class="block text-xs font-medium text-muted-foreground mb-1.5 uppercase tracking-wide">
              Виновный <span class="text-destructive">*</span>
            </label>
            <input
              v-model="solutionData.suspect"
              type="text"
              required
              class="w-full bg-input border border-border rounded-xl px-4 py-3 text-sm text-foreground focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary/50 transition-all"
              placeholder="Имя подозреваемого"
            />
          </div>
          <div>
            <label class="block text-xs font-medium text-muted-foreground mb-1.5 uppercase tracking-wide">
              Мотив <span class="text-destructive">*</span>
            </label>
            <input
              v-model="solutionData.motive"
              type="text"
              required
              class="w-full bg-input border border-border rounded-xl px-4 py-3 text-sm text-foreground focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary/50 transition-all"
              placeholder="Почему он это сделал?"
            />
          </div>
          <div>
            <label class="block text-xs font-medium text-muted-foreground mb-1.5 uppercase tracking-wide">
              Орудие/Способ <span class="text-destructive">*</span>
            </label>
            <input
              v-model="solutionData.method"
              type="text"
              required
              class="w-full bg-input border border-border rounded-xl px-4 py-3 text-sm text-foreground focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary/50 transition-all"
              placeholder="Как было совершено преступление?"
            />
          </div>
          <div>
            <label class="block text-xs font-medium text-muted-foreground mb-1.5 uppercase tracking-wide">
              Обоснование
            </label>
            <textarea
              v-model="solutionData.justification"
              rows="3"
              class="w-full bg-input border border-border rounded-xl px-4 py-3 text-sm text-foreground focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary/50 transition-all resize-none"
              placeholder="Ваши логические выводы (необязательно)"
            ></textarea>
          </div>
          <div class="flex items-center gap-3 mt-4">
            <button
              type="button"
              @click="showSolutionForm = false"
              class="flex-1 px-4 py-3 rounded-xl border border-border font-medium text-muted-foreground hover:text-foreground transition-colors"
            >
              Отмена
            </button>
            <button
              type="submit"
              class="flex-1 px-4 py-3 rounded-xl bg-primary text-primary-foreground font-medium hover:opacity-90 transition-opacity"
            >
              Отправить
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';
import { stompClient, onStompConnect } from '../api/websocket';
import {
  Clock, Users, BookOpen, ArrowLeft, Send, AlertTriangle, CheckCircle, Eye, EyeOff,
  Zap, Lock, Trophy, Info, MessageCircle, ListChecks
} from 'lucide-vue-next';
import { MOCK_CASES, MOCK_SOLUTIONS, MOCK_USERS, type Case, type User } from '../data/mockData';
import { apiClient } from '../api/client';

const props = defineProps<{
  caseId?: string | number;
  currentUser?: User | null;
}>();

const emit = defineEmits(['navigate']);

const activeTab = ref<'overview' | 'clues' | 'chat' | 'results'>('overview');
const hasJoined = ref(false);
const showSolutionForm = ref(false);const expandedCharacterId = ref(null);const solutionData = ref({
  suspect: '',
  motive: '',
  method: '',
  justification: ''
});
const showGroundTruth = ref(false);

const caseData = ref<any>(null);
const publishedClues = ref<any[]>([]);
const allSolutions = ref<any[]>([]);

const userSolution = computed(() => {
  if (!props.currentUser || !caseData.value) return null;
  if (caseData.value.userSolution) return caseData.value.userSolution;
  const sol = allSolutions.value.find(s => String(s.userId || s.user?.id) === String(props.currentUser?.id) || s.user?.username === props.currentUser?.nick || s.user?.nick === props.currentUser?.nick);
  if (sol) return sol;
  return MOCK_SOLUTIONS.find(s => String(s.caseId) === String(caseData.value.id) && String(s.userId) === String(props.currentUser?.id));
});

const sortedSolutions = computed(() => {
  return [...allSolutions.value].sort((a, b) => (b.score ?? 0) - (a.score ?? 0));
});

function getSolUser(sol: any) {
  if (sol.user) return sol.user;
  return MOCK_USERS.find(u => String(u.id) === String(sol.userId));
}

function getSolUserNick(sol: any) {
  const u = getSolUser(sol);
  return u?.nick || u?.username || 'Неизвестен';
}

function getSolUserInitial(sol: any) {
  const nick = getSolUserNick(sol);
  return nick ? nick[0].toUpperCase() : '?';
}

function isWinner(sol: any) {
  return sol.place === 1;
}

const hideDetails = computed(() => !caseData.value);



const chatMessages = ref<any[]>([]);
const newChatMessage = ref('');

const loadChatMessages = async (id: string | number) => {
  try {
    const res = await apiClient.get(`/cases/${id}/chat`);
    chatMessages.value = res.data;
  } catch (e) {
    console.error('Failed to load chat messages', e);
  }
};

const sendChatMessage = async () => {
  if (!newChatMessage.value.trim() || isClosed.value || !props.currentUser) return;
  try {
    await apiClient.post(`/cases/${props.caseId}/chat`, { content: newChatMessage.value });
    newChatMessage.value = '';
  } catch (e) {
    console.error('Failed to send chat message', e);
  }
};

const subscriptions = ref<any[]>([]);

const subscribeToCaseEvents = (id: string) => {
  subscriptions.value.forEach(sub => sub.unsubscribe());
  subscriptions.value = [];

  onStompConnect(() => {
    // Chat subscription
    subscriptions.value.push(stompClient.subscribe(`/topic/case/${id}/chat`, (message) => {
      const chatMsg = JSON.parse(message.body);
      if (!chatMessages.value.find((m: any) => m.id === chatMsg.id)) {
        chatMessages.value.push(chatMsg);
      }
    }));

    // Clues subscription
    subscriptions.value.push(stompClient.subscribe(`/topic/case/${id}/clues`, (message) => {
      const clue = JSON.parse(message.body);
      // add or override clue
      const index = publishedClues.value.findIndex(c => c.id === clue.id);
      if (index !== -1) {
        publishedClues.value[index] = clue;
      } else {
        publishedClues.value.push(clue);
      }
    }));

    // Case Status subscription
    subscriptions.value.push(stompClient.subscribe(`/topic/case/${id}/status`, (message) => {
      const status = JSON.parse(message.body); // expecting string "CLOSED" or "SCORED"
      if (caseData.value) {
        caseData.value.status = status;
      }
    }));
  });
};

onMounted(() => {
  if (props.caseId) {
    loadCase(String(props.caseId));
    loadChatMessages(String(props.caseId));
    subscribeToCaseEvents(String(props.caseId));
  }
});

onUnmounted(() => {
  subscriptions.value.forEach(sub => sub.unsubscribe());
});

watch(() => props.caseId, (newId) => {
  if (newId) {
    loadCase(String(newId));
    loadChatMessages(String(newId));
    subscribeToCaseEvents(String(newId));
  }
});

async function loadCase(id: string) {
  try {
    let apiData: any = null;
    try {
      const res = await apiClient.get(`/cases/${id}`);
      apiData = res.data;
    } catch {}

const mockCase = MOCK_CASES.find(c => String(c.id) === String(id));

    if (apiData) {
      let diffVal = 3;
      if (typeof apiData.difficulty === 'number') diffVal = apiData.difficulty;
      else if (typeof apiData.difficulty === 'string') {
        const dl = apiData.difficulty.toLowerCase();
        if (dl.includes('easy') || dl.includes('1')) diffVal = 1;
        else if (dl.includes('2')) diffVal = 2;
        else if (dl.includes('3')) diffVal = 3;
        else if (dl.includes('hard') || dl.includes('4')) diffVal = 4;
        else if (dl.includes('expert') || dl.includes('5')) diffVal = 5;
      }

      caseData.value = {
        ...(mockCase || {}), ...apiData,
        title: apiData.title || apiData.theme || mockCase?.title,
          genre: apiData.theme || apiData.genre || mockCase?.theme || mockCase?.genre || 'Классический детектив',      
        difficulty: diffVal ?? mockCase?.difficulty,
        mode: (apiData.mode || mockCase?.mode || '').toLowerCase(),
        status: (apiData.status || mockCase?.status || '').toLowerCase(),        
        tags: apiData.tags?.length ? apiData.tags : mockCase?.tags?.length ? mockCase.tags : ['детектив', 'расследование'],
        characters: apiData.suspects?.length ? apiData.suspects : apiData.characters?.length ? apiData.characters : mockCase?.characters,
        clues: apiData.clues?.length ? apiData.clues : (mockCase?.clues || []),
        participants: Array.isArray(apiData.participants) ? apiData.participants : (mockCase?.participants || [])
      };
    } else if (mockCase) {
      caseData.value = {
        ...mockCase,
        mode: (mockCase?.mode || '').toLowerCase(),
        status: (mockCase?.status || '').toLowerCase()
      };
    }

    if (props.currentUser && caseData.value.participants) {
       hasJoined.value = caseData.value.participants.some((p: any) => 
         String(p) === String(props.currentUser?.id) || String(p.id) === String(props.currentUser?.id) || p.username === props.currentUser?.nick || p.nick === props.currentUser?.nick
       );
    } else {
       hasJoined.value = false;
    }

    // Load clues
    try {
      const cluesRes = await apiClient.get(`/cases/${id}/clues`);
      publishedClues.value = cluesRes.data.length ? cluesRes.data : caseData.value.clues.filter((c: any) => c.status === 'published' || c.isPublished === true || c.published || (!c.revealAt && String(c.status) !== 'hidden') || (c.revealAt && new Date(c.revealAt).getTime() <= Date.now()));
    } catch (e) {
      publishedClues.value = caseData.value.clues.filter((c: any) => c.status === 'published' || c.isPublished === true || c.published || (!c.revealAt && String(c.status) !== 'hidden') || (c.revealAt && new Date(c.revealAt).getTime() <= Date.now()));
    }
    
    // Load solutions
    try {
      const solRes = await apiClient.get(`/cases/${id}/solutions`);
      allSolutions.value = solRes.data.length ? solRes.data : MOCK_SOLUTIONS.filter(s => String(s.caseId) === String(id));
    } catch(e) {
      allSolutions.value = MOCK_SOLUTIONS.filter(s => String(s.caseId) === String(id));
    }
    
    if (props.currentUser) {
      const userSol = allSolutions.value.find((s: any) => String(s.userId || s.user?.id) === String(props.currentUser?.id) || s.user?.username === props.currentUser?.nick || s.user?.nick === props.currentUser?.nick);
      if (userSol) caseData.value.userSolution = userSol;
    }
  } catch (e) {
    console.error('Failed to load case details', e);
  }
}

const hiddenCluesCount = computed(() => caseData.value ? (caseData.value.clues?.length || publishedClues.value.length || 0) - publishedClues.value.length : 0);
const isActive = computed(() => caseData.value ? ['active', 'ready'].includes(String(caseData.value.status).toLowerCase()) : false);
const isClosed = computed(() => caseData.value ? ['closed', 'scoring', 'scored', 'failed'].includes(String(caseData.value.status).toLowerCase()) : false);
const isScored = computed(() => caseData.value ? ['closed', 'scored'].includes(String(caseData.value.status).toLowerCase()) : false);

const publishedCluesSorted = computed(() => [...publishedClues.value].sort((a, b) => (a.importance || a.importanceRank || 0) - (b.importance || b.importanceRank || 0)));

const clueTypeLabels: Record<string, string> = {
  physical: 'Физическая',
  testimonial: 'Показания',
  documentary: 'Документальная',
  digital: 'Цифровая',
  behavioral: 'Поведенческая',
};

const clueTypeColors: Record<string, string> = {
  physical: 'border-chart-4/40 bg-chart-4/5',
  testimonial: 'border-chart-5/40 bg-chart-5/5',
  documentary: 'border-chart-1/40 bg-chart-1/5',
  digital: 'border-chart-2/40 bg-chart-2/5',
  behavioral: 'border-chart-3/40 bg-chart-3/5',
};

const tabs = computed(() => [
  { id: 'overview', label: 'Обзор', icon: Info },
  { id: 'clues', label: `Улики (${publishedClues.value.length})`, icon: BookOpen },
  { id: 'chat', label: 'Чат', icon: MessageCircle },
  { id: 'results', label: 'Результаты', icon: ListChecks },
]);

function formatDate(dateStr: string): string {
  if (!dateStr) return '';
  return new Date(dateStr).toLocaleString('ru-RU', {
    day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit',
  });
}

async function handleJoin() {
  if (!props.currentUser) return emit('navigate', 'auth');
  if (!caseData.value) return;
  try {
    const res = await apiClient.post(`/cases/${caseData.value.id}/join`);
    hasJoined.value = true;
    if (res.data && Array.isArray(res.data.participants)) {
      caseData.value.participants = res.data.participants;
    } else {
      await loadCase(String(caseData.value.id));
    }
  } catch (e) {
    console.error('Failed to join', e);
  }
}

async function handleLeave() {
  if (!caseData.value) return;
  try {
    const res = await apiClient.post(`/cases/${caseData.value.id}/leave`);
    hasJoined.value = false;
    if (res.data && Array.isArray(res.data.participants)) {
      caseData.value.participants = res.data.participants;
    } else {
      await loadCase(String(caseData.value.id));
    }
  } catch(e) {
    console.error('Failed to leave', e);
  }
}

async function submitSolution() {
  if (!caseData.value) return;
  try {
    await apiClient.post(`/cases/${caseData.value.id}/solutions`, solutionData.value);
    showSolutionForm.value = false;
    alert('Решение успешно отправлено!');
    await loadCase(String(caseData.value.id));
  } catch (e) {
    console.error('Failed to submit solution', e);
    alert('Ошибка при отправке решения');
  }
}
</script>


