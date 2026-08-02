<script setup lang="ts">
import OrgTitleForm from "./components/OrgTitleForm.vue";
definePageMeta({ middleware: ["auth"] });
</script>
<template><OrgTitleForm :edit="false" /></template>
