<script setup>
import { computed } from 'vue'
import AppButton from '@/components/common/AppButton.vue'

const props = defineProps({
  modelValue: { type: Array, default: () => [] },
})
const emit = defineEmits(['update:modelValue'])

const tasks = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value),
})

function addTask() {
  tasks.value = [
    ...tasks.value,
    {
      id: crypto.randomUUID ? crypto.randomUUID() : `task-${Date.now()}-${tasks.value.length}`,
      code: '',
      libelle: '',
      obligatoire: true,
      roleResponsable: 'RH',
      echeanceJours: 0,
      ordre: tasks.value.length + 1,
    },
  ]
}

function removeTask(index) {
  tasks.value = tasks.value.filter((_, taskIndex) => taskIndex !== index)
}
</script>

<template>
  <section class="editor">
    <div class="editor__header">
      <h3>Tâches du modèle</h3>
      <AppButton type="button" variant="secondary" @click="addTask">Ajouter une tâche</AppButton>
    </div>

    <div v-if="!tasks.length" class="editor__empty">Aucune tâche ajoutée.</div>

    <div v-for="(task, index) in tasks" :key="task.id ?? `${task.code}-${index}`" class="task-card">
      <div class="task-card__row">
        <label>
          Ordre
          <input v-model.number="task.ordre" type="number" min="1" />
        </label>
        <label>
          Code
          <input v-model="task.code" type="text" placeholder="CONTRAT_SIGNE" />
        </label>
        <label>
          Responsable
          <select v-model="task.roleResponsable">
            <option value="RH">RH</option>
            <option value="IT_SERVICES">IT / Services généraux</option>
            <option value="PAIE">Paie</option>
            <option value="MANAGER">Manager</option>
          </select>
        </label>
      </div>
      <div class="task-card__row">
        <label class="task-card__wide">
          Libellé
          <input v-model="task.libelle" type="text" placeholder="Libellé de la tâche" />
        </label>
      </div>
      <div class="task-card__row">
        <label>
          Échéance (jours)
          <input v-model.number="task.echeanceJours" type="number" />
        </label>
        <label class="checkbox">
          <input v-model="task.obligatoire" type="checkbox" />
          <span>Obligatoire</span>
        </label>
      </div>
      <div class="task-card__actions">
        <AppButton type="button" variant="secondary" @click="removeTask(index)">Supprimer</AppButton>
      </div>
    </div>
  </section>
</template>

<style scoped>
.editor {
  display: grid;
  gap: 1rem;
}
.editor__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}
.editor__header h3 {
  margin: 0;
}
.editor__empty {
  color: var(--color-text-muted);
}
.task-card {
  display: grid;
  gap: 1rem;
  padding: 1rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-surface);
}
.task-card__row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 0.75rem;
}
.task-card__wide {
  grid-column: 1 / -1;
}
.task-card label {
  display: grid;
  gap: 0.45rem;
  color: var(--color-text-muted);
  font-size: 0.8rem;
}
.task-card input,
.task-card select {
  min-height: var(--control-height);
  padding: 0 0.75rem;
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text);
  font: inherit;
}
.checkbox {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  align-self: end;
  min-height: var(--control-height);
}
.checkbox input {
  width: 1rem;
  height: 1rem;
}
.task-card__actions {
  display: flex;
  justify-content: flex-end;
}
</style>
