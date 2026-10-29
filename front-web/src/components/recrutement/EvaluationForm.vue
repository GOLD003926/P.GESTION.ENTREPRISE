<script setup>
import { reactive, ref } from 'vue'
import { AVIS_LABELS } from '@/constants/statuts'
import AppAlert from '@/components/common/AppAlert.vue'
import AppButton from '@/components/common/AppButton.vue'

const props = defineProps({
  entretienId: { type: [String, Number], required: true },
  evaluation: { type: Object, default: null },
  loading: { type: Boolean, default: false },
  error: { type: String, default: '' },
})
const emit = defineEmits(['submit', 'cancel'])
const form = reactive({
  noteGlobale: props.evaluation?.noteGlobale ?? '',
  avis: props.evaluation?.avis ?? 'FAVORABLE',
  commentaire: props.evaluation?.commentaire ?? '',
  criteres: props.evaluation?.criteres?.length
    ? props.evaluation.criteres.map((item) => ({ ...item }))
    : [{ critere: '', note: '' }],
})
const validationError = ref('')

function addCriterion() {
  form.criteres.push({ critere: '', note: '' })
}
function submit() {
  if (form.criteres.some((item) => !item.critere.trim() || item.note === '')) {
    validationError.value = 'Chaque critère doit avoir un libellé et une note.'
    return
  }
  validationError.value = ''
  emit('submit', {
    ...(props.evaluation ? {} : { entretienId: Number(props.entretienId) }),
    noteGlobale: Number(form.noteGlobale),
    criteres: form.criteres.map((item) => ({
      critere: item.critere.trim(),
      note: Number(item.note),
    })),
    avis: form.avis,
    commentaire: form.commentaire.trim() || null,
  })
}
</script>

<template>
  <form class="evaluation-form" @submit.prevent="submit">
    <AppAlert v-if="error" variant="error">{{ error }}</AppAlert>
    <AppAlert v-if="validationError" variant="error">{{ validationError }}</AppAlert>
    <label
      >Note globale / 20<input
        v-model.number="form.noteGlobale"
        type="number"
        min="0"
        max="20"
        step="0.1"
        required
    /></label>
    <label
      >Avis<select v-model="form.avis">
        <option v-for="(label, value) in AVIS_LABELS" :key="value" :value="value">
          {{ label }}
        </option>
      </select></label
    >
    <fieldset>
      <legend>Critères</legend>
      <div v-for="(criterion, index) in form.criteres" :key="index" class="criterion">
        <label>Critère<input v-model.trim="criterion.critere" maxlength="100" required /></label>
        <label
          >Note<input
            v-model.number="criterion.note"
            type="number"
            min="0"
            max="20"
            step="0.1"
            required
        /></label>
      </div>
      <button type="button" class="add-criterion" @click="addCriterion">Ajouter un critère</button>
    </fieldset>
    <label>Commentaire<textarea v-model.trim="form.commentaire" maxlength="4000" rows="4" /></label>
    <footer>
      <AppButton type="button" variant="secondary" :disabled="loading" @click="emit('cancel')"
        >Annuler</AppButton
      ><AppButton type="submit" :loading="loading">{{
        evaluation ? 'Modifier' : 'Enregistrer l’évaluation'
      }}</AppButton>
    </footer>
  </form>
</template>

<style scoped>
.evaluation-form {
  display: grid;
  gap: var(--space-4);
}
.evaluation-form label {
  display: grid;
  gap: var(--space-2);
  color: var(--color-text);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
}
.evaluation-form input,
.evaluation-form select,
.evaluation-form textarea {
  min-height: var(--control-height);
  padding: 0 var(--space-3);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text);
  font: inherit;
  font-weight: var(--font-weight-normal);
}
.evaluation-form fieldset {
  display: grid;
  gap: var(--space-3);
  padding: var(--space-4);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
}
.evaluation-form legend {
  padding: 0 var(--space-2);
  color: var(--color-text);
  font-weight: var(--font-weight-semibold);
}
.criterion {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: var(--space-3);
}
.add-criterion {
  justify-self: start;
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-primary);
  font: inherit;
  cursor: pointer;
}
.evaluation-form footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
}
@media (max-width: 600px) {
  .criterion {
    grid-template-columns: 1fr;
  }
}
</style>
