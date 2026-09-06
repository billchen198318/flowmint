<script setup lang="ts">
import { computed, onBeforeUnmount, ref } from "vue";
import { bpmnDesignerGuide } from "./bpmnDesignerGuide";

const dialog = ref<HTMLDialogElement | null>(null);
const search = ref("");
const selectedId = ref("quick-start");
const content = ref<HTMLElement | null>(null);
let previousFocus: HTMLElement | null = null;
let previousOverflow: string | null = null;
const sections = computed(() => {
  const query = search.value.trim().toLocaleLowerCase();
  return bpmnDesignerGuide.filter((section) =>
    JSON.stringify(section).toLocaleLowerCase().includes(query),
  );
});
const activeSection = computed(
  () =>
    sections.value.find((section) => section.id === selectedId.value) ||
    sections.value[0],
);
const select = (id: string) => {
  selectedId.value = id;
  content.value?.scrollTo({ top: 0 });
};
const open = (topic = "quick-start") => {
  if (!dialog.value || dialog.value.open) return;
  search.value = "";
  select(topic);
  previousFocus =
    document.activeElement instanceof HTMLElement
      ? document.activeElement
      : null;
  previousOverflow = document.body.style.overflow;
  dialog.value.showModal();
  document.body.style.overflow = "hidden";
};
const restore = () => {
  if (previousOverflow !== null) {
    document.body.style.overflow = previousOverflow;
    previousOverflow = null;
  }
  if (previousFocus?.isConnected) previousFocus.focus();
  previousFocus = null;
};
const close = () => dialog.value?.close();
onBeforeUnmount(restore);
defineExpose({ open });
</script>

<template>
  <dialog
    ref="dialog"
    class="bpmn-help border rounded shadow"
    aria-labelledby="bpmn-help-title"
    @close="restore"
    @cancel.prevent="close"
    @keydown.stop
  >
    <div class="help-layout">
      <div
        class="d-flex justify-content-between align-items-center border-bottom p-3 gap-3"
      >
        <div>
          <h2 id="bpmn-help-title" class="h5 mb-1">BPMN 流程設計說明</h2>
          <div class="small text-secondary">
            操作步驟、節點配置、範例與發布檢查
          </div>
        </div>
        <button
          type="button"
          class="btn-close"
          aria-label="關閉說明"
          @click="close"
        ></button>
      </div>
      <div class="help-body">
        <nav class="help-nav border-end p-3" aria-label="說明主題">
          <label for="bpmn-help-search" class="form-label">搜尋說明</label>
          <input
            id="bpmn-help-search"
            v-model="search"
            type="search"
            class="form-control mb-3"
            placeholder="例如：會簽、Gateway、JSON"
            @input="content?.scrollTo({ top: 0 })"
          />
          <div class="small text-secondary mb-2" role="status">
            {{ sections.length }} 個主題
          </div>
          <div class="list-group">
            <button
              v-for="section in sections"
              :key="section.id"
              type="button"
              class="list-group-item list-group-item-action"
              :class="{ active: activeSection?.id === section.id }"
              :aria-current="
                activeSection?.id === section.id ? 'true' : undefined
              "
              @click="select(section.id)"
            >
              {{ section.title }}
            </button>
          </div>
        </nav>
        <article
          ref="content"
          class="help-content p-3 p-lg-4"
          tabindex="0"
          aria-label="主題內容"
        >
          <template v-if="activeSection">
            <h3 class="h4 mb-3">{{ activeSection.title }}</h3>
            <p>{{ activeSection.intro }}</p>
            <ol v-if="activeSection.steps" class="ps-4">
              <li v-for="step in activeSection.steps" :key="step" class="mb-3">
                {{ step }}
              </li>
            </ol>
            <div v-if="activeSection.rows" class="table-responsive">
              <table class="table table-bordered align-top">
                <caption class="visually-hidden">
                  {{
                    activeSection.title
                  }}配置對照
                </caption>
                <thead>
                  <tr>
                    <th scope="col">項目</th>
                    <th scope="col">配置與行為</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="[label, description] in activeSection.rows"
                    :key="label"
                  >
                    <th scope="row" class="help-label">{{ label }}</th>
                    <td>{{ description }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
            <div
              v-for="example in activeSection.examples"
              :key="example.title"
              class="my-3"
            >
              <h4 class="h6">{{ example.title }}</h4>
              <pre
                class="bg-body-tertiary border rounded p-3"
              ><code>{{ example.code }}</code></pre>
            </div>
            <aside v-if="activeSection.notes" class="alert alert-info mb-0">
              <p
                v-for="note in activeSection.notes"
                :key="note"
                class="help-note"
              >
                {{ note }}
              </p>
            </aside>
          </template>
          <p v-else role="status" class="text-secondary">
            找不到符合的說明，請改用其他關鍵字或清空搜尋。
          </p>
        </article>
      </div>
      <div
        class="border-top p-3 d-flex justify-content-between align-items-center gap-3"
      >
        <span class="small text-secondary"
          >Esc 可關閉，返回原本的流程配置畫面。</span
        >
        <button type="button" class="btn btn-secondary" @click="close">
          關閉
        </button>
      </div>
    </div>
  </dialog>
</template>

<style scoped>
.bpmn-help {
  width: min(1200px, calc(100vw - 2rem));
  max-width: none;
  max-height: calc(100dvh - 2rem);
  padding: 0;
  color: var(--bs-body-color);
  background: var(--bs-body-bg);
}
.bpmn-help::backdrop {
  background: rgba(0, 0, 0, 0.5);
}
.help-layout {
  display: flex;
  flex-direction: column;
  height: min(820px, calc(100dvh - 2rem - 2px));
}
.help-body {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  min-height: 0;
  flex: 1;
}
.help-nav,
.help-content {
  overflow-y: auto;
  overscroll-behavior: contain;
}
.help-label {
  width: 30%;
  min-width: 150px;
  overflow-wrap: anywhere;
}
pre {
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}
.help-note:last-child {
  margin-bottom: 0;
}
@media (max-width: 767px) {
  .help-body {
    grid-template-columns: minmax(0, 1fr);
    grid-template-rows: minmax(130px, 30%) minmax(0, 1fr);
  }
  .help-nav {
    border-bottom: 1px solid var(--bs-border-color);
  }
}
</style>
