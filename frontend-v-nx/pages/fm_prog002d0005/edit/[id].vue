<script setup lang="ts">
import OrgUnitHeadForm from "../components/OrgUnitHeadForm.vue";
definePageMeta({ middleware: ["auth"] });
</script>
<template><OrgUnitHeadForm :edit="true" /></template>
