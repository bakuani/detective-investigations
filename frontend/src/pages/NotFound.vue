<template>
  <div class="flex flex-col items-center justify-center min-h-[80vh] px-6 text-center">
    <div class="text-6xl mb-6">🕵️‍♂️</div>
    <h1 class="text-4xl font-bold text-foreground mb-4">404 - Дело закрыто</h1>
    <p class="text-xl text-muted-foreground mb-8 max-w-md">
      Похоже, вы забрели в тупик. Страница, которую вы ищете, была удалена или никогда не существовала.
    </p>
    <button
      @click="emit('navigate', 'home')"
      class="px-6 py-3 bg-primary text-primary-foreground rounded-xl font-semibold hover:opacity-90 transition-opacity"
    >
      Вернуться в штаб-квартиру
    </button>
  </div>
</template>

<script setup lang="ts">
import type { AppPage } from '../store/appStore'

const emit = defineEmits<{
  (e: 'navigate', page: AppPage): void
}>()
</script>