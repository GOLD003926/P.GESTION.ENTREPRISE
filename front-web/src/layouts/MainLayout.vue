<script setup>
import { ref, watch } from 'vue'
import { RouterView, useRoute } from 'vue-router'
import AppBreadcrumb from '@/components/layout/AppBreadcrumb.vue'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppSidebar from '@/components/layout/AppSidebar.vue'

const route = useRoute()
const sidebarOpen = ref(false)

watch(
  () => route.fullPath,
  () => {
    sidebarOpen.value = false
  },
)
</script>

<template>
  <div class="app-shell">
    <a class="app-shell__skip-link" href="#main-content">Aller au contenu principal</a>
    <AppSidebar :open="sidebarOpen" @navigate="sidebarOpen = false" />

    <button
      v-if="sidebarOpen"
      class="app-shell__scrim"
      type="button"
      aria-label="Fermer le menu"
      @click="sidebarOpen = false"
    />

    <div class="app-shell__main">
      <AppHeader :navigation-open="sidebarOpen" @toggle-navigation="sidebarOpen = !sidebarOpen" />
      <main id="main-content" class="app-shell__content">
        <AppBreadcrumb />
        <RouterView />
      </main>
    </div>
  </div>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
  display: flex;
  background: var(--color-bg);
}

.app-shell__main {
  min-width: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.app-shell__skip-link {
  position: fixed;
  z-index: 40;
  top: var(--space-2);
  left: var(--space-2);
  transform: translateY(-150%);
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-primary);
  font-weight: var(--font-weight-semibold);
}

.app-shell__skip-link:focus {
  transform: translateY(0);
}

.app-shell__content {
  width: 100%;
  max-width: 1600px;
  margin: 0 auto;
  padding: var(--space-6);
  flex: 1;
}

.app-shell__scrim {
  display: none;
}

@media (max-width: 900px) {
  .app-shell__content {
    padding: var(--space-4);
  }

  .app-shell__scrim {
    display: block;
    position: fixed;
    z-index: 19;
    inset: 0;
    border: 0;
    background: var(--color-overlay);
  }
}
</style>
