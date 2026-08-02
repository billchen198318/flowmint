<script setup lang="ts">
import { checkInvalid, invalidFeedback } from "@/components/BaseHelper";

defineProps<{
  assignments: any[];
  assignmentForm: any;
  checkFields: Record<string, string>;
  orgUnitOptions: any[];
  titleOptions: any[];
  managerOptions: any[];
}>();

const emit = defineEmits<{
  (event: "save"): void;
  (event: "reset"): void;
  (event: "edit", value: any): void;
  (event: "deactivate", value: any): void;
}>();

const managerSourceLabels: Record<string, string> = {
  ORG_HEAD: "本部門主管",
  PARENT_HEAD: "上層部門主管",
  EXPLICIT: "指定員工任職",
  NONE: "不設定直屬主管",
};
</script>

<template>
  <div class="card mt-4">
    <div class="card-header">
      <h5 class="mb-1">部門任職與直屬主管</h5>
      <small class="text-muted">
        設定員工所屬部門、職稱及主管來源。若選「本部門主管」，系統會由該部門的主管配置判定；只有特殊例外才使用「指定員工任職」。
      </small>
    </div>
    <div class="card-body">
      <div class="row g-3">
        <div class="col-md-4">
          <label for="assignmentOrgUnitId" class="form-label">部門</label>
          <select
            id="assignmentOrgUnitId"
            v-model="assignmentForm.orgUnitId"
            :class="['form-select', checkInvalid('orgUnitId', checkFields) ? 'is-invalid' : '']"
          >
            <option value="">請選擇部門</option>
            <option v-for="item in orgUnitOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </option>
          </select>
          <div v-if="checkInvalid('orgUnitId', checkFields)" class="invalid-feedback">
            {{ invalidFeedback("orgUnitId", checkFields) }}
          </div>
        </div>

        <div class="col-md-4">
          <label for="assignmentTitleId" class="form-label">職稱</label>
          <select
            id="assignmentTitleId"
            v-model="assignmentForm.titleId"
            :class="['form-select', checkInvalid('titleId', checkFields) ? 'is-invalid' : '']"
          >
            <option value="">請選擇職稱</option>
            <option v-for="item in titleOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </option>
          </select>
          <div v-if="checkInvalid('titleId', checkFields)" class="invalid-feedback">
            {{ invalidFeedback("titleId", checkFields) }}
          </div>
        </div>

        <div class="col-md-4">
          <label for="assignmentManagerSource" class="form-label">直屬主管來源</label>
          <select
            id="assignmentManagerSource"
            v-model="assignmentForm.managerSource"
            :class="['form-select', checkInvalid('managerSource', checkFields) ? 'is-invalid' : '']"
          >
            <option value="ORG_HEAD">本部門主管</option>
            <option value="PARENT_HEAD">上層部門主管</option>
            <option value="EXPLICIT">指定員工任職</option>
            <option value="NONE">不設定直屬主管</option>
          </select>
          <div v-if="checkInvalid('managerSource', checkFields)" class="invalid-feedback">
            {{ invalidFeedback("managerSource", checkFields) }}
          </div>
        </div>

        <div v-if="assignmentForm.managerSource === 'EXPLICIT'" class="col-md-4">
          <label for="directManagerAssignmentId" class="form-label">指定直屬主管</label>
          <select
            id="directManagerAssignmentId"
            v-model="assignmentForm.directManagerAssignmentId"
            :class="[
              'form-select',
              checkInvalid('directManagerAssignmentId', checkFields) ? 'is-invalid' : '',
            ]"
          >
            <option value="">請選擇主管（員工／部門）</option>
            <option v-for="item in managerOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </option>
          </select>
          <div v-if="checkInvalid('directManagerAssignmentId', checkFields)" class="invalid-feedback">
            {{ invalidFeedback("directManagerAssignmentId", checkFields) }}
          </div>
        </div>

        <div class="col-md-2">
          <label for="assignmentPrimary" class="form-label">主要任職</label>
          <select id="assignmentPrimary" v-model="assignmentForm.isPrimary" class="form-select">
            <option value="Y">是</option>
            <option value="N">否</option>
          </select>
        </div>
        <div class="col-md-2">
          <label for="assignmentStatus" class="form-label">狀態</label>
          <select id="assignmentStatus" v-model="assignmentForm.status" class="form-select">
            <option value="ACTIVE">啟用</option>
            <option value="INACTIVE">停用</option>
          </select>
        </div>
        <div class="col-md-3">
          <label for="assignmentEffectiveFrom" class="form-label">生效時間</label>
          <input
            id="assignmentEffectiveFrom"
            v-model="assignmentForm.effectiveFrom"
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
          <label for="assignmentEffectiveTo" class="form-label">失效時間</label>
          <input
            id="assignmentEffectiveTo"
            v-model="assignmentForm.effectiveTo"
            type="datetime-local"
            class="form-control"
          />
        </div>
      </div>

      <div class="d-flex gap-2 mt-3">
        <button type="button" class="btn btn-primary" @click="emit('save')">
          <i class="bi bi-save"></i> {{ assignmentForm.oid ? "更新任職" : "新增任職" }}
        </button>
        <button type="button" class="btn btn-outline-secondary" @click="emit('reset')">
          <i class="bi bi-eraser"></i> 清除任職表單
        </button>
      </div>

      <div class="table-responsive mt-4">
        <table class="table table-striped table-hover align-middle">
          <thead>
            <tr>
              <th>部門</th>
              <th>職稱</th>
              <th>主管來源</th>
              <th>指定主管</th>
              <th>主要</th>
              <th>狀態</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in assignments" :key="item.oid">
              <td>{{ item.orgUnitLabel }}</td>
              <td>{{ item.titleLabel }}</td>
              <td>{{ managerSourceLabels[item.managerSource] || item.managerSource }}</td>
              <td>{{ item.directManagerLabel || "—" }}</td>
              <td>{{ item.isPrimary === "Y" ? "是" : "否" }}</td>
              <td>{{ item.status === "ACTIVE" ? "啟用" : "停用" }}</td>
              <td class="text-nowrap">
                <button type="button" class="btn btn-sm btn-outline-primary me-2" @click="emit('edit', item)">
                  編輯
                </button>
                <button
                  v-if="item.status === 'ACTIVE'"
                  type="button"
                  class="btn btn-sm btn-outline-danger"
                  @click="emit('deactivate', item)"
                >
                  停用
                </button>
              </td>
            </tr>
            <tr v-if="assignments.length === 0">
              <td colspan="7" class="text-center text-muted py-4">尚未設定部門任職</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
