<script setup lang="ts">
definePageMeta({ middleware: ["auth"] });

await navigateTo("/workspace", { replace: true });
</script>

<template></template>
