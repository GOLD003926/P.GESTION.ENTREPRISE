<script setup>
import { computed, onMounted, ref } from 'vue'
import { offresApi } from '@/api/offres'
import { ROUTE_NAMES } from '@/constants/routeNames'
import { STATUT_OFFRE_LABELS } from '@/constants/statuts'
import { useAuthStore } from '@/stores/auth'
import { ROLES } from '@/constants/roles'
import AppAlert from '@/components/common/AppAlert.vue'
import AppButton from '@/components/common/AppButton.vue'

const props = defineProps({ id: { type: String, required: true } })
const auth = useAuthStore()
const offre = ref(null)
const loading = ref(true)
const busy = ref(false)
const error = ref('')
const canManage = computed(() => auth.role === ROLES.RH)
const editable = computed(() => canManage.value && offre.value?.statut === 'BROUILLON')

async function load() {
  try {
    offre.value = await offresApi.obtenir(props.id)
  } catch (cause) {
    error.value = cause.message
  } finally {
    loading.value = false
  }
}
async function action(name) {
  busy.value = true
  error.value = ''
  try {
    offre.value = await offresApi[name](props.id)
  } catch (cause) {
    error.value = cause.message
  } finally {
    busy.value = false
  }
}
async function cancelOffer() {
  if (!window.confirm('Annuler cette offre ?')) return
  await action('annuler')
}
onMounted(load)
</script>

<template>
  <section class="recruitment-page">
    <p class="eyebrow">
      <RouterLink :to="{ name: ROUTE_NAMES.OFFRES }">Offres</RouterLink> / Détail
    </p>
    <p v-if="loading" role="status">Chargement de l’offre…</p>
    <AppAlert v-if="error" variant="error">{{ error }}</AppAlert>
    <article v-if="offre" class="panel">
      <header>
        <div>
          <p class="eyebrow">
            {{ offre.reference }} · {{ STATUT_OFFRE_LABELS[offre.statut] ?? offre.statut }}
          </p>
          <h1>{{ offre.titre }}</h1>
          <p>{{ offre.posteIntitule }} · {{ offre.demandeReference }}</p>
        </div>
        <nav>
          <RouterLink
            v-if="editable"
            class="action-link"
            :to="{ name: ROUTE_NAMES.OFFRE_EDIT, params: { id } }"
            >Modifier</RouterLink
          ><AppButton
            v-if="canManage && offre.statut === 'BROUILLON'"
            :disabled="busy || !offre.dateCloture"
            @click="action('publier')"
            >Publier</AppButton
          ><AppButton
            v-if="canManage && offre.statut === 'PUBLIEE'"
            variant="secondary"
            :disabled="busy"
            @click="action('cloturer')"
            >Clôturer</AppButton
          ><AppButton
            v-if="canManage && ['BROUILLON', 'PUBLIEE'].includes(offre.statut)"
            variant="secondary"
            :disabled="busy"
            @click="cancelOffer"
            >Annuler</AppButton
          >
        </nav>
      </header>
      <dl>
        <div>
          <dt>Description</dt>
          <dd>{{ offre.description }}</dd>
        </div>
        <div>
          <dt>Profil recherché</dt>
          <dd>{{ offre.profilRecherche || 'Non précisé' }}</dd>
        </div>
        <div>
          <dt>Nombre de postes</dt>
          <dd>{{ offre.nombrePostes }}</dd>
        </div>
        <div>
          <dt>Niveaux de sélection</dt>
          <dd>{{ offre.nombreNiveauxSelection }}</dd>
        </div>
        <div>
          <dt>Canal</dt>
          <dd>{{ offre.canalPublication || '—' }}</dd>
        </div>
        <div>
          <dt>Publication</dt>
          <dd>{{ offre.datePublication || '—' }}</dd>
        </div>
        <div>
          <dt>Date limite</dt>
          <dd>{{ offre.dateCloture || '—' }}</dd>
        </div>
        <div>
          <dt>Candidatures</dt>
          <dd>{{ offre.nombreCandidatures ?? 0 }}</dd>
        </div>
      </dl>
      <footer v-if="[ROLES.RH, ROLES.MANAGER].includes(auth.role)">
        <RouterLink :to="{ name: ROUTE_NAMES.CANDIDATURES, query: { offreId: offre.id } }"
          >Consulter les candidatures</RouterLink
        >
      </footer>
    </article>
  </section>
</template>

<style scoped>
.recruitment-page {
  display: grid;
  gap: var(--space-5);
}
.eyebrow {
  margin: 0;
  color: var(--color-primary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
}
.eyebrow a,
.action-link {
  color: var(--color-primary);
  text-decoration: none;
}
.panel {
  display: grid;
  gap: var(--space-5);
  padding: var(--space-5);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  box-shadow: var(--shadow-card);
}
.panel header {
  display: flex;
  justify-content: space-between;
  gap: var(--space-4);
}
.panel h1 {
  margin: var(--space-2) 0;
  color: var(--color-text);
  font-size: var(--font-size-2xl);
}
.panel header p:last-child,
dd {
  color: var(--color-text-muted);
}
.panel nav {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-2);
}
.panel dl {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-4);
  margin: 0;
}
.panel dl div {
  padding: var(--space-3);
  background: var(--color-bg);
  border-radius: var(--radius-sm);
}
.panel dt {
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}
.panel dd {
  margin: var(--space-1) 0 0;
  white-space: pre-wrap;
}
.panel footer {
  padding-top: var(--space-4);
  border-top: 1px solid var(--color-border);
}
.panel footer a {
  color: var(--color-primary);
  font-weight: var(--font-weight-semibold);
}
@media (max-width: 720px) {
  .panel header {
    flex-direction: column;
  }
  .panel dl {
    grid-template-columns: 1fr;
  }
}
</style>
