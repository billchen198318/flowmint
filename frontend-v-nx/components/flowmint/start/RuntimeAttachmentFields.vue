<script setup lang="ts">
defineProps<{
  fields: any[];
  files: Record<string, any[]>;
  uploadSessionId: string;
  uploadingField: string;
  disabled: boolean;
  acceptedFileTypes: (field: any) => string;
}>();

defineEmits<{
  upload: [field: any, event: Event];
  delete: [field: any, attachment: any];
}>();
</script>

<template>
  <div v-if="fields.length" class="attachments mt-4">
    <div v-for="field in fields" :key="field.key" class="attachment-field">
      <label class="form-label fw-semibold">
        {{ field.label || field.key }}
        <span v-if="field.validate?.required" class="text-danger">*</span>
      </label>
      <input
        class="form-control"
        type="file"
        :accept="acceptedFileTypes(field)"
        :disabled="!uploadSessionId || uploadingField === field.key || disabled"
        @change="$emit('upload', field, $event)"
      >
      <div class="form-text">
        單檔上限 {{ field.fileMaxSize || "8MB" }}；最多
        {{ field.maxNumberOfFiles || (field.multiple ? 10 : 1) }} 個。
      </div>
      <ul v-if="files[field.key]?.length" class="list-group mt-2">
        <li
          v-for="file in files[field.key]"
          :key="file.attachmentId"
          class="list-group-item d-flex justify-content-between align-items-center"
        >
          <span>{{ file.fileName }}</span>
          <button
            type="button"
            class="btn btn-sm btn-outline-danger"
            :disabled="disabled"
            @click="$emit('delete', field, file)"
          >
            刪除
          </button>
        </li>
      </ul>
    </div>
  </div>
</template>

<style scoped>
.attachment-field {
  margin-bottom: 1rem;
  padding: 1rem;
  border: 1px solid #e1e7ef;
  border-radius: .8rem;
}
</style>
