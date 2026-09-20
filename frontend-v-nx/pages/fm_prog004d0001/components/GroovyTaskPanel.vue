<script setup lang="ts">
import { computed, ref } from "vue";
import { toast } from "vue3-toastify";
import GroovyEditorDialog from "./GroovyEditorDialog.vue";
import { formGroovyFields, type GroovyBinding } from "../bpmn/groovyContract";
import type { PrepareGroovyPreview } from "../bpmn/groovyPreview";

const props = defineProps<{
  element: any;
  modeler: any;
  disabled?: boolean;
  schemaContent: string;
  preparePreview: PrepareGroovyPreview;
}>();
const dialog = ref<InstanceType<typeof GroovyEditorDialog> | null>(null);
const fields = computed(() => formGroovyFields(props.schemaContent));
const revision = ref(0);
const binding = computed<GroovyBinding | undefined>(() => {
  void revision.value;
  return props.element?.businessObject?.$groovyBinding;
});
const open = () => {
  if (!binding.value) {
    toast.warning("此節點缺少版本腳本資料，請重新載入完整流程版本。");
    return;
  }
  dialog.value?.open(
    JSON.parse(JSON.stringify({ ...binding.value, nodeId: props.element.id })),
  );
};
const apply = (value: GroovyBinding) => {
  if (
    props.disabled ||
    !props.modeler?.get("elementRegistry").get(props.element.id)
  )
    return;
  props.modeler.get("commandStack").execute("flowmint.updateGroovyBinding", {
    element: props.element,
    binding: value,
  });
  revision.value++;
};
</script>

<template>
  <div>
    <div class="alert alert-info small">
      腳本可隨草稿保存。檢查／試跑與正式發布須由管理員完成服務啟用及專用權限配置；發布時後端會重新驗證並固定版本。
    </div>
    <label class="form-label" for="groovy-node-name">節點名稱</label>
    <input
      id="groovy-node-name"
      :value="element.businessObject.name || ''"
      class="form-control mb-3"
      :disabled="disabled"
      @input="
        modeler.get('modeling').updateProperties(element, {
          name: ($event.target as HTMLInputElement).value,
        })
      "
    />
    <div class="small text-secondary mb-3">
      節點：{{ element.id }}<br />類型：GROOVY<br />
      腳本：{{
        binding?.scriptContent.split("\n").length || 0
      }}
      行<br />狀態：尚未編譯驗證
    </div>
    <button type="button" class="btn btn-outline-primary" @click="open">
      {{ disabled ? "檢視腳本" : "編輯腳本" }}
    </button>
    <GroovyEditorDialog
      ref="dialog"
      :fields="fields"
      :readonly="disabled"
      :prepare-preview="preparePreview"
      @apply="apply"
    />
  </div>
</template>
