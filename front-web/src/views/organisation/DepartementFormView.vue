<script setup>
import DepartementForm from '@/components/organisation/DepartementForm.vue'

defineProps({ id: { type: String, default: null } })
</script>

<template>
  <DepartementForm :id="id" />
</template>
