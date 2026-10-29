<script setup>
import OnboardingTacheItem from './OnboardingTacheItem.vue'

defineProps({
  tasks: { type: Array, default: () => [] },
})
</script>

<template>
  <div class="checklist">
    <p v-if="!tasks.length" class="empty">Aucune tâche à afficher.</p>
    <ul v-else class="checklist__list">
      <OnboardingTacheItem v-for="task in tasks" :key="task.id ?? task.code" :task="task" />
    </ul>
  </div>
</template>

<style scoped>
.checklist {
  display: grid;
  gap: 1rem;
}
.checklist__list {
  margin: 0;
  padding: 0;
  list-style: none;
  display: grid;
  gap: 0.75rem;
}
.empty {
  margin: 0;
  color: var(--color-text-muted);
}
</style>
