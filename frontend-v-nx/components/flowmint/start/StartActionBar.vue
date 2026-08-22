<script setup lang="ts">
defineProps<{
  cancelTo: Record<string, any>;
  submitting: boolean;
}>();

defineEmits<{ submit: [] }>();
</script>

<template>
  <div class="request-actions">
    <NuxtLink :to="cancelTo" class="btn btn-light">取消</NuxtLink>
    <button
      type="button"
      class="btn btn-primary px-4"
      :disabled="submitting"
      @click="$emit('submit')"
    >
      <span v-if="submitting" class="spinner-border spinner-border-sm me-2"></span>
      送出申請
    </button>
  </div>
</template>

<style scoped>
.request-actions {
  position: sticky;
  bottom: 0;
  display: flex;
  justify-content: flex-end;
  gap: .75rem;
  padding: 1rem 1.4rem;
  border-top: 1px solid #e5eaf1;
  background: rgba(255, 255, 255, .96);
  backdrop-filter: blur(10px);
}

@media (max-width: 767.98px) {
  .request-actions {
    justify-content: stretch;
  }

  .request-actions > * {
    flex: 1;
  }
}
</style>
