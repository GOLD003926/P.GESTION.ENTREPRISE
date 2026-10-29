<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { departementsApi } from '@/api/departements'
import OrganisationFormPanel from '@/components/organisation/OrganisationFormPanel.vue'
import { ROUTE_NAMES } from '@/constants/routeNames'

const props = defineProps({ id: { type: String, default: null } })

const router = useRouter()
const isEdit = computed(() => Boolean(props.id))
const form = reactive({
  code: '',
  nom: '',
  departementParentId: '',
  responsableId: '',
})
const displayCode = ref('')
const parentOptions = ref([])
const loading = ref(true)
const saving = ref(false)
const loadFailed = ref(false)
const error = ref('')
const fieldErrors = ref({})

onMounted(async () => {
  try {
    const requests = [
      departementsApi.rechercher({ page: 0, size: 100, actif: true, sort: 'nom,asc' }),
    ]
    if (isEdit.value) requests.push(departementsApi.obtenir(props.id))
    const [parentsPage, department] = await Promise.all(requests)
    parentOptions.value = parentsPage.content.filter(
      (item) => !isEdit.value || String(item.id) !== props.id,
    )
    if (department) {
      displayCode.value = department.code
      form.nom = department.nom
      form.departementParentId = department.departementParentId
        ? String(department.departementParentId)
        : ''
      form.responsableId = department.responsableId ? String(department.responsableId) : ''
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
  saving.value = true
  const data = {
    ...(isEdit.value ? {} : { code: form.code.trim().toUpperCase() }),
    nom: form.nom.trim(),
    departementParentId: form.departementParentId ? Number(form.departementParentId) : null,
    responsableId: form.responsableId ? Number(form.responsableId) : null,
  }
  try {
    if (isEdit.value) await departementsApi.modifier(props.id, data)
    else await departementsApi.creer(data)
    await router.push({ name: ROUTE_NAMES.DEPARTEMENTS })
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
    :title="isEdit ? 'Modifier le département' : 'Créer un département'"
    description="Le code est unique et ne peut plus être modifié après la création."
    :loading="loading"
    :saving="saving"
    :load-failed="loadFailed"
    :error="error"
    :field-errors="fieldErrors"
    :cancel-route="{ name: ROUTE_NAMES.DEPARTEMENTS }"
    @submit="submit"
  >
    <div class="organisation-form organisation-form__grid">
      <div class="organisation-form__field">
        <label for="department-code">Code</label>
        <input
          v-if="!isEdit"
          id="department-code"
          v-model="form.code"
          class="organisation-form__control"
          name="code"
          maxlength="30"
          pattern="[A-Za-z0-9_\x2d]+"
          autocomplete="off"
          required
          :aria-invalid="Boolean(messageFor('code'))"
          :aria-describedby="messageFor('code') ? 'department-code-error' : 'department-code-hint'"
          @input="form.code = $event.target.value.toUpperCase()"
        />
        <input
          v-else
          id="department-code"
          class="organisation-form__control"
          :value="displayCode"
          readonly
          aria-describedby="department-code-hint"
        />
        <p v-if="messageFor('code')" id="department-code-error" class="organisation-form__error">
          {{ messageFor('code') }}
        </p>
        <p v-else id="department-code-hint" class="organisation-form__hint">
          30 caractères maximum : lettres, chiffres, tiret ou underscore.
        </p>
      </div>

      <div class="organisation-form__field">
        <label for="department-name">Nom du département</label>
        <input
          id="department-name"
          v-model="form.nom"
          class="organisation-form__control"
          name="nom"
          maxlength="150"
          required
          :aria-invalid="Boolean(messageFor('nom'))"
          :aria-describedby="messageFor('nom') ? 'department-name-error' : undefined"
        />
        <p v-if="messageFor('nom')" id="department-name-error" class="organisation-form__error">
          {{ messageFor('nom') }}
        </p>
      </div>

      <div class="organisation-form__field">
        <label for="department-parent">Département parent</label>
        <select
          id="department-parent"
          v-model="form.departementParentId"
          class="organisation-form__control"
          name="departementParentId"
          :aria-invalid="Boolean(messageFor('departementParentId'))"
          :aria-describedby="
            messageFor('departementParentId') ? 'department-parent-error' : undefined
          "
        >
          <option value="">Aucun (département racine)</option>
          <option v-for="parent in parentOptions" :key="parent.id" :value="String(parent.id)">
            {{ parent.code }} — {{ parent.nom }}
          </option>
        </select>
        <p
          v-if="messageFor('departementParentId')"
          id="department-parent-error"
          class="organisation-form__error"
        >
          {{ messageFor('departementParentId') }}
        </p>
        <p v-else class="organisation-form__hint">Seuls les départements actifs sont proposés.</p>
      </div>

      <div class="organisation-form__field">
        <label for="department-owner">Identifiant du responsable (facultatif)</label>
        <input
          id="department-owner"
          v-model="form.responsableId"
          class="organisation-form__control"
          name="responsableId"
          type="number"
          min="1"
          step="1"
          :aria-invalid="Boolean(messageFor('responsableId'))"
          :aria-describedby="
            messageFor('responsableId') ? 'department-owner-error' : 'department-owner-hint'
          "
        />
        <p
          v-if="messageFor('responsableId')"
          id="department-owner-error"
          class="organisation-form__error"
        >
          {{ messageFor('responsableId') }}
        </p>
        <p v-else id="department-owner-hint" class="organisation-form__hint">
          Saisissez l’identifiant de l’employé responsable.
        </p>
      </div>
    </div>
  </OrganisationFormPanel>
</template>
