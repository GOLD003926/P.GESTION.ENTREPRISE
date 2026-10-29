<script setup>
import { computed, ref } from 'vue'
import AppButton from '@/components/common/AppButton.vue'

const props = defineProps({
  proposition: { type: Object, default: null },
  loading: { type: Boolean, default: false },
})

const emit = defineEmits(['accept', 'refuse', 'delay'])
const motifRefus = ref('')

const canRespond = computed(() =>
  props.proposition && ['ENVOYEE', 'DELAI_ACCORDE'].includes(props.proposition.statut),
)

function onRefuse() {
  emit('refuse', { motifRefus: motifRefus.value.trim() })
}

function onDelay() {
  emit('delay')
}

function onAccept() {
  emit('accept')
}
</script>

<template>
  <section v-if="canRespond" class="panel">
    <header>
      <h3>Réponse du candidat</h3>
    </header>
    <div class="actions">
      <AppButton :loading="loading" @click="onAccept">Accepter</AppButton>
      <AppButton variant="secondary" :loading="loading" @click="onDelay">Demander un délai</AppButton>
    </div>
    <label class="motif">
      Motif du refus
      <textarea v-model="motifRefus" rows="3" placeholder="Indiquez le motif du refus" />
    </label>
    <AppButton variant="secondary" :loading="loading" @click="onRefuse">Refuser</AppButton>
  </section>
</template>

<style scoped>
.panel {
  display: grid;
  gap: var(--space-4);
  padding: var(--space-5);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  box-shadow: var(--shadow-card);
}

header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

h3 {
  margin: 0;
  color: var(--color-text);
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-3);
}

.motif {
  display: grid;
  gap: var(--space-2);
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
}

textarea {
  min-height: 80px;
  padding: var(--space-3);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text);
  font: inherit;
}
</style>
