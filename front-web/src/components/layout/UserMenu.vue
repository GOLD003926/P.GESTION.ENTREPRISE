<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ROLE_LABELS } from '@/constants/roles'
import { ROUTE_NAMES } from '@/constants/routeNames'
import { useAuthStore } from '@/stores/auth'
import AppNavIcon from './AppNavIcon.vue'

const auth = useAuthStore()
const router = useRouter()
const initials = computed(() =>
  [auth.user?.prenom, auth.user?.nom]
    .filter(Boolean)
    .map((part) => part.trim().charAt(0).toUpperCase())
    .join('')
    .slice(0, 2),
)
const roleLabel = computed(() => ROLE_LABELS[auth.role] ?? 'Utilisateur')

function logout() {
  auth.logout()
  router.replace({ name: ROUTE_NAMES.LOGIN })
}
</script>

<template>
  <details class="user-menu">
    <summary class="user-menu__trigger" :aria-label="`Menu utilisateur, ${auth.fullName}`">
      <span class="user-menu__avatar" aria-hidden="true">{{ initials || 'U' }}</span>
      <span class="user-menu__identity">
        <strong>{{ auth.fullName || 'Utilisateur' }}</strong>
        <small>{{ roleLabel }}</small>
      </span>
      <span class="user-menu__chevron" aria-hidden="true">⌄</span>
    </summary>

    <div class="user-menu__panel">
      <div class="user-menu__profile">
        <span class="user-menu__avatar user-menu__avatar--large" aria-hidden="true">
          {{ initials || 'U' }}
        </span>
        <span>
          <strong>{{ auth.fullName || 'Utilisateur' }}</strong>
          <small>{{ auth.user?.email }}</small>
        </span>
      </div>
      <div class="user-menu__divider" />
      <button class="user-menu__logout" type="button" @click="logout">
        <AppNavIcon name="logout" :size="18" />
        <span>Se déconnecter</span>
      </button>
    </div>
  </details>
</template>

<style scoped>
.user-menu {
  position: relative;
}

.user-menu__trigger {
  min-height: 42px;
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-1) var(--space-2);
  border-radius: var(--radius-md);
  color: var(--color-text);
  cursor: pointer;
  list-style: none;
}

.user-menu__trigger::-webkit-details-marker {
  display: none;
}

.user-menu__trigger:hover,
.user-menu[open] .user-menu__trigger {
  background: var(--color-bg);
}

.user-menu__avatar {
  width: 34px;
  height: 34px;
  display: grid;
  flex: 0 0 34px;
  place-items: center;
  border-radius: 50%;
  background: var(--color-primary);
  color: var(--color-on-primary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
}

.user-menu__identity,
.user-menu__profile > span:last-child {
  min-width: 0;
  display: grid;
  gap: 2px;
  text-align: left;
}

.user-menu__identity strong {
  max-width: 150px;
  overflow: hidden;
  font-size: var(--font-size-sm);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-menu__identity small,
.user-menu__profile small {
  color: var(--color-text-muted);
  font-size: 0.75rem;
}

.user-menu__chevron {
  color: var(--color-text-muted);
}

.user-menu__panel {
  position: absolute;
  z-index: 30;
  top: calc(100% + var(--space-2));
  right: 0;
  width: min(280px, calc(100vw - var(--space-8)));
  padding: var(--space-3);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-surface);
  box-shadow: var(--shadow-card);
}

.user-menu__profile {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-2);
}

.user-menu__profile strong {
  color: var(--color-text);
  font-size: var(--font-size-sm);
}

.user-menu__profile small {
  overflow-wrap: anywhere;
}

.user-menu__avatar--large {
  width: 42px;
  height: 42px;
  flex-basis: 42px;
}

.user-menu__divider {
  height: 1px;
  margin: var(--space-2) 0;
  background: var(--color-border);
}

.user-menu__logout {
  width: 100%;
  min-height: 40px;
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: 0 var(--space-2);
  border: 0;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--color-danger);
  text-align: left;
}

.user-menu__logout:hover {
  background: var(--color-danger-soft);
}

.user-menu__logout:focus-visible,
.user-menu__trigger:focus-visible {
  outline: 3px solid var(--color-focus-ring);
  outline-offset: 2px;
}
</style>
