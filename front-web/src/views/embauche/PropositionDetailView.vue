<script setup>
import { computed, onMounted, ref } from 'vue'
import { propositionsApi } from '@/api/propositions'
import AppAlert from '@/components/common/AppAlert.vue'
import AppButton from '@/components/common/AppButton.vue'
import DelaiReponseCountdown from '@/components/embauche/DelaiReponseCountdown.vue'
import PropositionReponsePanel from '@/components/embauche/PropositionReponsePanel.vue'
import { ROUTE_NAMES } from '@/constants/routeNames'
import {
  STATUT_PROPOSITION_LABELS,
  TYPE_CONTRAT_LABELS,
} from '@/constants/statuts'

const props = defineProps({ id: { type: String, required: true } })
const loading = ref(true)
const saving = ref(false)
const error = ref('')
const proposition = ref(null)
const historique = ref([])

const canEdit = computed(() => proposition.value?.statut === 'BROUILLON')

async function load() {
  loading.value = true
  error.value = ''
  try {
    const [detail, history] = await Promise.all([
      propositionsApi.obtenir(props.id),
      propositionsApi.historique(props.id),
    ])
    proposition.value = detail
    historique.value = history ?? []
  } catch (cause) {
    error.value = cause.message
  } finally {
    loading.value = false
  }
}

async function respond(actionName, payload = {}) {
  saving.value = true
  error.value = ''
  try {
    await propositionsApi.repondre(props.id, { reponse: actionName, ...payload })
    await load()
  } catch (cause) {
    error.value = cause.message
  } finally {
    saving.value = false
  }
}

async function send() {
  saving.value = true
  error.value = ''
  try {
    await propositionsApi.envoyer(props.id)
    await load()
  } catch (cause) {
    error.value = cause.message
  } finally {
    saving.value = false
  }
}

async function withdraw() {
  saving.value = true
  error.value = ''
  try {
    await propositionsApi.retirer(props.id)
    await load()
  } catch (cause) {
    error.value = cause.message
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<template>
  <section class="page">
    <p class="eyebrow">
      <RouterLink :to="{ name: ROUTE_NAMES.PROPOSITIONS }">Propositions</RouterLink>
      / Détail
    </p>
    <p v-if="loading" class="state" role="status">Chargement de la proposition…</p>
    <AppAlert v-if="error" variant="error">{{ error }}</AppAlert>

    <template v-if="proposition">
      <article class="panel summary">
        <header>
          <div>
            <p class="eyebrow">#{{ proposition.id }}</p>
            <h1>{{ proposition.posteIntitule }}</h1>
            <p>{{ proposition.candidatNom }} · {{ proposition.candidatureId }}</p>
          </div>
          <div class="header-actions">
            <span class="status">{{ STATUT_PROPOSITION_LABELS[proposition.statut] ?? proposition.statut }}</span>
            <DelaiReponseCountdown :date-limite-reponse="proposition.dateLimiteReponse" />
          </div>
        </header>

        <dl>
          <div><dt>Type de contrat</dt><dd>{{ TYPE_CONTRAT_LABELS[proposition.typeContratPropose] ?? proposition.typeContratPropose }}</dd></div>
          <div><dt>Salaire</dt><dd>{{ proposition.salairePropose }} {{ proposition.devise }}</dd></div>
          <div><dt>Début prévu</dt><dd>{{ proposition.dateDebutPrevue }}</dd></div>
          <div><dt>Lieu</dt><dd>{{ proposition.lieuTravail }}</dd></div>
          <div><dt>Horaires</dt><dd>{{ proposition.horairesTravail }}</dd></div>
          <div><dt>Date limite</dt><dd>{{ proposition.dateLimiteReponse || '—' }}</dd></div>
        </dl>

        <div class="actions">
          <AppButton v-if="canEdit" variant="secondary" :loading="saving" @click="send">
            Envoyer au candidat
          </AppButton>
          <AppButton v-if="canEdit" variant="secondary" :loading="saving" @click="withdraw">
            Retirer
          </AppButton>
          <RouterLink
            v-if="canEdit"
            class="link"
            :to="{ name: ROUTE_NAMES.PROPOSITION_EDIT, params: { id: proposition.id } }"
          >
            Modifier
          </RouterLink>
        </div>
      </article>

      <PropositionReponsePanel
        :proposition="proposition"
        :loading="saving"
        @accept="respond('ACCEPTER')"
        @refuse="(payload) => respond('REFUSER', payload)"
        @delay="respond('DEMANDER_DELAI')"
      />

      <section class="panel">
        <header class="section-header">
          <h2>Historique</h2>
        </header>
        <ul v-if="historique.length" class="history">
          <li v-for="item in historique" :key="item.id || item.date + item.statut">
            <strong>{{ item.statut }}</strong>
            <span>{{ item.libelle || 'Mise à jour du statut' }}</span>
            <small>{{ item.date ? new Date(item.date).toLocaleString('fr-FR') : '—' }}</small>
          </li>
        </ul>
        <p v-else class="empty">Aucun historique enregistré.</p>
      </section>
    </template>
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

.state,
.empty {
  padding: var(--space-4);
  color: var(--color-text-muted);
}

.panel {
  display: grid;
  gap: var(--space-4);
  padding: var(--space-5);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  box-shadow: var(--shadow-card);
}

.summary header {
  display: flex;
  justify-content: space-between;
  gap: var(--space-4);
  align-items: center;
}

h1 {
  margin: 0;
  font-size: var(--font-size-2xl);
  color: var(--color-text);
}

.summary p {
  margin: var(--space-2) 0 0;
  color: var(--color-text-muted);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.status {
  display: inline-flex;
  align-items: center;
  padding: 0.25rem 0.625rem;
  border-radius: 999px;
  background: var(--color-info-soft);
  color: var(--color-primary);
  font-size: 0.72rem;
  font-weight: var(--font-weight-semibold);
}

dl {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--space-4);
  margin: 0;
}

dl div {
  display: grid;
  gap: var(--space-2);
}

dt {
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}

dd {
  margin: 0;
  color: var(--color-text);
  font-weight: var(--font-weight-semibold);
}

.actions {
  display: flex;
  gap: var(--space-3);
  flex-wrap: wrap;
  align-items: center;
}

.link {
  color: var(--color-primary);
  font-weight: var(--font-weight-semibold);
  text-decoration: none;
}

.section-header h2 {
  margin: 0;
}

.history {
  margin: 0;
  padding-left: 1.25rem;
  display: grid;
  gap: var(--space-3);
}

.history li {
  display: grid;
  gap: var(--space-1);
}

.history small {
  color: var(--color-text-muted);
}

@media (max-width: 720px) {
  dl {
    grid-template-columns: 1fr;
  }

  .summary header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
