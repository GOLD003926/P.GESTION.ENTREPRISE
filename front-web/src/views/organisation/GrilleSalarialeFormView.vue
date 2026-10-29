<script setup>
import GrilleSalarialeForm from '@/components/organisation/GrilleSalarialeForm.vue'

defineProps({ id: { type: String, default: null } })
</script>

<template>
  <GrilleSalarialeForm :id="id" />
</template>
