<script setup>
import CandidatForm from '@/components/recrutement/CandidatForm.vue'
import { useRoute } from 'vue-router'
const route = useRoute()
</script>

<template><CandidatForm :id="route.params.id" /></template>
