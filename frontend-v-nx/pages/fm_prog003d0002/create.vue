<script setup lang="ts">
import WorkflowDelegationForm from "./components/WorkflowDelegationForm.vue";

definePageMeta({ middleware: ["auth"] });
</script>

<template>
  <WorkflowDelegationForm />
</template>
