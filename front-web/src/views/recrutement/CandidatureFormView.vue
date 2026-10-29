<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { candidatsApi } from '@/api/candidats'
import { candidaturesApi } from '@/api/candidatures'
import { offresApi } from '@/api/offres'
import { ROUTE_NAMES } from '@/constants/routeNames'
import AppAlert from '@/components/common/AppAlert.vue'
import AppButton from '@/components/common/AppButton.vue'

const router = useRouter()
const offres = ref([])
const candidats = ref([])
const useExisting = ref(true)
const fichier = ref(null)
const createdId = ref(null)
const loading = ref(true)
const saving = ref(false)
const error = ref('')
const form = reactive({
  offreId: '',
  candidatId: '',
  lettreMotivation: '',
  candidat: { nom: '', prenom: '', email: '', telephone: '', consentementDonnees: false },
})

async function load() {
  try {
    const [offersPage, peoplePage] = await Promise.all([
      offresApi.rechercher({ statut: 'PUBLIEE', page: 0, size: 100 }),
      candidatsApi.rechercher({ page: 0, size: 100 }),
    ])
    offres.value = offersPage.content ?? []
    candidats.value = peoplePage.content ?? []
  } catch (cause) {
    error.value = cause.message
  } finally {
    loading.value = false
  }
}
function setFile(event) {
  fichier.value = event.target.files?.[0] ?? null
}
async function submit() {
  error.value = ''
  if (
    !form.offreId ||
    (useExisting.value && !form.candidatId) ||
    (!useExisting.value &&
      (!form.candidat.nom.trim() || !form.candidat.prenom.trim() || !form.candidat.email.trim()))
  ) {
    error.value = 'Complétez l’offre et les informations requises pour le candidat.'
    return
  }
  saving.value = true
  try {
    const payload = {
      offreId: Number(form.offreId),
      lettreMotivation: form.lettreMotivation.trim() || null,
      ...(useExisting.value
        ? { candidatId: Number(form.candidatId) }
        : { candidat: { ...form.candidat } }),
    }
    const result = createdId.value ? { id: createdId.value } : await candidaturesApi.creer(payload)
    createdId.value = result.id
    if (fichier.value) await candidaturesApi.televerserCv(result.id, fichier.value)
    await router.push({ name: ROUTE_NAMES.CANDIDATURE_DETAIL, params: { id: result.id } })
  } catch (cause) {
    error.value = createdId.value
      ? `La candidature est créée, mais le CV n’a pas pu être déposé : ${cause.message}. Réessayez.`
      : cause.message
  } finally {
    saving.value = false
  }
}
onMounted(load)
</script>

<template>
  <section class="page">
    <header>
      <p class="eyebrow">Recrutement</p>
      <h1>Enregistrer une candidature</h1>
    </header>
    <AppAlert v-if="error" variant="error">{{ error }}</AppAlert>
    <p v-if="loading" role="status">Chargement des offres et candidats…</p>
    <form v-else class="panel form" @submit.prevent="submit">
      <label
        >Offre publiée<select v-model="form.offreId" required>
          <option value="" disabled>Sélectionner une offre</option>
          <option v-for="offre in offres" :key="offre.id" :value="String(offre.id)">
            {{ offre.reference }} — {{ offre.titre }}
          </option>
        </select></label
      >
      <div class="candidate-choice">
        <strong>Candidat</strong
        ><label
          ><input v-model="useExisting" type="radio" :value="true" />Choisir un candidat
          existant</label
        ><label
          ><input v-model="useExisting" type="radio" :value="false" />Créer le candidat avec cette
          candidature</label
        >
      </div>
      <label v-if="useExisting"
        >Candidat<select v-model="form.candidatId" required>
          <option value="" disabled>Sélectionner un candidat</option>
          <option v-for="candidat in candidats" :key="candidat.id" :value="String(candidat.id)">
            {{ candidat.prenom }} {{ candidat.nom }} — {{ candidat.email }}
          </option>
        </select></label
      >
      <template v-else
        ><label>Nom<input v-model.trim="form.candidat.nom" maxlength="100" required /></label
        ><label>Prénom<input v-model.trim="form.candidat.prenom" maxlength="100" required /></label
        ><label>E-mail<input v-model.trim="form.candidat.email" type="email" required /></label
        ><label>Téléphone<input v-model.trim="form.candidat.telephone" maxlength="30" /></label
        ><label class="consent"
          ><input v-model="form.candidat.consentementDonnees" type="checkbox" />Consentement au
          traitement des données</label
        ></template
      >
      <label>Lettre de motivation<textarea v-model.trim="form.lettreMotivation" rows="5" /></label>
      <label
        >CV (PDF, DOC ou DOCX)<input
          type="file"
          accept=".pdf,.doc,.docx,application/pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document"
          @change="setFile"
      /></label>
      <footer>
        <RouterLink :to="{ name: ROUTE_NAMES.CANDIDATURES }">Retour</RouterLink
        ><AppButton type="submit" :loading="saving">{{
          createdId ? 'Réessayer le dépôt du CV' : 'Enregistrer la candidature'
        }}</AppButton>
      </footer>
    </form>
  </section>
</template>

<style scoped>
.page {
  display: grid;
  gap: var(--space-5);
}
.page h1 {
  margin: 0;
  color: var(--color-text);
  font-size: var(--font-size-2xl);
}
.eyebrow {
  margin: 0 0 var(--space-1);
  color: var(--color-primary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
}
.panel {
  padding: var(--space-5);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  box-shadow: var(--shadow-card);
}
.form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-4);
}
.form > label,
.candidate-choice {
  display: grid;
  align-content: start;
  gap: var(--space-2);
  color: var(--color-text);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
}
.form input:not([type='radio']):not([type='checkbox']),
.form select,
.form textarea {
  min-height: var(--control-height);
  padding: var(--space-3);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text);
  font: inherit;
  font-weight: var(--font-weight-normal);
}
.candidate-choice {
  padding: var(--space-3);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
}
.candidate-choice label,
.consent {
  display: flex !important;
  align-items: center;
  gap: var(--space-2);
  font-weight: var(--font-weight-normal) !important;
}
.form > label:nth-last-child(2),
.form > label:last-of-type,
.form footer {
  grid-column: 1 / -1;
}
.form footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: var(--space-4);
  padding-top: var(--space-4);
  border-top: 1px solid var(--color-border);
}
.form footer a {
  color: var(--color-primary);
  font-weight: var(--font-weight-semibold);
  text-decoration: none;
}
@media (max-width: 700px) {
  .form {
    grid-template-columns: 1fr;
  }
  .form > * {
    grid-column: 1 !important;
  }
  .form footer {
    align-items: stretch;
    flex-direction: column-reverse;
  }
}
</style>
