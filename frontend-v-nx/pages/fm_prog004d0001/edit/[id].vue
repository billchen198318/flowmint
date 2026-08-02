<script setup lang="ts">
import ProcessDesignerForm from "../components/ProcessDesignerForm.vue";
definePageMeta({ middleware: ["auth"] });
</script>
<template><ProcessDesignerForm edit /></template>
