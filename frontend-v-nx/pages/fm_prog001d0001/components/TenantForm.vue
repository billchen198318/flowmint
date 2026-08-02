<script setup lang="ts">
import { checkInvalid, invalidFeedback } from "@/components/BaseHelper";

const model = defineModel<any>({ required: true });

defineProps<{
  checkFields: Record<string, string>;
  tenantIdReadonly?: boolean;
  tenantCodeReadonly?: boolean;
}>();
</script>

<template>
  <div class="row g-3">
    <div class="col-md-6">
      <label for="tenantId" class="form-label">Tenant ID</label>
      <input
        id="tenantId"
        v-model="model.tenantId"
        :readonly="tenantIdReadonly"
        :class="[
          'form-control',
          checkInvalid('tenantId', checkFields) ? 'is-invalid' : '',
        ]"
      />
      <div
        v-if="checkInvalid('tenantId', checkFields)"
        class="invalid-feedback"
      >
        {{ invalidFeedback("tenantId", checkFields) }}
      </div>
    </div>

    <div class="col-md-6">
      <label for="tenantCode" class="form-label">Tenant 代碼</label>
      <input
        id="tenantCode"
        v-model="model.tenantCode"
        :readonly="tenantCodeReadonly"
        :class="[
          'form-control',
          checkInvalid('tenantCode', checkFields) ? 'is-invalid' : '',
        ]"
      />
      <div
        v-if="checkInvalid('tenantCode', checkFields)"
        class="invalid-feedback"
      >
        {{ invalidFeedback("tenantCode", checkFields) }}
      </div>
    </div>

    <div class="col-md-6">
      <label for="tenantName" class="form-label">Tenant 名稱</label>
      <input
        id="tenantName"
        v-model="model.tenantName"
        :class="[
          'form-control',
          checkInvalid('tenantName', checkFields) ? 'is-invalid' : '',
        ]"
      />
      <div
        v-if="checkInvalid('tenantName', checkFields)"
        class="invalid-feedback"
      >
        {{ invalidFeedback("tenantName", checkFields) }}
      </div>
    </div>

    <div class="col-md-3">
      <label for="defaultLocale" class="form-label">預設語系</label>
      <input
        id="defaultLocale"
        v-model="model.defaultLocale"
        class="form-control"
      />
    </div>

    <div class="col-md-3">
      <label for="defaultTimezone" class="form-label">預設時區</label>
      <input
        id="defaultTimezone"
        v-model="model.defaultTimezone"
        class="form-control"
      />
    </div>

    <div class="col-md-3">
      <label for="status" class="form-label">狀態</label>
      <select id="status" v-model="model.status" class="form-select">
        <option value="ACTIVE">啟用</option>
        <option value="INACTIVE">停用</option>
      </select>
    </div>

    <div class="col-12">
      <label for="description" class="form-label">說明</label>
      <textarea
        id="description"
        v-model="model.description"
        class="form-control"
        maxlength="500"
      ></textarea>
    </div>
  </div>
</template>
