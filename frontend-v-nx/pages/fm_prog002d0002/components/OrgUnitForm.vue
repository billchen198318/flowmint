<script setup lang="ts">
import { checkInvalid, invalidFeedback } from "@/components/BaseHelper";

const model = defineModel<any>({ required: true });

defineProps<{
  checkFields: Record<string, string>;
  tenantOptions: any[];
  parentOptions: any[];
  tenantReadonly?: boolean;
  unitCodeReadonly?: boolean;
  parentReadonly?: boolean;
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
        <option
          v-for="item in tenantOptions"
          :key="item.value"
          :value="item.value"
        >
          {{ item.label }}
        </option>
      </select>
      <div
        v-if="checkInvalid('tenantId', checkFields)"
        class="invalid-feedback"
      >
        {{ invalidFeedback("tenantId", checkFields) }}
      </div>
    </div>

    <div class="col-md-4">
      <label for="unitCode" class="form-label">部門代碼</label>
      <input
        id="unitCode"
        v-model="model.unitCode"
        :readonly="unitCodeReadonly"
        :class="[
          'form-control',
          checkInvalid('unitCode', checkFields) ? 'is-invalid' : '',
        ]"
      />
      <div
        v-if="checkInvalid('unitCode', checkFields)"
        class="invalid-feedback"
      >
        {{ invalidFeedback("unitCode", checkFields) }}
      </div>
    </div>

    <div class="col-md-4">
      <label for="unitName" class="form-label">部門名稱</label>
      <input
        id="unitName"
        v-model="model.unitName"
        :class="[
          'form-control',
          checkInvalid('unitName', checkFields) ? 'is-invalid' : '',
        ]"
      />
      <div
        v-if="checkInvalid('unitName', checkFields)"
        class="invalid-feedback"
      >
        {{ invalidFeedback("unitName", checkFields) }}
      </div>
    </div>

    <div class="col-md-4">
      <label for="shortName" class="form-label">部門簡稱</label>
      <input id="shortName" v-model="model.shortName" class="form-control" />
    </div>

    <div class="col-md-4">
      <label for="parentOrgUnitId" class="form-label">父部門</label>
      <select
        id="parentOrgUnitId"
        v-model="model.parentOrgUnitId"
        :disabled="parentReadonly"
        class="form-select"
      >
        <option value="">根部門</option>
        <option
          v-for="item in parentOptions"
          :key="item.orgUnitId"
          :value="item.orgUnitId"
        >
          {{ "　".repeat(item.treeDepth) }}{{ item.unitCode }}／{{
            item.unitName
          }}
        </option>
      </select>
      <div v-if="parentReadonly" class="form-text">
        父部門異動請使用組織樹拖拉功能。
      </div>
    </div>

    <div class="col-md-4">
      <label for="unitType" class="form-label">部門類型</label>
      <select id="unitType" v-model="model.unitType" class="form-select">
        <option value="COMPANY">公司</option>
        <option value="DIVISION">處／中心</option>
        <option value="DEPARTMENT">部門</option>
        <option value="SECTION">課</option>
        <option value="TEAM">組／班</option>
        <option value="OTHER">其他</option>
      </select>
    </div>

    <div class="col-md-3">
      <label for="sortNo" class="form-label">排序</label>
      <input
        id="sortNo"
        v-model.number="model.sortNo"
        type="number"
        class="form-control"
      />
    </div>

    <div class="col-md-3">
      <label for="isVirtual" class="form-label">虛擬部門</label>
      <select id="isVirtual" v-model="model.isVirtual" class="form-select">
        <option value="N">否</option>
        <option value="Y">是</option>
      </select>
    </div>

    <div class="col-md-3">
      <label for="status" class="form-label">狀態</label>
      <select id="status" v-model="model.status" class="form-select">
        <option value="ACTIVE">啟用</option>
        <option value="INACTIVE">停用</option>
      </select>
    </div>

    <div class="col-md-3">
      <label for="currentVersionNo" class="form-label">目前版本</label>
      <input
        id="currentVersionNo"
        :value="model.currentVersionNo || 1"
        disabled
        class="form-control"
      />
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
      <div
        v-if="checkInvalid('effectiveFrom', checkFields)"
        class="invalid-feedback"
      >
        {{ invalidFeedback("effectiveFrom", checkFields) }}
      </div>
    </div>

    <div class="col-md-3">
      <label for="effectiveTo" class="form-label">失效時間</label>
      <input
        id="effectiveTo"
        v-model="model.effectiveTo"
        type="datetime-local"
        class="form-control"
      />
    </div>

    <div class="col-12">
      <label for="description" class="form-label">說明</label>
      <textarea
        id="description"
        v-model="model.description"
        maxlength="500"
        class="form-control"
      ></textarea>
    </div>
  </div>
</template>
