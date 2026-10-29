<script setup>
import { onMounted, ref } from 'vue'
import { candidatsApi } from '@/api/candidats'
import { candidaturesApi } from '@/api/candidatures'
import { ROUTE_NAMES } from '@/constants/routeNames'
import AppAlert from '@/components/common/AppAlert.vue'
import CandidatureTable from '@/components/recrutement/CandidatureTable.vue'

const props = defineProps({ id: { type: String, required: true } })
const candidat = ref(null)
const candidatures = ref([])
const error = ref('')
const loading = ref(true)
onMounted(async () => {
  try {
    const [candidate, applications] = await Promise.all([
      candidatsApi.obtenir(props.id),
      candidaturesApi.rechercher({ candidatId: props.id, page: 0, size: 100 }),
    ])
    candidat.value = candidate
    candidatures.value = applications.content ?? []
  } catch (cause) {
    error.value = cause.message
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <section class="page">
    <p class="eyebrow">
      <RouterLink :to="{ name: ROUTE_NAMES.CANDIDATS }">Candidats</RouterLink> / Détail
    </p>
    <p v-if="loading" role="status">Chargement du candidat…</p>
    <AppAlert v-if="error" variant="error">{{ error }}</AppAlert>
    <article v-if="candidat" class="panel">
      <header>
        <div>
          <p class="eyebrow">Profil candidat</p>
          <h1>{{ candidat.prenom }} {{ candidat.nom }}</h1>
          <p>{{ candidat.email }}</p>
        </div>
        <RouterLink class="action" :to="{ name: ROUTE_NAMES.CANDIDAT_EDIT, params: { id } }"
          >Modifier</RouterLink
        >
      </header>
      <dl>
        <div>
          <dt>Téléphone</dt>
          <dd>{{ candidat.telephone || '—' }}</dd>
        </div>
        <div>
          <dt>Date de naissance</dt>
          <dd>{{ candidat.dateNaissance || '—' }}</dd>
        </div>
        <div>
          <dt>Nationalité</dt>
          <dd>{{ candidat.nationalite || '—' }}</dd>
        </div>
        <div>
          <dt>Adresse</dt>
          <dd>{{ candidat.adresse || '—' }}</dd>
        </div>
        <div>
          <dt>Consentement aux données</dt>
          <dd>{{ candidat.consentementDonnees ? 'Accordé' : 'Non accordé' }}</dd>
        </div>
      </dl>
    </article>
    <section v-if="candidat" class="panel">
      <h2>Candidatures associées</h2>
      <p v-if="!candidatures.length" class="muted">
        Aucune candidature enregistrée pour ce profil.
      </p>
      <CandidatureTable v-else :candidatures="candidatures" />
    </section>
  </section>
</template>

<style scoped>
.page {
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
.action {
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
.panel h2 {
  margin: 0;
  color: var(--color-text);
}
.panel header p:last-child,
dd,
.muted {
  color: var(--color-text-muted);
}
.panel dl {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-3);
  margin: 0;
}
.panel dl div {
  padding: var(--space-3);
  border-radius: var(--radius-sm);
  background: var(--color-bg);
}
.panel dt {
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}
.panel dd {
  margin: var(--space-1) 0 0;
  white-space: pre-wrap;
}
@media (max-width: 650px) {
  .panel header {
    flex-direction: column;
  }
  .panel dl {
    grid-template-columns: 1fr;
  }
}
</style>
