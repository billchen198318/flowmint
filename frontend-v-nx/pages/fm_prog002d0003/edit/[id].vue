<script setup lang="ts">
import SchemeEditor from "../components/SchemeEditor.vue";
definePageMeta({ middleware: ["auth"] });
</script>
<template><SchemeEditor :edit="true" /></template>
