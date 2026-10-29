<script setup>
defineProps({
  title: { type: String, required: true },
  description: { type: String, required: true },
  loading: { type: Boolean, default: false },
  saving: { type: Boolean, default: false },
  loadFailed: { type: Boolean, default: false },
  error: { type: String, default: '' },
  fieldErrors: { type: Object, default: () => ({}) },
  cancelRoute: { type: Object, required: true },
})

const emit = defineEmits(['submit'])
</script>

<template>
  <section class="form-page">
    <header class="form-page__header">
      <p class="form-page__eyebrow">Référentiel entreprise</p>
      <h1>{{ title }}</h1>
      <p>{{ description }}</p>
    </header>

    <div class="form-card">
      <p v-if="loading" class="form-state" role="status">Chargement des données…</p>
      <template v-else>
        <div v-if="error" class="form-error" role="alert">
          <p>{{ error }}</p>
        </div>
        <form v-if="!loadFailed" class="form-content" @submit.prevent="emit('submit')">
          <slot />
          <div class="form-actions">
            <RouterLink class="form-actions__cancel" :to="cancelRoute">Annuler</RouterLink>
            <button class="form-actions__submit" type="submit" :disabled="saving">
              {{ saving ? 'Enregistrement…' : 'Enregistrer' }}
            </button>
          </div>
        </form>
        <div v-else class="form-actions form-actions--only">
          <RouterLink class="form-actions__cancel" :to="cancelRoute">Retour à la liste</RouterLink>
        </div>
      </template>
    </div>
  </section>
</template>

<style scoped>
.form-page {
  max-width: 900px;
  display: grid;
  gap: var(--space-5);
  margin: 0 auto;
}

.form-page__header {
  display: grid;
  gap: var(--space-2);
}

.form-page__eyebrow {
  margin: 0;
  color: var(--color-primary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
}

.form-page__header h1 {
  margin: 0;
  color: var(--color-text);
  font-size: var(--font-size-2xl);
}

.form-page__header p:last-child {
  margin: 0;
  color: var(--color-text-muted);
}

.form-card {
  overflow: hidden;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  box-shadow: var(--shadow-card);
}

.form-content {
  display: grid;
  gap: var(--space-5);
  padding: var(--space-6);
}

.form-state {
  margin: 0;
  padding: var(--space-8);
  color: var(--color-text-muted);
  text-align: center;
}

.form-error {
  margin: var(--space-5) var(--space-6) 0;
  padding: var(--space-3) var(--space-4);
  border: 1px solid var(--color-danger-border);
  border-radius: var(--radius-sm);
  background: var(--color-danger-soft);
  color: var(--color-danger);
}

.form-error p {
  margin: 0;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  padding-top: var(--space-4);
  border-top: 1px solid var(--color-border);
}

.form-actions--only {
  padding: 0 var(--space-6) var(--space-5);
  border: 0;
}

.form-actions__cancel,
.form-actions__submit {
  min-height: var(--control-height);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0 var(--space-4);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text);
  font: inherit;
  font-weight: var(--font-weight-semibold);
  text-decoration: none;
  cursor: pointer;
}

.form-actions__submit {
  border-color: var(--color-primary);
  background: var(--color-primary);
  color: var(--color-on-primary);
}

.form-actions__submit:hover:not(:disabled) {
  background: var(--color-primary-hover);
}

.form-actions__submit:disabled {
  cursor: wait;
  opacity: 0.65;
}

.form-actions__cancel:focus-visible,
.form-actions__submit:focus-visible {
  outline: 3px solid var(--color-focus-ring);
  outline-offset: 2px;
}
</style>
