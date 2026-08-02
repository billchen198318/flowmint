<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { toast } from "vue3-toastify";
import Toolbar from "@/components/Toolbar.vue";
import { escapeQifuHtmlMsg, getAxiosInstance } from "@/components/BaseHelper";
import { useSwalLoading } from "@/composables/useSwalLoading";
import { PageConstants } from "../config";
const props = defineProps<{ edit: boolean }>(),
  route = useRoute(),
  router = useRouter(),
  tenants = ref<any[]>([]),
  checkFields = ref<any>({});
const localNow = () => {
    const d = new Date();
    return new Date(d.getTime() - d.getTimezoneOffset() * 60000)
      .toISOString()
      .slice(0, 16);
  },
  blank = () => ({
    tenantId: "",
    schemeCode: "",
    schemeName: "",
    isDefault: "N",
    status: "ACTIVE",
    effectiveFrom: localNow(),
    effectiveTo: "",
    description: "",
    levels: [
      {
        levelCode: "COMPANY",
        levelName: "公司層",
        levelOrder: 0,
        isHighestLevel: "Y",
        status: "ACTIVE",
        effectiveFrom: localNow(),
        effectiveTo: "",
        description: "",
      },
    ],
  });
const form = ref<any>(blank()),
  { showLoading, hideLoading, confirmFire } = useSwalLoading();
const dates = (v: any) => ({
  ...v,
  effectiveFrom: v.effectiveFrom
    ? new Date(v.effectiveFrom).toISOString()
    : null,
  effectiveTo: v.effectiveTo ? new Date(v.effectiveTo).toISOString() : null,
  levels: v.levels.map((x: any) => ({
    ...x,
    effectiveFrom: new Date(x.effectiveFrom).toISOString(),
    effectiveTo: x.effectiveTo ? new Date(x.effectiveTo).toISOString() : null,
  })),
});
const local = (v: any) => {
  const f = (x: any) =>
    x
      ? new Date(
          new Date(x).getTime() - new Date(x).getTimezoneOffset() * 60000,
        )
          .toISOString()
          .slice(0, 16)
      : "";
  form.value = {
    ...v,
    effectiveFrom: f(v.effectiveFrom),
    effectiveTo: f(v.effectiveTo),
    levels: v.levels.map((x: any) => ({
      ...x,
      effectiveFrom: f(x.effectiveFrom),
      effectiveTo: f(x.effectiveTo),
    })),
  };
};
const load = async () => {
  if (!props.edit) return;
  const x = await getAxiosInstance().post(
    import.meta.env.VITE_API_URL + PageConstants.eventNamespace + "/load",
    { oid: route.params.id },
  );
  if (x.data?.success === import.meta.env.VITE_SUCCESS_FLAG)
    local(x.data.value);
  else toast.warning(escapeQifuHtmlMsg(x.data?.message));
};
const save = async () => {
  showLoading();
  try {
    const x = await getAxiosInstance().post(
      import.meta.env.VITE_API_URL +
        PageConstants.eventNamespace +
        (props.edit ? "/update" : "/save"),
      dates(form.value),
    );
    checkFields.value = x.data?.checkFields || {};
    if (x.data?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      toast.warning(escapeQifuHtmlMsg(x.data?.message));
      return;
    }
    toast.success(x.data.message);
    if (props.edit) local(x.data.value);
    else form.value = blank();
  } finally {
    hideLoading();
  }
};
const clear = () => {
  checkFields.value = {};
  form.value = blank();
};
const deactivate = async () => {
  showLoading();
  try {
    const response = await getAxiosInstance().post(
      import.meta.env.VITE_API_URL +
        PageConstants.eventNamespace +
        "/deactivate",
      { oid: form.value.oid },
    );
    if (response.data?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      toast.warning(escapeQifuHtmlMsg(response.data?.message));
      return;
    }
    toast.success(response.data.message);
    local(response.data.value);
  } finally {
    hideLoading();
  }
};
const confirmDeactivate = () =>
  confirmFire("確定停用此組織簽核層級方案？", deactivate);
const add = () =>
  form.value.levels.push({
    levelCode: "",
    levelName: "",
    levelOrder: (form.value.levels.length + 1) * 10,
    isHighestLevel: "N",
    status: "ACTIVE",
    effectiveFrom: form.value.effectiveFrom,
    effectiveTo: "",
    description: "",
  });
