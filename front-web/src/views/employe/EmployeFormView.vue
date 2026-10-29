<script setup>
import { useRoute } from 'vue-router'
import EmployeForm from '@/components/employe/EmployeForm.vue'

const route = useRoute()
</script>

<template><EmployeForm :id="route.params.id" /></template>
