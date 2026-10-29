<script setup>
import { computed } from 'vue'

const props = defineProps({
  demande: { type: Object, required: true },
  validations: { type: Array, default: () => [] },
})

const stages = [
  { role: 'RH', label: 'Ressources humaines', status: 'EN_VALIDATION_RH' },
  { role: 'FINANCE', label: 'Finance', status: 'EN_VALIDATION_FINANCE' },
  { role: 'DG', label: 'Direction générale', status: 'EN_VALIDATION_DG' },
]

const stageStates = computed(() => {
  const status = props.demande.statut
  const current = stages.findIndex((stage) => stage.status === status)
  const cycle = props.validations.filter((item) => item.cycle === props.demande.cycleCourant)
  const allApproved =
    ['VALIDEE', 'EN_RECRUTEMENT', 'POURVUE'].includes(status) ||
    (status === 'EN_VALIDATION_DG' &&
      cycle.some((item) => item.roleValidateur === 'RH' && item.decision === 'APPROUVEE') &&
      cycle.some((item) => item.roleValidateur === 'FINANCE' && item.decision === 'APPROUVEE'))

  return stages.map((stage, index) => {
    const validation = cycle.find((item) => item.roleValidateur === stage.role)
    let state = 'upcoming'
    if (validation?.decision === 'APPROUVEE' || index < current || allApproved) {
      state = 'complete'
    }
    if (index === current) state = 'current'
    if (validation?.decision === 'REJETEE') state = 'rejected'
    if (validation?.decision === 'RETOURNEE') state = 'returned'
    return { ...stage, state }
  })
})
</script>

<template>
  <section class="demande-stepper" aria-label="Étapes de validation">
    <ol>
      <li
        v-for="(stage, index) in stageStates"
        :key="stage.role"
        class="demande-stepper__step"
        :class="`demande-stepper__step--${stage.state}`"
      >
        <span class="demande-stepper__marker" aria-hidden="true">
          {{ stage.state === 'complete' ? '✓' : index + 1 }}
        </span>
        <span class="demande-stepper__text">
          <strong>{{ stage.label }}</strong>
          <small>
            {{
              {
                complete: 'Validée',
                current: 'En attente de décision',
                returned: 'Retour pour modification',
                rejected: 'Décision de rejet',
                upcoming: 'À venir',
              }[stage.state]
            }}
          </small>
        </span>
      </li>
    </ol>
  </section>
</template>

<style scoped>
.demande-stepper ol {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--space-3);
  margin: 0;
  padding: 0;
  list-style: none;
}

.demande-stepper__step {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.demande-stepper__marker {
  width: 36px;
  height: 36px;
  display: grid;
  flex: 0 0 36px;
  place-items: center;
  border: 1px solid var(--color-border);
  border-radius: 50%;
  background: var(--color-surface);
  color: var(--color-text-muted);
  font-weight: var(--font-weight-semibold);
}

.demande-stepper__text {
  min-width: 0;
  display: grid;
  gap: var(--space-1);
}

.demande-stepper__text strong {
  color: var(--color-text);
  font-size: var(--font-size-sm);
}

.demande-stepper__text small {
  color: var(--color-text-muted);
  font-size: 0.75rem;
}

.demande-stepper__step--complete .demande-stepper__marker {
  border-color: var(--color-success-border);
  background: var(--color-success-soft);
  color: var(--color-success);
}

.demande-stepper__step--current .demande-stepper__marker {
  border-color: var(--color-info-border);
  background: var(--color-info-soft);
  color: var(--color-info);
}

.demande-stepper__step--returned .demande-stepper__marker {
  border-color: var(--color-warning-border);
  background: var(--color-warning-soft);
  color: var(--color-warning);
}

.demande-stepper__step--rejected .demande-stepper__marker {
  border-color: var(--color-danger-border);
  background: var(--color-danger-soft);
  color: var(--color-danger);
}

@media (max-width: 720px) {
  .demande-stepper ol {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
