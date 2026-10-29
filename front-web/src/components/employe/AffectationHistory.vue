<script setup>
import { computed, reactive, ref } from 'vue'
import { affectationsApi } from '@/api/affectations'
import { MOTIF_AFFECTATION_LABELS } from '@/constants/statuts'
import AppAlert from '@/components/common/AppAlert.vue'
import AppButton from '@/components/common/AppButton.vue'

const props = defineProps({
  affectations: { type: Array, required: true },
  canManage: { type: Boolean, default: false },
})
const emit = defineEmits(['updated'])
const ordered = computed(() =>
  [...props.affectations].sort((a, b) => new Date(b.dateDebut) - new Date(a.dateDebut)),
)
const closingId = ref(null)
const savingId = ref(null)
const error = ref('')
const datesFin = reactive({})

function startClosing(affectation) {
  closingId.value = closingId.value === affectation.id ? null : affectation.id
  const today = new Date().toISOString().slice(0, 10)
  datesFin[affectation.id] = today < affectation.dateDebut ? affectation.dateDebut : today
}
async function closeAssignment(id) {
  error.value = ''
  savingId.value = id
  try {
    await affectationsApi.cloturer(id, { dateFin: datesFin[id] })
    closingId.value = null
    emit('updated')
  } catch (cause) {
    error.value = cause.message
  } finally {
    savingId.value = null
  }
}
</script>

<template>
  <div class="history">
    <AppAlert v-if="error" variant="error">{{ error }}</AppAlert>
    <p v-if="!ordered.length" class="empty">Aucune affectation enregistrée.</p>
    <ol v-else>
      <li
        v-for="affectation in ordered"
        :key="affectation.id"
        :class="{ current: affectation.active }"
      >
        <span class="marker" aria-hidden="true" />
        <article>
          <header>
            <div>
              <strong>{{ affectation.posteIntitule }}</strong
              ><span v-if="affectation.active" class="active">Affectation active</span>
            </div>
            <AppButton
              v-if="canManage && affectation.active"
              type="button"
              variant="secondary"
              @click="startClosing(affectation)"
              >{{ closingId === affectation.id ? 'Annuler la clôture' : 'Clôturer' }}</AppButton
            >
          </header>
          <p>
            {{ affectation.departementNom || 'Département non précisé' }} ·
            {{ MOTIF_AFFECTATION_LABELS[affectation.motif] ?? affectation.motif }}
          </p>
          <p>
            {{ affectation.dateDebut }} — {{ affectation.dateFin || 'En cours' }} ·
            {{ affectation.tauxOccupation }} %
          </p>
          <p v-if="affectation.managerNom">Manager : {{ affectation.managerNom }}</p>
          <form
            v-if="closingId === affectation.id"
            class="close-form"
            @submit.prevent="closeAssignment(affectation.id)"
          >
            <label
              >Date de fin<input
                v-model="datesFin[affectation.id]"
                type="date"
                :min="affectation.dateDebut"
                required
            /></label>
            <AppButton type="submit" :loading="savingId === affectation.id"
              >Confirmer la clôture</AppButton
            >
          </form>
        </article>
      </li>
    </ol>
  </div>
</template>

<style scoped>
.history ol {
  display: grid;
  gap: 0;
  margin: 0;
  padding: 0;
  list-style: none;
}
.history li {
  position: relative;
  display: grid;
  grid-template-columns: 20px minmax(0, 1fr);
  gap: var(--space-3);
  padding-bottom: var(--space-4);
}
.history li:not(:last-child)::before {
  position: absolute;
  top: 14px;
  bottom: 0;
  left: 7px;
  width: 2px;
  background: var(--color-border);
  content: '';
}
.marker {
  z-index: 1;
  width: 16px;
  height: 16px;
  margin-top: 4px;
  border: 3px solid var(--color-border-strong);
  border-radius: 50%;
  background: var(--color-surface);
}
.current .marker {
  border-color: var(--color-success);
}
.history article {
  min-width: 0;
  padding: var(--space-3);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
}
.history header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
}
.history header div {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-2);
  color: var(--color-text);
}
.history article p {
  margin: var(--space-2) 0 0;
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}
.active {
  padding: var(--space-1) var(--space-2);
  border-radius: 999px;
  background: var(--color-success-soft);
  color: var(--color-success);
  font-size: 0.75rem;
  font-weight: var(--font-weight-semibold);
}
.close-form {
  display: flex;
  align-items: end;
  flex-wrap: wrap;
  gap: var(--space-3);
  margin-top: var(--space-3);
  padding-top: var(--space-3);
  border-top: 1px solid var(--color-border);
}
.close-form label {
  display: grid;
  gap: var(--space-1);
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}
.close-form input {
  min-height: var(--control-height);
  padding: 0 var(--space-3);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text);
  font: inherit;
}
.empty {
  color: var(--color-text-muted);
}
</style>
