<template>
  <div data-cmp="AuthPage" class="min-h-[calc(100vh-80px)] flex items-center justify-center px-4 py-12">
    <div class="w-full max-w-md">
      <!-- Logo -->
      <div class="text-center mb-8">
        <div class="text-4xl mb-3">🔍</div>
        <h1 class="text-2xl font-bold text-foreground">Detective Platform</h1>
        <p class="text-muted-foreground text-sm mt-1">Войдите чтобы начать расследования</p>
      </div>

      <!-- Card -->
      <div class="bg-card border border-border rounded-2xl p-8 shadow-custom">
        <!-- Tabs -->
        <div v-if="tab !== 'reset'" class="flex bg-muted rounded-xl p-1 mb-6">
          <button
            v-for="t in ['login', 'register']"
            :key="t"
            @click="tab = t as 'login' | 'register'"
            :class="{
              'bg-card text-foreground shadow-custom': tab === t,
              'text-muted-foreground hover:text-foreground': tab !== t
            }"
            class="flex-1 py-2 rounded-lg text-sm font-medium transition-all cursor-pointer"
          >
            {{ t === 'login' ? 'Вход' : 'Регистрация' }}
          </button>
        </div>

        <!-- Login form -->
        <form v-if="tab === 'login'" @submit.prevent="handleLogin" class="flex flex-col gap-4">
          <div>
            <label class="block text-xs font-medium text-muted-foreground mb-1.5 uppercase tracking-wide">Email</label>
            <div class="relative">
              <Mail :size="14" class="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
              <input
                v-model="loginForm.email"
                type="email"
                placeholder="detective@example.com"
                class="w-full pl-9 pr-3 py-2.5 bg-input border border-border rounded-xl text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary/50"
              />
            </div>
          </div>
          <div>
            <label class="block text-xs font-medium text-muted-foreground mb-1.5 uppercase tracking-wide">Пароль</label>
            <div class="relative">
              <Lock :size="14" class="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
              <input
                v-model="loginForm.password"
                :type="showPass ? 'text' : 'password'"
                placeholder="••••••••"
                class="w-full pl-9 pr-10 py-2.5 bg-input border border-border rounded-xl text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary/50"
              />
              <button
                type="button"
                @click="showPass = !showPass"
                class="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
              >
                <EyeOff v-if="showPass" :size="14" />
                <Eye v-else :size="14" />
              </button>
            </div>
          </div>
          <button
            type="button"
            @click="tab = 'reset'"
            class="text-xs text-primary hover:underline text-left"
          >
            Забыли пароль?
          </button>
          <button
            type="submit"
            :disabled="loading"
            class="w-full py-3 bg-primary text-primary-foreground rounded-xl font-semibold text-sm hover:opacity-90 disabled:opacity-60 transition-opacity mt-2"
          >
            {{ loading ? 'Вход...' : 'Войти' }}
          </button>
          <div class="flex items-center gap-3 my-1">
            <div class="flex-1 h-px bg-border" />
            <span class="text-xs text-muted-foreground">или</span>
            <div class="flex-1 h-px bg-border" />
          </div>
          <p class="text-center text-sm text-muted-foreground">
            Нет аккаунта?
            <button
              type="button"
              @click="tab = 'register'"
              class="text-primary hover:underline"
            >
              Зарегистрироваться
            </button>
          </p>
        </form>

        <!-- Register form -->
        <form v-if="tab === 'register'" @submit.prevent="handleRegister" class="flex flex-col gap-4">
          <div>
            <label class="block text-xs font-medium text-muted-foreground mb-1.5 uppercase tracking-wide">Псевдоним</label>
            <div class="relative">
              <User :size="14" class="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
              <input
                v-model="regForm.nick"
                type="text"
                placeholder="MysteryHunter"
                class="w-full pl-9 pr-3 py-2.5 bg-input border border-border rounded-xl text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary/50"
              />
            </div>
          </div>
          <div>
            <label class="block text-xs font-medium text-muted-foreground mb-1.5 uppercase tracking-wide">Email</label>
            <div class="relative">
              <Mail :size="14" class="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
              <input
                v-model="regForm.email"
                type="email"
                placeholder="detective@example.com"
                class="w-full pl-9 pr-3 py-2.5 bg-input border border-border rounded-xl text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary/50"
              />
            </div>
          </div>
          <div>
            <label class="block text-xs font-medium text-muted-foreground mb-1.5 uppercase tracking-wide">Пароль</label>
            <div class="relative">
              <Lock :size="14" class="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
              <input
                v-model="regForm.password"
                :type="showPass ? 'text' : 'password'"
                placeholder="Минимум 8 символов"
                class="w-full pl-9 pr-10 py-2.5 bg-input border border-border rounded-xl text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary/50"
              />
              <button
                type="button"
                @click="showPass = !showPass"
                class="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
              >
                <EyeOff v-if="showPass" :size="14" />
                <Eye v-else :size="14" />
              </button>
            </div>
          </div>
          <div>
            <label class="block text-xs font-medium text-muted-foreground mb-1.5 uppercase tracking-wide">Подтвердите пароль</label>
            <div class="relative">
              <Lock :size="14" class="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
              <input
                v-model="regForm.confirm"
                :type="showPass ? 'text' : 'password'"
                placeholder="••••••••"
                class="w-full pl-9 pr-3 py-2.5 bg-input border border-border rounded-xl text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary/50"
              />
            </div>
          </div>
          <button
            type="submit"
            :disabled="loading"
            class="w-full py-3 bg-primary text-primary-foreground rounded-xl font-semibold text-sm hover:opacity-90 disabled:opacity-60 transition-opacity mt-2"
          >
            {{ loading ? 'Создание аккаунта...' : 'Зарегистрироваться' }}
          </button>
        </form>

        <!-- Reset form -->
        <form v-if="tab === 'reset'" @submit.prevent="handleReset" class="flex flex-col gap-4">
          <button
            type="button"
            @click="tab = 'login'"
            class="flex items-center gap-1 text-xs text-muted-foreground hover:text-foreground mb-2 w-fit cursor-pointer"
          >
            ← Вернуться ко входу
          </button>
          <div class="text-center mb-2">
            <h2 class="font-bold text-foreground text-lg">Сброс пароля</h2>
            <p class="text-sm text-muted-foreground mt-1">Введите email и мы отправим инструкции</p>
          </div>
          <div>
            <label class="block text-xs font-medium text-muted-foreground mb-1.5 uppercase tracking-wide">Email</label>
            <div class="relative">
              <Mail :size="14" class="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
              <input
                v-model="resetEmail"
                type="email"
                placeholder="detective@example.com"
                class="w-full pl-9 pr-3 py-2.5 bg-input border border-border rounded-xl text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary/50"
              />
            </div>
          </div>
          <button
            type="submit"
            :disabled="loading"
            class="w-full py-3 bg-primary text-primary-foreground rounded-xl font-semibold text-sm hover:opacity-90 disabled:opacity-60 transition-opacity flex items-center justify-center gap-2"
          >
            <span v-if="!loading">Отправить инструкцию</span>
            <span v-else>Отправка...</span>
            <ChevronRight v-if="!loading" :size="16" />
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Eye, EyeOff, Lock, Mail, User, ChevronRight } from 'lucide-vue-next'

