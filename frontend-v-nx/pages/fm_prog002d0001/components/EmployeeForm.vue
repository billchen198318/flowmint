<script setup lang="ts">
import { checkInvalid, invalidFeedback } from "@/components/BaseHelper";

const model = defineModel<any>({ required: true });

defineProps<{
  checkFields: Record<string, string>;
  tenantOptions: any[];
  accountOptions: any[];
  tenantReadonly?: boolean;
}>();
</script>

<template>
  <div class="row g-3">
    <div class="col-md-4">
      <label for="tenantId" class="form-label">Tenant</label>
      <select
        id="tenantId"
        v-model="model.tenantId"
        :disabled="tenantReadonly"
        :class="[
          'form-select',
          checkInvalid('tenantId', checkFields) ? 'is-invalid' : '',
        ]"
      >
        <option value="">請選擇 Tenant</option>
        <option v-for="item in tenantOptions" :key="item.value" :value="item.value">
          {{ item.label }}
        </option>
      </select>
      <div v-if="checkInvalid('tenantId', checkFields)" class="invalid-feedback">
        {{ invalidFeedback("tenantId", checkFields) }}
      </div>
    </div>

    <div class="col-md-4">
      <label for="employeeNo" class="form-label">員工編號</label>
      <input
        id="employeeNo"
        v-model="model.employeeNo"
        :class="[
          'form-control',
          checkInvalid('employeeNo', checkFields) ? 'is-invalid' : '',
        ]"
      />
      <div v-if="checkInvalid('employeeNo', checkFields)" class="invalid-feedback">
        {{ invalidFeedback("employeeNo", checkFields) }}
      </div>
    </div>

    <div class="col-md-4">
      <label for="account" class="form-label">登入帳號</label>
      <select
        id="account"
        v-model="model.account"
        :class="[
          'form-select',
          checkInvalid('account', checkFields) ? 'is-invalid' : '',
        ]"
      >
        <option value="">請選擇帳號</option>
        <option v-for="item in accountOptions" :key="item.value" :value="item.value">
          {{ item.label }}
        </option>
      </select>
      <div v-if="checkInvalid('account', checkFields)" class="invalid-feedback">
        {{ invalidFeedback("account", checkFields) }}
      </div>
    </div>

    <div class="col-md-4">
      <label for="displayName" class="form-label">顯示名稱</label>
      <input
        id="displayName"
        v-model="model.displayName"
        :class="[
          'form-control',
          checkInvalid('displayName', checkFields) ? 'is-invalid' : '',
        ]"
      />
      <div v-if="checkInvalid('displayName', checkFields)" class="invalid-feedback">
        {{ invalidFeedback("displayName", checkFields) }}
      </div>
    </div>

    <div class="col-md-4">
      <label for="email" class="form-label">Email</label>
      <input id="email" v-model="model.email" type="email" class="form-control" />
    </div>
    <div class="col-md-4">
      <label for="mobile" class="form-label">手機</label>
      <input id="mobile" v-model="model.mobile" class="form-control" />
    </div>
    <div class="col-md-3">
      <label for="locale" class="form-label">語系</label>
      <input id="locale" v-model="model.locale" class="form-control" />
    </div>
    <div class="col-md-3">
      <label for="timezone" class="form-label">時區</label>
      <input id="timezone" v-model="model.timezone" class="form-control" />
    </div>
    <div class="col-md-3">
      <label for="status" class="form-label">狀態</label>
      <select id="status" v-model="model.status" class="form-select">
        <option value="ACTIVE">啟用</option>
        <option value="INACTIVE">停用</option>
      </select>
    </div>
    <div class="col-md-3">
      <label for="effectiveFrom" class="form-label">生效時間</label>
      <input
        id="effectiveFrom"
        v-model="model.effectiveFrom"
        type="datetime-local"
        :class="[
          'form-control',
          checkInvalid('effectiveFrom', checkFields) ? 'is-invalid' : '',
        ]"
      />
      <div v-if="checkInvalid('effectiveFrom', checkFields)" class="invalid-feedback">
        {{ invalidFeedback("effectiveFrom", checkFields) }}
      </div>
    </div>
    <div class="col-md-3">
      <label for="effectiveTo" class="form-label">失效時間</label>
      <input id="effectiveTo" v-model="model.effectiveTo" type="datetime-local" class="form-control" />
    </div>
    <div class="col-12">
      <label for="description" class="form-label">說明</label>
      <textarea id="description" v-model="model.description" class="form-control" maxlength="500"></textarea>
    </div>
  </div>
</template>
