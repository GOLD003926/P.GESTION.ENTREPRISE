<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const title = computed(() => route.meta.title ?? 'SIRH')
</script>

<template>
  <div class="breadcrumb" aria-label="Fil d’Ariane">
    <span>Accueil</span>
    <span class="breadcrumb__separator" aria-hidden="true">/</span>
    <span class="breadcrumb__current" aria-current="page">{{ title }}</span>
  </div>
</template>

<style scoped>
.breadcrumb {
  min-height: 28px;
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-4);
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}

.breadcrumb__separator {
  color: var(--color-border-strong);
}

.breadcrumb__current {
  color: var(--color-text);
  font-weight: var(--font-weight-medium);
}
</style>
