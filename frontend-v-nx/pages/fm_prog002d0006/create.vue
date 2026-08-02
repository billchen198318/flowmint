<script setup lang="ts">
import OrgDutyForm from "./components/OrgDutyForm.vue";

definePageMeta({ middleware: ["auth"] });
</script>

<template>
  <OrgDutyForm />
</template>
