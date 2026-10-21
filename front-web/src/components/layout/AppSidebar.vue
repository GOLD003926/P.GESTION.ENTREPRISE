<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { NAVIGATION_ITEMS } from '@/constants/navigation'
import { ROUTE_NAMES } from '@/constants/routeNames'
import { useAuthStore } from '@/stores/auth'
import AppNavIcon from './AppNavIcon.vue'

defineProps({
  open: { type: Boolean, default: false },
})

const emit = defineEmits(['navigate'])
const auth = useAuthStore()
const route = useRoute()

const items = computed(() => NAVIGATION_ITEMS.filter((item) => item.roles.includes(auth.role)))

function isActive(item) {
  return item.routeName === route.name
}
</script>

<template>
  <aside class="sidebar" :class="{ 'sidebar--open': open }" aria-label="Navigation principale">
    <div class="sidebar__rail">
      <RouterLink
        :to="{ name: ROUTE_NAMES.DASHBOARD }"
        class="sidebar__brand"
        aria-label="SIRH, tableau de bord"
        @click="emit('navigate')"
      >
        <span>S</span>
      </RouterLink>

      <nav class="sidebar__rail-nav" aria-label="Raccourcis des modules">
        <template v-for="item in items" :key="item.id">
          <RouterLink
            v-if="item.enabled"
            :to="{ name: item.routeName }"
            class="sidebar__rail-link"
            :class="{ 'sidebar__rail-link--active': isActive(item) }"
            :aria-label="item.label"
            :aria-current="isActive(item) ? 'page' : undefined"
            @click="emit('navigate')"
          >
            <AppNavIcon :name="item.icon" />
          </RouterLink>
          <span
            v-else
            class="sidebar__rail-link sidebar__rail-link--disabled"
            :aria-label="`${item.label}, bientôt disponible`"
            :title="`${item.label} : bientôt disponible`"
          >
            <AppNavIcon :name="item.icon" />
          </span>
        </template>
      </nav>

      <span class="sidebar__rail-footer" aria-hidden="true">•••</span>
    </div>

    <div class="sidebar__panel">
      <div class="sidebar__panel-heading">
        <span class="sidebar__eyebrow">Espace de travail</span>
        <h2>Navigation</h2>
      </div>

      <nav class="sidebar__links" aria-label="Modules">
        <span class="sidebar__section-label">MENU PRINCIPAL</span>
        <template v-for="item in items" :key="item.id">
          <RouterLink
            v-if="item.enabled"
            :to="{ name: item.routeName }"
            class="sidebar__link"
            :class="{ 'sidebar__link--active': isActive(item) }"
            :aria-current="isActive(item) ? 'page' : undefined"
            @click="emit('navigate')"
          >
            <AppNavIcon :name="item.icon" :size="18" />
            <span>{{ item.label }}</span>
          </RouterLink>
          <div
            v-else
            class="sidebar__link sidebar__link--disabled"
            :aria-label="`${item.label}, bientôt disponible`"
            :title="`${item.label} : bientôt disponible`"
          >
            <AppNavIcon :name="item.icon" :size="18" />
            <span>{{ item.label }}</span>
            <span class="sidebar__soon">À venir</span>
          </div>
        </template>
      </nav>

      <div class="sidebar__footer">
        <span class="sidebar__footer-mark">S</span>
        <span>
          <strong>SIRH</strong>
          <small>Gestion des ressources humaines</small>
        </span>
      </div>
    </div>
  </aside>
</template>

<style scoped>
.sidebar {
  z-index: 20;
  width: 264px;
  min-height: 100vh;
  display: flex;
  flex: 0 0 264px;
  background: var(--color-surface);
  border-right: 1px solid var(--color-border);
}

.sidebar__rail {
  width: 52px;
  display: flex;
  flex-direction: column;
  align-items: center;
  background: var(--color-sidebar);
  color: var(--color-sidebar-text);
}

.sidebar__brand {
  width: 34px;
  height: 34px;
  margin: var(--space-4) 0 var(--space-6);
  display: grid;
  place-items: center;
  border: 1px solid var(--color-sidebar-muted);
  border-radius: var(--radius-sm);
  color: var(--color-sidebar-text);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  text-decoration: none;
}

.sidebar__rail-nav {
  width: 100%;
  display: grid;
  gap: var(--space-2);
  justify-items: center;
}

.sidebar__rail-link {
  width: 36px;
  height: 36px;
  display: grid;
  place-items: center;
  border-radius: var(--radius-sm);
  color: var(--color-sidebar-muted);
  text-decoration: none;
  transition:
    color var(--transition),
    background-color var(--transition);
}

.sidebar__rail-link:hover:not(.sidebar__rail-link--disabled),
.sidebar__rail-link--active {
  background: var(--color-sidebar-active);
  color: var(--color-sidebar);
}

.sidebar__rail-link--disabled {
  opacity: 0.55;
}

.sidebar__rail-footer {
  margin-top: auto;
  padding: var(--space-5) 0;
  color: var(--color-sidebar-muted);
  font-size: var(--font-size-sm);
  letter-spacing: 0.08em;
}

.sidebar__panel {
  min-width: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: var(--space-5) var(--space-3) var(--space-4);
}

.sidebar__panel-heading {
  padding: 0 var(--space-2) var(--space-6);
}

.sidebar__eyebrow,
.sidebar__section-label {
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}

.sidebar__panel-heading h2 {
  margin: var(--space-1) 0 0;
  color: var(--color-text);
  font-size: var(--font-size-lg);
}

.sidebar__links {
  display: grid;
  align-content: start;
  gap: var(--space-1);
}

.sidebar__section-label {
  padding: 0 var(--space-2) var(--space-2);
  font-size: 0.7rem;
  font-weight: var(--font-weight-semibold);
  letter-spacing: 0.06em;
}

.sidebar__link {
  min-height: 40px;
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: 0 var(--space-2);
  border-radius: var(--radius-sm);
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
  text-decoration: none;
  transition:
    color var(--transition),
    background-color var(--transition);
}

.sidebar__link:hover:not(.sidebar__link--disabled),
.sidebar__link--active {
  background: var(--color-info-soft);
  color: var(--color-primary);
}

.sidebar__link--active {
  font-weight: var(--font-weight-semibold);
}

.sidebar__link--disabled {
  cursor: not-allowed;
  opacity: 0.62;
}

.sidebar__soon {
  margin-left: auto;
  color: var(--color-text-muted);
  font-size: 0.65rem;
  white-space: nowrap;
}

.sidebar__footer {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-top: auto;
  padding: var(--space-3) var(--space-1) 0;
  border-top: 1px solid var(--color-border);
  color: var(--color-text);
  font-size: var(--font-size-sm);
}

.sidebar__footer-mark {
  width: 30px;
  height: 30px;
  display: grid;
  place-items: center;
  border-radius: var(--radius-sm);
  background: var(--color-info-soft);
  color: var(--color-primary);
  font-weight: var(--font-weight-bold);
}

.sidebar__footer strong,
.sidebar__footer small {
  display: block;
}

.sidebar__footer small {
  margin-top: var(--space-1);
  color: var(--color-text-muted);
  font-size: 0.65rem;
}

@media (max-width: 900px) {
  .sidebar {
    position: fixed;
    inset: 0 auto 0 0;
    min-height: 100dvh;
    transform: translateX(-100%);
    transition: transform var(--transition);
  }

  .sidebar--open {
    transform: translateX(0);
  }
}
</style>
