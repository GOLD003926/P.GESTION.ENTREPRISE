<script setup>
import { onMounted, reactive, ref } from 'vue'
import { affectationsApi } from '@/api/affectations'
import { employesApi } from '@/api/employes'
import { postesApi } from '@/api/postes'
import { MOTIF_AFFECTATION_LABELS } from '@/constants/statuts'
import AppAlert from '@/components/common/AppAlert.vue'
import AppButton from '@/components/common/AppButton.vue'

const props = defineProps({
  employeId: { type: [String, Number], required: true },
  loading: { type: Boolean, default: false },
})
const emit = defineEmits(['created', 'cancel'])
const postes = ref([])
const managers = ref([])
const loadingOptions = ref(true)
const saving = ref(false)
const error = ref('')
const form = reactive({
  posteId: '',
  managerId: '',
  dateDebut: new Date().toISOString().slice(0, 10),
  tauxOccupation: 100,
  motif: 'MUTATION',
  cloturerActives: false,
})

async function loadOptions() {
  try {
    const [postesPage, employeesPage] = await Promise.all([
      postesApi.rechercher({ actif: true, page: 0, size: 100 }),
      employesApi.rechercher({ statut: 'ACTIF', page: 0, size: 100 }),
    ])
    postes.value = postesPage.content ?? []
    managers.value = (employeesPage.content ?? []).filter(
      (employe) => String(employe.id) !== String(props.employeId),
    )
  } catch (cause) {
    error.value = cause.message
  } finally {
    loadingOptions.value = false
  }
}

async function submit() {
  error.value = ''
  saving.value = true
  try {
    const affectation = await affectationsApi.creer({
      employeId: Number(props.employeId),
      posteId: Number(form.posteId),
      managerId: form.managerId ? Number(form.managerId) : null,
      dateDebut: form.dateDebut,
      tauxOccupation: Number(form.tauxOccupation),
      motif: form.motif,
      cloturerActives: form.cloturerActives,
    })
    emit('created', affectation)
  } catch (cause) {
    error.value = cause.message
  } finally {
    saving.value = false
  }
}

onMounted(loadOptions)
</script>

<template>
  <form class="assignment-form" @submit.prevent="submit">
    <AppAlert v-if="error" variant="error">{{ error }}</AppAlert>
    <p v-if="loadingOptions" role="status">Chargement des postes et managers…</p>
    <template v-else>
      <label
        >Poste actif
        <select v-model="form.posteId" required>
          <option value="" disabled>Sélectionner un poste</option>
          <option v-for="poste in postes" :key="poste.id" :value="String(poste.id)">
            {{ poste.code }} — {{ poste.intitule }} ({{ poste.departementNom }})
          </option>
        </select>
      </label>
      <label
        >Manager
        <select v-model="form.managerId">
          <option value="">Aucun manager</option>
          <option v-for="manager in managers" :key="manager.id" :value="String(manager.id)">
            {{ manager.prenom }} {{ manager.nom }} · {{ manager.matricule }}
          </option>
        </select>
      </label>
      <label>Date de début<input v-model="form.dateDebut" type="date" required /></label>
      <label
        >Taux d’occupation (%)<input
          v-model.number="form.tauxOccupation"
          type="number"
          min="0.01"
          max="100"
          step="0.01"
          required
      /></label>
      <label
        >Motif<select v-model="form.motif" required>
          <option v-for="(label, value) in MOTIF_AFFECTATION_LABELS" :key="value" :value="value">
            {{ label }}
          </option>
        </select></label
      >
      <label class="close-active"
        ><input v-model="form.cloturerActives" type="checkbox" />Clôturer les affectations actives à
        la veille de cette prise de poste</label
      >
      <p v-if="!postes.length" class="muted">
        Aucun poste actif disponible. Activez ou créez un poste avant d’ajouter une affectation.
      </p>
    </template>
    <footer>
      <AppButton
        type="button"
        variant="secondary"
        :disabled="saving || loading"
        @click="emit('cancel')"
        >Annuler</AppButton
      ><AppButton type="submit" :loading="saving" :disabled="loadingOptions || !postes.length"
        >Créer l’affectation</AppButton
      >
    </footer>
  </form>
</template>

<style scoped>
.assignment-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-4);
}
.assignment-form > label {
  display: grid;
  align-content: start;
  gap: var(--space-2);
  color: var(--color-text);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
}
.assignment-form input:not([type='checkbox']),
.assignment-form select {
  min-height: var(--control-height);
  padding: 0 var(--space-3);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text);
  font: inherit;
  font-weight: var(--font-weight-normal);
}
.close-active,
.assignment-form footer,
.muted {
  grid-column: 1 / -1;
}
.assignment-form .close-active {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-weight: var(--font-weight-normal);
}
.assignment-form footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
}
.muted {
  margin: 0;
  color: var(--color-text-muted);
}
@media (max-width: 650px) {
  .assignment-form {
    grid-template-columns: 1fr;
  }
  .close-active,
  .assignment-form footer,
  .muted {
    grid-column: auto;
  }
}
</style>
