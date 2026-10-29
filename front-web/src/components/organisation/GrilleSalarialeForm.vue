<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { grillesSalarialesApi } from '@/api/grillesSalariales'
import OrganisationFormPanel from '@/components/organisation/OrganisationFormPanel.vue'
import { ROUTE_NAMES } from '@/constants/routeNames'

const props = defineProps({ id: { type: String, default: null } })
const router = useRouter()
const isEdit = computed(() => Boolean(props.id))
const form = reactive({
  code: '',
  intitule: '',
  niveau: '',
  salaireMin: '',
  salaireMax: '',
  devise: 'MGA',
})
const displayCode = ref('')
const loading = ref(true)
const saving = ref(false)
const loadFailed = ref(false)
const error = ref('')
const fieldErrors = ref({})

onMounted(async () => {
  if (!isEdit.value) {
    loading.value = false
    return
  }
  try {
    const grid = await grillesSalarialesApi.obtenir(props.id)
    displayCode.value = grid.code
    form.intitule = grid.intitule
    form.niveau = grid.niveau
    form.salaireMin = String(grid.salaireMin)
    form.salaireMax = String(grid.salaireMax)
    form.devise = grid.devise
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
  const minimum = Number(form.salaireMin)
  const maximum = Number(form.salaireMax)
  if (maximum < minimum) {
    fieldErrors.value = {
      salaireMax: 'Le salaire maximum doit être supérieur ou égal au salaire minimum.',
    }
    return
  }

  saving.value = true
  const data = {
    ...(isEdit.value ? {} : { code: form.code.trim().toUpperCase() }),
    intitule: form.intitule.trim(),
    niveau: form.niveau.trim(),
    salaireMin: minimum,
    salaireMax: maximum,
    devise: form.devise.trim().toUpperCase(),
  }
  try {
    if (isEdit.value) await grillesSalarialesApi.modifier(props.id, data)
    else await grillesSalarialesApi.creer(data)
    await router.push({ name: ROUTE_NAMES.GRILLES_SALARIALES })
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
    :title="isEdit ? 'Modifier la grille salariale' : 'Créer une grille salariale'"
    description="Définissez le niveau et la fourchette de rémunération. Le code reste immuable après création."
    :loading="loading"
    :saving="saving"
    :load-failed="loadFailed"
    :error="error"
    :field-errors="fieldErrors"
    :cancel-route="{ name: ROUTE_NAMES.GRILLES_SALARIALES }"
    @submit="submit"
  >
    <div class="organisation-form organisation-form__grid">
      <div class="organisation-form__field">
        <label for="grid-code">Code</label>
        <input
          v-if="!isEdit"
          id="grid-code"
          v-model="form.code"
          class="organisation-form__control"
          name="code"
          maxlength="30"
          pattern="[A-Za-z0-9_\x2d]+"
          autocomplete="off"
          required
          :aria-invalid="Boolean(messageFor('code'))"
          :aria-describedby="messageFor('code') ? 'grid-code-error' : 'grid-code-hint'"
          @input="form.code = $event.target.value.toUpperCase()"
        />
        <input
          v-else
          id="grid-code"
          class="organisation-form__control"
          :value="displayCode"
          readonly
          aria-describedby="grid-code-hint"
        />
        <p v-if="messageFor('code')" id="grid-code-error" class="organisation-form__error">
          {{ messageFor('code') }}
        </p>
        <p v-else id="grid-code-hint" class="organisation-form__hint">
          30 caractères maximum : lettres, chiffres, tiret ou underscore.
        </p>
      </div>

      <div class="organisation-form__field">
        <label for="grid-name">Intitulé</label>
        <input
          id="grid-name"
          v-model="form.intitule"
          class="organisation-form__control"
          name="intitule"
          maxlength="100"
          required
          :aria-invalid="Boolean(messageFor('intitule'))"
          :aria-describedby="messageFor('intitule') ? 'grid-name-error' : undefined"
        />
        <p v-if="messageFor('intitule')" id="grid-name-error" class="organisation-form__error">
          {{ messageFor('intitule') }}
        </p>
      </div>

      <div class="organisation-form__field">
        <label for="grid-level">Niveau</label>
        <input
          id="grid-level"
          v-model="form.niveau"
          class="organisation-form__control"
          name="niveau"
          maxlength="30"
          required
          :aria-invalid="Boolean(messageFor('niveau'))"
          :aria-describedby="messageFor('niveau') ? 'grid-level-error' : undefined"
        />
        <p v-if="messageFor('niveau')" id="grid-level-error" class="organisation-form__error">
          {{ messageFor('niveau') }}
        </p>
      </div>

      <div class="organisation-form__field">
        <label for="grid-currency">Devise</label>
        <input
          id="grid-currency"
          v-model="form.devise"
          class="organisation-form__control"
          name="devise"
          maxlength="3"
          pattern="[A-Za-z]{3}"
          required
          :aria-invalid="Boolean(messageFor('devise'))"
          :aria-describedby="messageFor('devise') ? 'grid-currency-error' : 'grid-currency-hint'"
          @input="form.devise = $event.target.value.toUpperCase()"
        />
        <p v-if="messageFor('devise')" id="grid-currency-error" class="organisation-form__error">
          {{ messageFor('devise') }}
        </p>
        <p v-else id="grid-currency-hint" class="organisation-form__hint">
          Code de devise ISO sur trois lettres. Par défaut : MGA.
        </p>
      </div>

      <div class="organisation-form__field">
        <label for="grid-minimum">Salaire minimum</label>
        <input
          id="grid-minimum"
          v-model="form.salaireMin"
          class="organisation-form__control"
          name="salaireMin"
          type="number"
          min="0.01"
          step="0.01"
          required
          :aria-invalid="Boolean(messageFor('salaireMin'))"
          :aria-describedby="messageFor('salaireMin') ? 'grid-minimum-error' : undefined"
        />
        <p v-if="messageFor('salaireMin')" id="grid-minimum-error" class="organisation-form__error">
          {{ messageFor('salaireMin') }}
        </p>
      </div>

      <div class="organisation-form__field">
        <label for="grid-maximum">Salaire maximum</label>
        <input
          id="grid-maximum"
          v-model="form.salaireMax"
          class="organisation-form__control"
          name="salaireMax"
          type="number"
          min="0.01"
          step="0.01"
          required
          :aria-invalid="Boolean(messageFor('salaireMax'))"
          :aria-describedby="messageFor('salaireMax') ? 'grid-maximum-error' : undefined"
        />
        <p v-if="messageFor('salaireMax')" id="grid-maximum-error" class="organisation-form__error">
          {{ messageFor('salaireMax') }}
        </p>
      </div>
    </div>
  </OrganisationFormPanel>
</template>