const remove = (i: number) => form.value.levels.splice(i, 1);
onMounted(async () => {
  const x = await getAxiosInstance().post(
    import.meta.env.VITE_API_URL +
      PageConstants.eventNamespace +
      "/tenant-options",
  );
  tenants.value = x.data?.value || [];
  await load();
});
</script>
<template>
  <Toolbar
    :progId="edit ? PageConstants.EditId : PageConstants.CreateId"
    description="組織簽核層級"
    backFlag="Y"
    saveFlag="Y"
    refreshFlag="Y"
    @backMethod="router.back()"
    @saveMethod="save"
    @refreshMethod="edit ? load() : clear()"
  />
  <div class="card mb-3">
    <div class="card-body row g-3">
      <div class="col-md-4">
        <label>Tenant</label
        ><select v-model="form.tenantId" :disabled="edit" class="form-select">
          <option value="">請選擇</option>
          <option v-for="x in tenants" :value="x.value">{{ x.label }}</option>
        </select>
      </div>
      <div class="col-md-4">
        <label>方案代碼</label
        ><input v-model="form.schemeCode" class="form-control" />
      </div>
      <div class="col-md-4">
        <label>方案名稱</label
        ><input v-model="form.schemeName" class="form-control" />
      </div>
      <div class="col-md-3">
        <label>預設方案</label
        ><select v-model="form.isDefault" class="form-select">
          <option value="N">否</option>
          <option value="Y">是</option>
        </select>
      </div>
      <div class="col-md-3">
        <label>狀態</label
        ><select v-model="form.status" class="form-select">
          <option>ACTIVE</option>
          <option>INACTIVE</option>
        </select>
      </div>
      <div class="col-md-3">
        <label>生效時間</label
        ><input
          v-model="form.effectiveFrom"
          type="datetime-local"
          class="form-control"
        />
      </div>
      <div class="col-md-3">
        <label>失效時間</label
        ><input
          v-model="form.effectiveTo"
          type="datetime-local"
          class="form-control"
        />
      </div>
    </div>
  </div>
  <div class="card">
    <div class="card-header d-flex justify-content-between">
      層級明細<button class="btn btn-sm btn-primary" @click="add">
        新增層級
      </button>
    </div>
    <div class="card-body table-responsive">
      <table class="table">
        <thead>
          <tr>
            <th>順序</th>
            <th>代碼</th>
            <th>名稱</th>
            <th>最高</th>
            <th>狀態</th>
            <th>生效時間</th>
            <th>失效時間</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(x, i) in form.levels" :key="x.oid || i">
            <td>
              <input
                v-model.number="x.levelOrder"
                type="number"
                min="0"
                class="form-control"
              />
            </td>
            <td><input v-model="x.levelCode" class="form-control" /></td>
            <td><input v-model="x.levelName" class="form-control" /></td>
            <td>
              <select v-model="x.isHighestLevel" class="form-select">
                <option value="N">否</option>
                <option value="Y">是</option>
              </select>
            </td>
            <td>
              <select v-model="x.status" class="form-select">
                <option>ACTIVE</option>
                <option>INACTIVE</option>
              </select>
            </td>
            <td>
              <input
                v-model="x.effectiveFrom"
                type="datetime-local"
                class="form-control"
              />
            </td>
            <td>
              <input
                v-model="x.effectiveTo"
                type="datetime-local"
                class="form-control"
              />
            </td>
            <td>
              <button class="btn btn-outline-danger btn-sm" @click="remove(i)">
                移除
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      <div class="d-flex gap-2 mt-4">
        <button type="button" class="btn btn-primary" @click="save">
          <i class="bi bi-save"></i> 儲存
        </button>
        <button
          v-if="!edit"
          type="button"
          class="btn btn-outline-secondary"
          @click="clear"
        >
          <i class="bi bi-eraser"></i> 清除
        </button>
        <button
          v-if="edit"
          type="button"
          class="btn btn-outline-secondary"
          @click="load"
        >
          <i class="bi bi-repeat"></i> 重新載入
        </button>
        <button
          v-if="edit && form.status === 'ACTIVE'"
          type="button"
          class="btn btn-outline-danger"
          @click="confirmDeactivate"
        >
          <i class="bi bi-slash-circle"></i> 停用方案
        </button>
      </div>
    </div>
  </div>
</template>
