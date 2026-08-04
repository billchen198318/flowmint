<script setup lang="ts">
import DataSourcePoolForm from "../components/DataSourcePoolForm.vue";

definePageMeta({ middleware: ["auth"] });
</script>

<template>
  <DataSourcePoolForm edit />
</template>
