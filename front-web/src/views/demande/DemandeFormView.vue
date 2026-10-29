<script setup>
import DemandeForm from '@/components/demande/DemandeForm.vue'

defineProps({ id: { type: String, default: null } })
</script>

<template>
  <DemandeForm :id="id" />
</template>
