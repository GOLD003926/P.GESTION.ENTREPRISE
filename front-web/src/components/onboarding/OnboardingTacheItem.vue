<script setup>
import AppStatusBadge from '@/components/common/AppStatusBadge.vue'

defineProps({
  task: { type: Object, required: true },
})

function statusTone(task) {
  if (!task) return 'neutral'
  if (task.statut === 'FAIT') return 'success'
  if (task.statut === 'EN_COURS') return 'info'
  if (task.statut === 'NON_APPLICABLE') return 'neutral'
  if (task.enRetard) return 'warning'
  return 'neutral'
}

function statusLabel(task) {
  if (!task) return 'À faire'
  const map = {
    A_FAIRE: 'À faire',
    EN_COURS: 'En cours',
    FAIT: 'Fait',
    NON_APPLICABLE: 'Non applicable',
  }
  return map[task.statut] ?? task.statut ?? 'À faire'
}
</script>

<template>
  <li v-if="task" class="task-item">
    <div class="task-item__header">
      <div>
        <span class="task-item__code">{{ task.code }}</span>
        <h4>{{ task.libelle }}</h4>
      </div>
      <AppStatusBadge :label="statusLabel(task)" :tone="statusTone(task)" />
    </div>

    <div class="task-item__meta">
      <span>Responsable : {{ task.roleResponsable }}</span>
      <span v-if="task.echeance">Échéance : {{ task.echeance }}</span>
      <span v-if="task.enRetard" class="task-item__late">En retard</span>
    </div>

    <p v-if="task.commentaire" class="task-item__commentary">{{ task.commentaire }}</p>
  </li>
</template>

<style scoped>
.task-item {
  display: grid;
  gap: 0.75rem;
  padding: 1rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-surface);
}
.task-item__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 0.75rem;
}
.task-item__header h4 {
  margin: 0.2rem 0 0;
  color: var(--color-text);
  font-size: 1rem;
}
.task-item__code {
  color: var(--color-primary);
  font-size: 0.72rem;
  font-weight: var(--font-weight-semibold);
  letter-spacing: 0.04em;
  text-transform: uppercase;
}
.task-item__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  color: var(--color-text-muted);
  font-size: 0.8rem;
}
.task-item__late {
  color: var(--color-warning);
  font-weight: var(--font-weight-semibold);
}
.task-item__commentary {
  margin: 0;
  color: var(--color-text-muted);
}
</style>
