<script setup lang="ts">
import { onMounted } from 'vue';
import { checkHasRole } from '../components/BaseHelper';

definePageMeta({ middleware: ['auth'] });

onMounted(() => {
  if (checkHasRole('admin') || checkHasRole('*')) {
    navigateTo('/dashboard');
  }
});
</script>

<template>
  <div class="callout callout-info">
    <h4><i class="bi bi-info-circle"></i>&nbsp;Welcome</h4>
    Welcome to FlowMint
    <br>
    請點選左邊選單，進行操作。
    <br>
  </div>
  <div class="flowmint-brand-panel">
    <i class="bi bi-diagram-3"></i>
    <span>FlowMint</span>
  </div>
</template>

<style scoped>
/* 加入 index 專屬樣式 */
</style>
