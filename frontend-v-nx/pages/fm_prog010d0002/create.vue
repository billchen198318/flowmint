<script setup lang="ts">
import ApiClientForm from "./components/ApiClientForm.vue";
definePageMeta({ middleware: ["auth"] });
</script>

<template><ApiClientForm /></template>
