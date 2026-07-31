<script setup lang="ts">
import OrgUnitEditor from "../components/OrgUnitEditor.vue";

definePageMeta({ middleware: ["auth"] });
</script>

<template>
  <OrgUnitEditor :edit="true" />
</template>
