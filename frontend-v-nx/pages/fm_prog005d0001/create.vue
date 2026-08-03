<script setup lang="ts">
import FormDesigner from "./components/FormDesigner.vue";
definePageMeta({ middleware: ["auth"] });
</script>

<template>
  <FormDesigner />
</template>
