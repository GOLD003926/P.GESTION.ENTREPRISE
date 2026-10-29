<script setup>
import PosteForm from '@/components/organisation/PosteForm.vue'

defineProps({ id: { type: String, default: null } })
</script>

<template>
  <PosteForm :id="id" />
</template>
