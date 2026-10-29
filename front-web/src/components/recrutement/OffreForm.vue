<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { offresApi } from '@/api/offres'
import { demandesApi } from '@/api/demandes'
import { ROUTE_NAMES } from '@/constants/routeNames'
import AppAlert from '@/components/common/AppAlert.vue'
import AppButton from '@/components/common/AppButton.vue'

const props = defineProps({ id: { type: String, default: null } })
const router = useRouter()
const isEdit = Boolean(props.id)
const form = reactive({
  demandeId: '',
  titre: '',
  description: '',
  profilRecherche: '',
  nombrePostes: 1,
  nombreNiveauxSelection: 3,
  canalPublication: '',
  dateCloture: '',
})
const demandes = ref([])
const loading = ref(true)
const saving = ref(false)
const error = ref('')
const fields = ref({})

function validate() {
  const next = {}
  if (!isEdit && !form.demandeId) next.demandeId = 'Sélectionnez une demande validée.'
  if (!form.titre.trim()) next.titre = 'Le titre est obligatoire.'
  if (!form.description.trim()) next.description = 'La description est obligatoire.'
  if (!Number.isInteger(Number(form.nombrePostes)) || Number(form.nombrePostes) < 1) {
    next.nombrePostes = 'Le nombre de postes doit être au moins égal à 1.'
  }
  if (![1, 2, 3].includes(Number(form.nombreNiveauxSelection))) {
    next.nombreNiveauxSelection = 'Choisissez de 1 à 3 niveaux.'
  }
  fields.value = next
  return Object.keys(next).length === 0
}

async function load() {
  try {
    const [page, offer] = await Promise.all([
      isEdit ? Promise.resolve({ content: [] }) : demandesApi.rechercher({ page: 0, size: 100 }),
      isEdit ? offresApi.obtenir(props.id) : Promise.resolve(null),
    ])
    demandes.value = (page.content ?? []).filter((item) =>
      ['VALIDEE', 'EN_RECRUTEMENT'].includes(item.statut),
    )
    if (offer)
      Object.assign(form, {
        demandeId: String(offer.demandeId),
        titre: offer.titre,
        description: offer.description,
        profilRecherche: offer.profilRecherche ?? '',
        nombrePostes: offer.nombrePostes,
        nombreNiveauxSelection: offer.nombreNiveauxSelection,
        canalPublication: offer.canalPublication ?? '',
        dateCloture: offer.dateCloture ?? '',
      })
  } catch (cause) {
    error.value = cause.message
  } finally {
    loading.value = false
  }
}

async function submit() {
  error.value = ''
  if (!validate()) return
  saving.value = true
  const payload = {
    titre: form.titre.trim(),
    description: form.description.trim(),
    profilRecherche: form.profilRecherche.trim() || null,
    nombrePostes: Number(form.nombrePostes),
    nombreNiveauxSelection: Number(form.nombreNiveauxSelection),
    canalPublication: form.canalPublication.trim() || null,
    dateCloture: form.dateCloture || null,
  }
  if (!isEdit) payload.demandeId = Number(form.demandeId)
  try {
    const result = isEdit
      ? await offresApi.modifier(props.id, payload)
      : await offresApi.creer(payload)
    await router.push({ name: ROUTE_NAMES.OFFRE_DETAIL, params: { id: result.id } })
  } catch (cause) {
    error.value = cause.message
    fields.value = cause.fieldErrors ?? {}
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<template>
  <section class="recruitment-form-page">
    <header>
      <p class="recruitment-eyebrow">Recrutement</p>
      <h1>{{ isEdit ? 'Modifier une offre' : 'Créer une offre' }}</h1>
    </header>
    <AppAlert v-if="error" variant="error">{{ error }}</AppAlert>
    <p v-if="loading" role="status">Chargement du formulaire…</p>
    <form v-else class="recruitment-card recruitment-form" @submit.prevent="submit">
      <label v-if="!isEdit"
        >Demande validée
        <select v-model="form.demandeId" required>
          <option value="" disabled>Sélectionner une demande</option>
          <option v-for="item in demandes" :key="item.id" :value="String(item.id)">
            {{ item.reference }} — {{ item.posteIntitule }} ({{ item.statut }})
          </option></select
        ><small v-if="fields.demandeId">{{ fields.demandeId }}</small>
      </label>
      <label
        >Titre<input v-model.trim="form.titre" maxlength="150" required /><small
          v-if="fields.titre"
          >{{ fields.titre }}</small
        ></label
      >
      <label
        >Description<textarea v-model.trim="form.description" rows="5" required /><small
          v-if="fields.description"
          >{{ fields.description }}</small
        ></label
      >
      <label>Profil recherché<textarea v-model.trim="form.profilRecherche" rows="4" /></label>
      <label
        >Nombre de postes<input
          v-model.number="form.nombrePostes"
          type="number"
          min="1"
          required
        /><small v-if="fields.nombrePostes">{{ fields.nombrePostes }}</small></label
      >
      <label
        >Niveaux de sélection<input
          v-model.number="form.nombreNiveauxSelection"
          type="number"
          min="1"
          max="3"
          required
        /><small v-if="fields.nombreNiveauxSelection">{{
          fields.nombreNiveauxSelection
        }}</small></label
      >
      <label
        >Canal de publication<input v-model.trim="form.canalPublication" maxlength="100"
      /></label>
      <label
        >Date limite de candidature<input
          v-model="form.dateCloture"
          type="date"
          :min="new Date().toISOString().slice(0, 10)"
      /></label>
      <footer>
        <RouterLink :to="{ name: ROUTE_NAMES.OFFRES }">Retour</RouterLink
        ><AppButton type="submit" :loading="saving">{{
          isEdit ? 'Enregistrer' : 'Créer le brouillon'
        }}</AppButton>
      </footer>
    </form>
  </section>
</template>

<style scoped>
.recruitment-form-page {
  display: grid;
  gap: var(--space-5);
}
.recruitment-form-page h1 {
  margin: 0;
  color: var(--color-text);
  font-size: var(--font-size-2xl);
}
.recruitment-eyebrow {
  margin: 0 0 var(--space-1);
  color: var(--color-primary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
}
.recruitment-card {
  padding: var(--space-5);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  box-shadow: var(--shadow-card);
}
.recruitment-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-4);
}
.recruitment-form label {
  display: grid;
  align-content: start;
  gap: var(--space-2);
  color: var(--color-text);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
}
.recruitment-form label:nth-child(2),
.recruitment-form label:nth-child(3),
.recruitment-form footer {
  grid-column: 1 / -1;
}
.recruitment-form input,
.recruitment-form select,
.recruitment-form textarea {
  min-height: var(--control-height);
  width: 100%;
  padding: var(--space-3);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text);
  font: inherit;
  font-weight: var(--font-weight-normal);
}
.recruitment-form small {
  color: var(--color-danger);
  font-weight: var(--font-weight-normal);
}
.recruitment-form footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: var(--space-4);
  padding-top: var(--space-4);
  border-top: 1px solid var(--color-border);
}
.recruitment-form a {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: var(--font-weight-semibold);
}
@media (max-width: 700px) {
  .recruitment-form {
    grid-template-columns: 1fr;
  }
  .recruitment-form label,
  .recruitment-form footer {
    grid-column: auto;
  }
  .recruitment-form footer {
    align-items: stretch;
    flex-direction: column-reverse;
  }
}
</style>
