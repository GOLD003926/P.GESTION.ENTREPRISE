<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { departementsApi } from '@/api/departements'
import { grillesSalarialesApi } from '@/api/grillesSalariales'
import { postesApi } from '@/api/postes'
import OrganisationFormPanel from '@/components/organisation/OrganisationFormPanel.vue'
import { ROUTE_NAMES } from '@/constants/routeNames'

const props = defineProps({ id: { type: String, default: null } })
const router = useRouter()
const isEdit = computed(() => Boolean(props.id))
const form = reactive({
  code: '',
  intitule: '',
  description: '',
  competencesRequises: '',
  departementId: '',
  grilleSalarialeId: '',
  effectifAutorise: '',
})
const displayCode = ref('')
const currentOccupation = ref(0)
const departments = ref([])
const salaryGrids = ref([])
const loading = ref(true)
const saving = ref(false)
const loadFailed = ref(false)
const error = ref('')
const fieldErrors = ref({})

onMounted(async () => {
  try {
    const requests = [
      departementsApi.rechercher({ page: 0, size: 100, actif: true, sort: 'nom,asc' }),
      grillesSalarialesApi.rechercher({ page: 0, size: 100, actif: true, sort: 'intitule,asc' }),
    ]
    if (isEdit.value) requests.push(postesApi.obtenir(props.id))
    const [departmentPage, gridPage, position] = await Promise.all(requests)
    departments.value = departmentPage.content
    salaryGrids.value = gridPage.content
    if (position) {
      displayCode.value = position.code
      form.intitule = position.intitule
      form.description = position.description ?? ''
      form.competencesRequises = position.competencesRequises ?? ''
      form.departementId = String(position.departementId)
      form.grilleSalarialeId = String(position.grilleSalarialeId)
      form.effectifAutorise = String(position.effectifAutorise)
      currentOccupation.value = position.occupationActuelle
      if (!departments.value.some((item) => item.id === position.departementId)) {
        departments.value.push({
          id: position.departementId,
          code: '',
          nom: `${position.departementNom} (actuellement associé)`,
          inactif: true,
        })
      }
      if (!salaryGrids.value.some((item) => item.id === position.grilleSalarialeId)) {
        salaryGrids.value.push({
          id: position.grilleSalarialeId,
          code: '',
          intitule: `${position.grilleSalarialeIntitule} (actuellement associée)`,
          inactif: true,
        })
      }
    }
  } catch (cause) {
    error.value = cause.message
    loadFailed.value = true
  } finally {
    loading.value = false
  }
})

function messageFor(field) {
  return fieldErrors.value[field] ?? ''
}

