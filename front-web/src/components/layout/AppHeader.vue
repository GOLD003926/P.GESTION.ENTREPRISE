<script setup>
import { useRoute } from 'vue-router'
import AppNavIcon from './AppNavIcon.vue'
import UserMenu from './UserMenu.vue'

const emit = defineEmits(['toggle-navigation'])
defineProps({
  navigationOpen: { type: Boolean, default: false },
})
const route = useRoute()
</script>

<template>
  <header class="app-header">
    <div class="app-header__start">
      <button
        class="app-header__menu"
        type="button"
        aria-label="Ouvrir ou fermer la navigation"
        aria-controls="app-sidebar"
        :aria-expanded="navigationOpen"
        @click="emit('toggle-navigation')"
      >
        <AppNavIcon name="menu" />
      </button>
      <div class="app-header__title">
        <span class="app-header__brand">SIRH</span>
        <span class="app-header__divider" aria-hidden="true">/</span>
        <span class="app-header__page-title">{{ route.meta.title ?? 'Espace de travail' }}</span>
      </div>
    </div>

    <div class="app-header__end">
      <span class="app-header__environment">Espace collaborateurs</span>
      <UserMenu />
    </div>
  </header>
</template>

<style scoped>
.app-header {
  min-height: 68px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  padding: 0 var(--space-6);
  border-bottom: 1px solid var(--color-border);
  background: var(--color-surface);
}

.app-header__start,
.app-header__end,
.app-header__title {
  min-width: 0;
  display: flex;
  align-items: center;
}

.app-header__start {
  gap: var(--space-4);
}

.app-header__title {
  gap: var(--space-3);
}

.app-header__brand {
  color: var(--color-primary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-bold);
  letter-spacing: 0.06em;
}

.app-header__divider {
  color: var(--color-border-strong);
}

.app-header__page-title {
  overflow: hidden;
  margin: 0;
  color: var(--color-text);
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-semibold);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-header__end {
  gap: var(--space-4);
}

.app-header__environment {
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}

.app-header__menu {
  display: none;
  width: 40px;
  height: 40px;
  place-items: center;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-text);
}

.app-header__menu:focus-visible {
  outline: 3px solid var(--color-focus-ring);
  outline-offset: 2px;
}

@media (max-width: 900px) {
  .app-header {
    min-height: 60px;
    padding: 0 var(--space-4);
  }

  .app-header__menu {
    display: grid;
  }

  .app-header__start {
    gap: var(--space-3);
  }

  .app-header__environment,
  .app-header__divider {
    display: none;
  }
}

@media (max-width: 480px) {
  .app-header__page-title {
    max-width: 100px;
  }

  .app-header__end {
    gap: var(--space-1);
  }

  .user-menu__identity {
    display: none;
  }
}
</style>
