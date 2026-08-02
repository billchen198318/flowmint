<script setup lang="ts">
import ApprovalGroupForm from "../components/ApprovalGroupForm.vue";

definePageMeta({ middleware: ["auth"] });
</script>

<template>
  <ApprovalGroupForm :edit="true" />
</template>
