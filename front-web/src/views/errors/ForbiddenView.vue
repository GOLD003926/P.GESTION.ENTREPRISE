<script setup>
import { RouterLink } from 'vue-router'
import { ROUTE_NAMES } from '@/constants/routeNames'
</script>

<template>
  <section class="error-page" aria-labelledby="forbidden-title">
    <p class="error-page__code">403</p>
    <h2 id="forbidden-title">Accès refusé</h2>
    <p>Votre rôle ne vous permet pas d’accéder à cette page.</p>
    <RouterLink :to="{ name: ROUTE_NAMES.DASHBOARD }" class="error-page__link">
      Retour au tableau de bord
    </RouterLink>
  </section>
</template>

<style scoped>
.error-page {
  max-width: 520px;
  margin: var(--space-10) auto;
  padding: var(--space-8);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  box-shadow: var(--shadow-card);
  text-align: center;
}

.error-page__code {
  margin: 0;
  color: var(--color-primary);
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
}

.error-page h2 {
  margin: var(--space-2) 0;
  color: var(--color-text);
  font-size: var(--font-size-xl);
}

.error-page > p:not(.error-page__code) {
  color: var(--color-text-muted);
}

.error-page__link {
  display: inline-flex;
  min-height: var(--control-height);
  align-items: center;
  margin-top: var(--space-4);
  padding: 0 var(--space-4);
  border-radius: var(--radius-sm);
  background: var(--color-primary);
  color: var(--color-on-primary);
  font-weight: var(--font-weight-semibold);
  text-decoration: none;
}

.error-page__link:hover {
  background: var(--color-primary-hover);
}
</style>
