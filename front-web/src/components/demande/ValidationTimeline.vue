<script setup>
import { computed } from 'vue'
import { DECISION_VALIDATION_LABELS } from '@/constants/statuts'

const props = defineProps({
  validations: { type: Array, default: () => [] },
})

const cycles = computed(() => {
  const grouped = new Map()
  for (const validation of props.validations) {
    if (!grouped.has(validation.cycle)) grouped.set(validation.cycle, [])
    grouped.get(validation.cycle).push(validation)
  }
  return [...grouped.entries()]
    .sort(([left], [right]) => left - right)
    .map(([cycle, items]) => ({
      cycle,
      items: [...items].sort((left, right) => left.ordre - right.ordre),
    }))
})

function formatDate(value) {
  if (!value) return ''
  return new Intl.DateTimeFormat('fr-FR', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value))
}
</script>

<template>
  <section class="validation-timeline" aria-label="Historique des validations">
    <p v-if="!cycles.length" class="validation-timeline__empty">
      Aucune validation n’a encore été créée.
    </p>
    <div v-for="cycle in cycles" :key="cycle.cycle" class="validation-timeline__cycle">
      <h3>Cycle {{ cycle.cycle }}</h3>
      <ol>
        <li v-for="item in cycle.items" :key="item.id" class="validation-timeline__item">
          <span class="validation-timeline__marker" aria-hidden="true" />
          <div class="validation-timeline__content">
            <div class="validation-timeline__heading">
              <strong>{{ item.roleValidateur }}</strong>
              <span>{{ DECISION_VALIDATION_LABELS[item.decision] ?? item.decision }}</span>
            </div>
            <p v-if="item.validateurNom" class="validation-timeline__meta">
              Décision de {{ item.validateurNom }}
            </p>
            <p v-if="item.dateDecision" class="validation-timeline__meta">
              {{ formatDate(item.dateDecision) }}
            </p>
            <p v-if="item.commentaire" class="validation-timeline__comment">
              {{ item.commentaire }}
            </p>
          </div>
        </li>
      </ol>
    </div>
  </section>
</template>

<style scoped>
.validation-timeline {
  display: grid;
  gap: var(--space-5);
}

.validation-timeline__empty {
  margin: 0;
  color: var(--color-text-muted);
}

.validation-timeline__cycle h3 {
  margin: 0 0 var(--space-3);
  color: var(--color-text);
  font-size: var(--font-size-base);
}

.validation-timeline__cycle ol {
  display: grid;
  gap: var(--space-3);
  margin: 0;
  padding: 0;
  list-style: none;
}

.validation-timeline__item {
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
}

.validation-timeline__marker {
  width: 12px;
  height: 12px;
  flex: 0 0 12px;
  margin-top: 5px;
  border: 2px solid var(--color-info-border);
  border-radius: 50%;
  background: var(--color-info-soft);
}

.validation-timeline__content {
  min-width: 0;
  flex: 1;
  padding-bottom: var(--space-3);
  border-bottom: 1px solid var(--color-border);
}

.validation-timeline__heading {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: var(--space-2);
  color: var(--color-text);
  font-size: var(--font-size-sm);
}

.validation-timeline__heading span {
  color: var(--color-primary);
  font-weight: var(--font-weight-semibold);
}

.validation-timeline__meta,
.validation-timeline__comment {
  margin: var(--space-1) 0 0;
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}

.validation-timeline__comment {
  padding: var(--space-2) var(--space-3);
  border-left: 3px solid var(--color-border-strong);
  background: var(--color-bg);
  color: var(--color-text);
}
</style>
