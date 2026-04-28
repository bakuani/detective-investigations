import './assets/index.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'

const globalScope = globalThis as typeof globalThis & { global?: typeof globalThis }
if (typeof globalScope.global === 'undefined') {
  globalScope.global = globalThis
}

const app = createApp(App)

app.use(createPinia())

app.mount('#app')