const props = defineProps<{
  initialTab?: 'login' | 'register' | 'reset'
}>()

const emit = defineEmits(['navigate', 'login'])

const tab = ref<'login' | 'register' | 'reset'>(props.initialTab || 'login')
const showPass = ref(false)
const loading = ref(false)

const loginForm = ref({ email: '', password: '' })
const regForm = ref({ nick: '', email: '', password: '', confirm: '' })
const resetEmail = ref('')

const handleLogin = () => {
  if (!loginForm.value.email || !loginForm.value.password) { 
    alert('Заполните все поля')
    return 
  }
  loading.value = true
  setTimeout(() => {
    loading.value = false
    // Using vanilla alert as fallback for sonner if setup not complete
    // toast.success('Добро пожаловать!')
    emit('login')
    emit('navigate', 'home')
  }, 1000)
}

const handleRegister = () => {
  if (!regForm.value.nick || !regForm.value.email || !regForm.value.password) { 
    alert('Заполните все поля')
    return 
  }
  if (regForm.value.password !== regForm.value.confirm) { 
    alert('Пароли не совпадают')
    return 
  }
  if (regForm.value.password.length < 8) { 
    alert('Пароль должен быть не менее 8 символов')
    return 
  }
  loading.value = true
  setTimeout(() => {
    loading.value = false
    emit('login')
    emit('navigate', 'home')
  }, 1200)
}

const handleReset = () => {
  if (!resetEmail.value) {
    alert('Введите email')
    return
  }
  loading.value = true
  setTimeout(() => {
    loading.value = false
    alert('Инструкции отправлены на почту')
    tab.value = 'login'
  }, 1000)
}
</script>