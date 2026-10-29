<script setup>
import { useRoute } from 'vue-router'
import ContratForm from '@/components/contrat/ContratForm.vue'

const route = useRoute()
</script>

<template>
  <ContratForm :id="route.params.id" :employe-id="route.query.employeId" />
</template>