<script setup lang="ts">
import DataActionForm from "./components/DataActionForm.vue";

definePageMeta({ middleware: ["auth"] });
</script>

<template>
  <DataActionForm />
</template>
