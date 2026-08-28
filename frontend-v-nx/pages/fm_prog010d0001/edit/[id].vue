<script setup lang="ts">
import AiProviderForm from "../components/AiProviderForm.vue";

definePageMeta({ middleware: ["auth"] });
</script>

<template>
  <AiProviderForm edit />
</template>