async function submit() {
  error.value = ''
  fieldErrors.value = {}
  const authorizedCount = Number(form.effectifAutorise)
  if (isEdit.value && authorizedCount < currentOccupation.value) {
    fieldErrors.value = {
      effectifAutorise: `L’effectif autorisé ne peut pas être inférieur à l’occupation actuelle (${currentOccupation.value}).`,
    }
    return
  }

  saving.value = true
  const data = {
    ...(isEdit.value ? {} : { code: form.code.trim().toUpperCase() }),
    intitule: form.intitule.trim(),
    description: form.description.trim() || null,
    competencesRequises: form.competencesRequises.trim() || null,
    departementId: Number(form.departementId),
    grilleSalarialeId: Number(form.grilleSalarialeId),
    effectifAutorise: authorizedCount,
  }
  try {
    if (isEdit.value) await postesApi.modifier(props.id, data)
    else await postesApi.creer(data)
    await router.push({ name: ROUTE_NAMES.POSTES })
  } catch (cause) {
    error.value = cause.message
    fieldErrors.value = cause.fieldErrors ?? {}
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <OrganisationFormPanel
    :title="isEdit ? 'Modifier le poste' : 'Créer un poste'"
    description="Un poste est défini indépendamment de ses occupants et dépend d’un département et d’une grille actifs."
    :loading="loading"
    :saving="saving"
    :load-failed="loadFailed"
    :error="error"
    :field-errors="fieldErrors"
    :cancel-route="{ name: ROUTE_NAMES.POSTES }"
    @submit="submit"
  >
    <div class="organisation-form organisation-form__grid">
      <div class="organisation-form__field">
        <label for="position-code">Code</label>
        <input
          v-if="!isEdit"
          id="position-code"
          v-model="form.code"
          class="organisation-form__control"
          name="code"
          maxlength="30"
          pattern="[A-Za-z0-9_\x2d]+"
          autocomplete="off"
          required
          :aria-invalid="Boolean(messageFor('code'))"
          :aria-describedby="messageFor('code') ? 'position-code-error' : 'position-code-hint'"
          @input="form.code = $event.target.value.toUpperCase()"
        />
        <input
          v-else
          id="position-code"
          class="organisation-form__control"
          :value="displayCode"
          readonly
          aria-describedby="position-code-hint"
        />
        <p v-if="messageFor('code')" id="position-code-error" class="organisation-form__error">
          {{ messageFor('code') }}
        </p>
        <p v-else id="position-code-hint" class="organisation-form__hint">
          30 caractères maximum : lettres, chiffres, tiret ou underscore.
        </p>
      </div>

      <div class="organisation-form__field">
        <label for="position-title">Intitulé du poste</label>
        <input
          id="position-title"
          v-model="form.intitule"
          class="organisation-form__control"
          name="intitule"
          maxlength="150"
          required
          :aria-invalid="Boolean(messageFor('intitule'))"
          :aria-describedby="messageFor('intitule') ? 'position-title-error' : undefined"
        />
        <p v-if="messageFor('intitule')" id="position-title-error" class="organisation-form__error">
          {{ messageFor('intitule') }}
        </p>
      </div>

      <div class="organisation-form__field">
        <label for="position-department">Département</label>
        <select
          id="position-department"
          v-model="form.departementId"
          class="organisation-form__control"
          name="departementId"
          required
          :aria-invalid="Boolean(messageFor('departementId'))"
          :aria-describedby="messageFor('departementId') ? 'position-department-error' : undefined"
        >
          <option value="" disabled>Sélectionner un département actif</option>
          <option
            v-for="department in departments"
            :key="department.id"
            :value="String(department.id)"
            :disabled="department.inactif"
          >
            {{ department.code ? `${department.code} — ` : '' }}{{ department.nom }}
          </option>
        </select>
        <p
          v-if="messageFor('departementId')"
          id="position-department-error"
          class="organisation-form__error"
        >
          {{ messageFor('departementId') }}
        </p>
      </div>

      <div class="organisation-form__field">
        <label for="position-grid">Grille salariale</label>
        <select
          id="position-grid"
          v-model="form.grilleSalarialeId"
          class="organisation-form__control"
          name="grilleSalarialeId"
          required
          :aria-invalid="Boolean(messageFor('grilleSalarialeId'))"
          :aria-describedby="messageFor('grilleSalarialeId') ? 'position-grid-error' : undefined"
        >
          <option value="" disabled>Sélectionner une grille active</option>
          <option
            v-for="grid in salaryGrids"
            :key="grid.id"
            :value="String(grid.id)"
            :disabled="grid.inactif"
          >
            {{ grid.code ? `${grid.code} — ` : '' }}{{ grid.intitule }}
          </option>
        </select>
        <p
          v-if="messageFor('grilleSalarialeId')"
          id="position-grid-error"
          class="organisation-form__error"
        >
          {{ messageFor('grilleSalarialeId') }}
        </p>
      </div>

      <div class="organisation-form__field">
        <label for="position-capacity">Effectif autorisé</label>
        <input
          id="position-capacity"
          v-model="form.effectifAutorise"
          class="organisation-form__control"
          name="effectifAutorise"
          type="number"
          min="1"
          max="1000"
          step="1"
          required
          :aria-invalid="Boolean(messageFor('effectifAutorise'))"
          :aria-describedby="
            messageFor('effectifAutorise') ? 'position-capacity-error' : 'position-capacity-hint'
          "
        />
        <p
          v-if="messageFor('effectifAutorise')"
          id="position-capacity-error"
          class="organisation-form__error"
        >
          {{ messageFor('effectifAutorise') }}
        </p>
        <p v-else id="position-capacity-hint" class="organisation-form__hint">
          Entre 1 et 1 000. Occupation actuelle : {{ currentOccupation }}.
        </p>
      </div>

      <div class="organisation-form__field organisation-form__field--full">
        <label for="position-description">Description (facultatif)</label>
        <textarea
          id="position-description"
          v-model="form.description"
          class="organisation-form__control"
          name="description"
          maxlength="4000"
          :aria-invalid="Boolean(messageFor('description'))"
          :aria-describedby="messageFor('description') ? 'position-description-error' : undefined"
        />
        <p
          v-if="messageFor('description')"
          id="position-description-error"
          class="organisation-form__error"
        >
          {{ messageFor('description') }}
        </p>
      </div>

      <div class="organisation-form__field organisation-form__field--full">
        <label for="position-skills">Compétences requises (facultatif)</label>
        <textarea
          id="position-skills"
          v-model="form.competencesRequises"
          class="organisation-form__control"
          name="competencesRequises"
          maxlength="4000"
          :aria-invalid="Boolean(messageFor('competencesRequises'))"
          :aria-describedby="
            messageFor('competencesRequises') ? 'position-skills-error' : undefined
          "
        />
        <p
          v-if="messageFor('competencesRequises')"
          id="position-skills-error"
          class="organisation-form__error"
        >
          {{ messageFor('competencesRequises') }}
        </p>
      </div>
    </div>
  </OrganisationFormPanel>
</template>
