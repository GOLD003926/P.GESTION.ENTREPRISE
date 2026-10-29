<script setup>
defineProps({ employe: { type: Object, required: true } })
</script>

<template>
  <article class="employee-card">
    <header>
      <div class="avatar" aria-hidden="true">
        {{ employe.prenom?.[0] ?? '' }}{{ employe.nom?.[0] ?? '' }}
      </div>
      <div class="identity">
        <h1>{{ employe.prenom }} {{ employe.nom }}</h1>
        <p>{{ employe.matricule }}</p>
      </div>
    </header>
    <dl>
      <div>
        <dt>Statut</dt>
        <dd>
          <slot name="status">{{ employe.statut }}</slot>
        </dd>
      </div>
      <div>
        <dt>Date d’embauche</dt>
        <dd>{{ employe.dateEmbauche || '—' }}</dd>
      </div>
      <div>
        <dt>Poste actuel</dt>
        <dd>{{ employe.posteActuelIntitule || '—' }}</dd>
      </div>
      <div v-if="employe.departementActuelNom">
        <dt>Département</dt>
        <dd>{{ employe.departementActuelNom }}</dd>
      </div>
      <div v-if="employe.managerActuelNom">
        <dt>Manager</dt>
        <dd>{{ employe.managerActuelNom }}</dd>
      </div>
      <div v-if="employe.emailPro">
        <dt>E-mail professionnel</dt>
        <dd>{{ employe.emailPro }}</dd>
      </div>
      <div v-if="employe.emailPerso">
        <dt>E-mail personnel</dt>
        <dd>{{ employe.emailPerso }}</dd>
      </div>
      <div v-if="employe.telephone">
        <dt>Téléphone</dt>
        <dd>{{ employe.telephone }}</dd>
      </div>
      <div v-if="employe.dateNaissance">
        <dt>Date de naissance</dt>
        <dd>{{ employe.dateNaissance }}</dd>
      </div>
      <div v-if="employe.adresse">
        <dt>Adresse</dt>
        <dd>{{ employe.adresse }}</dd>
      </div>
      <div v-if="employe.numeroSecuriteSociale">
        <dt>Numéro de sécurité sociale</dt>
        <dd>{{ employe.numeroSecuriteSociale }}</dd>
      </div>
    </dl>
  </article>
</template>

<style scoped>
.employee-card {
  display: grid;
  gap: var(--space-5);
  padding: var(--space-5);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  box-shadow: var(--shadow-card);
}

.employee-card header {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.avatar {
  display: grid;
  width: 56px;
  height: 56px;
  flex: 0 0 auto;
  place-items: center;
  border-radius: 50%;
  background: var(--color-info-soft);
  color: var(--color-primary);
  font-weight: var(--font-weight-bold);
}

.identity h1 {
  margin: 0;
  color: var(--color-text);
  font-size: var(--font-size-2xl);
}

.identity p {
  margin: var(--space-1) 0 0;
  color: var(--color-text-muted);
}

dl {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-3);
  margin: 0;
}

dl div {
  min-width: 0;
  padding: var(--space-3);
  border-radius: var(--radius-sm);
  background: var(--color-bg);
}

dt {
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}

dd {
  margin: var(--space-1) 0 0;
  overflow-wrap: anywhere;
  color: var(--color-text);
}

@media (max-width: 650px) {
  dl {
    grid-template-columns: 1fr;
  }
}
</style>
