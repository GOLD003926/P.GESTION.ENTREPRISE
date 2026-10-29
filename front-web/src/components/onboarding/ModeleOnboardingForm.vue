<script setup>
import { reactive, watch } from 'vue'
import AppButton from '@/components/common/AppButton.vue'
import ModeleTacheEditor from './ModeleTacheEditor.vue'

const props = defineProps({
  modele: { type: Object, default: null },
})

const emit = defineEmits(['submit'])

const form = reactive({
  nom: '',
  typeContrat: '',
  actif: true,
  taches: [],
})

watch(
  () => props.modele,
  (value) => {
    form.nom = value?.nom ?? ''
    form.typeContrat = value?.typeContrat ?? ''
    form.actif = value?.actif ?? true
    form.taches = (value?.taches ?? []).map((tache, index) => ({
      ...tache,
      id: tache.id ?? `${index}-${tache.code ?? 'task'}`,
      obligatoire: tache.obligatoire ?? true,
      roleResponsable: tache.roleResponsable ?? 'RH',
      echeanceJours: tache.echeanceJours ?? 0,
      ordre: tache.ordre ?? index + 1,
    }))
  },
  { immediate: true },
)

function submit() {
  emit('submit', {
    ...form,
    taches: form.taches.map((tache, index) => ({
      ...tache,
      ordre: tache.ordre ?? index + 1,
      obligatoire: !!tache.obligatoire,
      code: String(tache.code ?? '').trim(),
      libelle: String(tache.libelle ?? '').trim(),
    })),
  })
}
</script>

<template>
  <form class="form" @submit.prevent="submit">
    <div class="field-grid">
      <label>
        Nom du modèle
        <input v-model="form.nom" type="text" placeholder="Intégration standard" />
      </label>
      <label>
        Type de contrat
        <select v-model="form.typeContrat">
          <option value="">Modèle par défaut</option>
          <option value="ESSAI">Période d’essai</option>
          <option value="CDD">CDD</option>
          <option value="CDI">CDI</option>
        </select>
      </label>
    </div>

    <label class="checkbox-row">
      <input v-model="form.actif" type="checkbox" />
      <span>Modèle actif</span>
    </label>

    <ModeleTacheEditor v-model="form.taches" />

    <footer class="actions">
      <AppButton type="submit">Enregistrer</AppButton>
    </footer>
  </form>
</template>

<style scoped>
.form {
  display: grid;
  gap: 1.5rem;
}
.field-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 1rem;
}
label {
  display: grid;
  gap: 0.45rem;
  color: var(--color-text-muted);
  font-size: 0.8rem;
}
input,
select {
  min-height: var(--control-height);
  padding: 0 0.75rem;
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text);
  font: inherit;
}
.checkbox-row {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--color-text);
}
.checkbox-row input {
  width: 1rem;
  height: 1rem;
}
.actions {
  display: flex;
  justify-content: flex-end;
}
</style>
