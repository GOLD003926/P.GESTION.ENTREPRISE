<script setup>
import { computed } from 'vue'
import { ROLES, ROLE_LABELS } from '@/constants/roles'
import { useAuthStore } from '@/stores/auth'
import MesTachesOnboardingWidget from '@/components/dashboard/MesTachesOnboardingWidget.vue'
import PendingValidationsWidget from '@/components/dashboard/PendingValidationsWidget.vue'
import RecentActivityWidget from '@/components/dashboard/RecentActivityWidget.vue'

const auth = useAuthStore()
const roleLabel = computed(() => ROLE_LABELS[auth.role] ?? 'Collaborateur')
const roleSummary = computed(() => {
  switch (auth.role) {
    case ROLES.RH:
      return 'Suivez les demandes à valider et les tâches d’intégration à coordonner.'
    case ROLES.MANAGER:
      return 'Retrouvez les tâches d’intégration qui concernent votre équipe.'
    case ROLES.FINANCE:
      return 'Consultez les demandes de recrutement en attente de votre validation.'
    case ROLES.DG:
      return 'Suivez les validations en attente et l’activité récente du système.'
    case ROLES.IT_SERVICES:
      return 'Retrouvez les tâches d’intégration liées aux accès, au matériel et aux badges.'
    case ROLES.PAIE:
      return 'Suivez les tâches d’inscription en paie des nouveaux collaborateurs.'
    case ROLES.SALARIE:
      return 'Consultez l’avancement de votre intégration et les tâches qui vous concernent.'
    case ROLES.ADMIN:
      return 'Bienvenue dans votre espace d’administration du SIRH.'
    default:
      return 'Bienvenue dans votre espace de travail SIRH.'
  }
})

const showValidations = computed(() => [ROLES.RH, ROLES.FINANCE, ROLES.DG].includes(auth.role))
const showOnboardingTasks = computed(() =>
  [ROLES.RH, ROLES.MANAGER, ROLES.IT_SERVICES, ROLES.PAIE].includes(auth.role),
)
const showMyOnboarding = computed(() => auth.role === ROLES.SALARIE)
const showRecentActivity = computed(() => [ROLES.RH, ROLES.DG].includes(auth.role))
const widgetCount = computed(
  () =>
    Number(showValidations.value) +
    Number(showOnboardingTasks.value) +
    Number(showMyOnboarding.value) +
    Number(showRecentActivity.value),
)
</script>

<template>
  <section class="dashboard" aria-labelledby="dashboard-title">
    <header class="dashboard__welcome">
      <div>
        <p class="dashboard__eyebrow">{{ roleLabel }}</p>
        <h2 id="dashboard-title">Bonjour{{ auth.user?.prenom ? `, ${auth.user.prenom}` : '' }}</h2>
        <p class="dashboard__summary">{{ roleSummary }}</p>
      </div>
      <div class="dashboard__welcome-mark" aria-hidden="true">S</div>
    </header>

    <div
      v-if="widgetCount > 0"
      class="dashboard__widgets"
      :class="{ 'dashboard__widgets--single': widgetCount === 1 }"
    >
      <PendingValidationsWidget v-if="showValidations" />
      <MesTachesOnboardingWidget v-if="showOnboardingTasks" />
      <MesTachesOnboardingWidget v-if="showMyOnboarding" mode="employee" />
      <RecentActivityWidget v-if="showRecentActivity" />
    </div>

    <section v-else class="dashboard__empty" aria-labelledby="dashboard-empty-title">
      <div class="dashboard__empty-icon" aria-hidden="true">✓</div>
      <h3 id="dashboard-empty-title">Votre espace est prêt</h3>
      <p>
        Les outils correspondant à votre rôle apparaîtront ici à mesure que les modules du SIRH
        seront disponibles.
      </p>
    </section>
  </section>
</template>

<style scoped>
.dashboard {
  display: grid;
  gap: var(--space-6);
}

.dashboard__welcome {
  min-height: 156px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-6);
  padding: var(--space-6) var(--space-8);
  overflow: hidden;
  border: 1px solid var(--color-info-border);
  border-radius: var(--radius-lg);
  background:
    radial-gradient(circle at 92% 15%, rgb(15 91 142 / 0.1), transparent 35%), var(--color-surface);
  box-shadow: var(--shadow-card);
}

.dashboard__eyebrow {
  margin: 0 0 var(--space-2);
  color: var(--color-primary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
}

.dashboard__welcome h2 {
  margin: 0;
  color: var(--color-text);
  font-size: clamp(var(--font-size-xl), 3vw, var(--font-size-2xl));
  line-height: 1.2;
}

.dashboard__summary {
  max-width: 620px;
  margin: var(--space-3) 0 0;
  color: var(--color-text-muted);
}

.dashboard__welcome-mark {
  width: 76px;
  height: 76px;
  display: grid;
  flex: 0 0 76px;
  place-items: center;
  border: 1px solid var(--color-info-border);
  border-radius: 22px;
  background: var(--color-info-soft);
  color: var(--color-primary);
  font-size: 2.5rem;
  font-weight: var(--font-weight-bold);
  transform: rotate(7deg);
}

.dashboard__widgets {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  align-items: start;
  gap: var(--space-4);
}

.dashboard__empty {
  max-width: 680px;
  display: grid;
  justify-items: center;
  margin: 0 auto;
  padding: var(--space-8);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  text-align: center;
}

.dashboard__empty-icon {
  width: 44px;
  height: 44px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: var(--color-success-soft);
  color: var(--color-success);
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-bold);
}

.dashboard__empty h3 {
  margin: var(--space-3) 0 0;
  color: var(--color-text);
}

.dashboard__empty p {
  max-width: 480px;
  margin: var(--space-2) 0 0;
  color: var(--color-text-muted);
}

.dashboard__widgets--single {
  grid-template-columns: minmax(0, 1fr);
}

@media (max-width: 680px) {
  .dashboard {
    gap: var(--space-4);
  }

  .dashboard__welcome {
    min-height: 0;
    padding: var(--space-5);
  }

  .dashboard__welcome-mark {
    width: 52px;
    height: 52px;
    flex-basis: 52px;
    border-radius: var(--radius-md);
    font-size: var(--font-size-xl);
  }

  .dashboard__widgets {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
