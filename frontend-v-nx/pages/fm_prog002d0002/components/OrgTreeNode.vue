<script setup lang="ts">
import { ref } from "vue";

defineOptions({ name: "OrgTreeNode" });

const props = defineProps<{
  node: any;
}>();

const emit = defineEmits<{
  (event: "move", sourceOrgUnitId: string, targetOrgUnitId: string): void;
  (event: "edit", oid: string): void;
}>();

const expanded = ref(true);

const forwardMove = (sourceOrgUnitId: string, targetOrgUnitId: string) => {
  emit("move", sourceOrgUnitId, targetOrgUnitId);
};

const dragStart = (event: DragEvent) => {
  event.dataTransfer?.setData("text/plain", props.node.orgUnitId);
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = "move";
  }
};

const drop = (event: DragEvent) => {
  const sourceOrgUnitId = event.dataTransfer?.getData("text/plain") || "";
  if (sourceOrgUnitId && sourceOrgUnitId !== props.node.orgUnitId) {
    emit("move", sourceOrgUnitId, props.node.orgUnitId);
  }
};
</script>

<template>
  <li class="org-tree-node">
    <div
      class="node-content d-flex align-items-center gap-2"
      :class="{ inactive: node.status !== 'ACTIVE' }"
      draggable="true"
      @dragstart="dragStart"
      @dragover.prevent
      @drop.stop.prevent="drop"
    >
      <button
        type="button"
        class="btn btn-link btn-sm p-0 tree-toggle"
        :disabled="!node.children.length"
        @click="expanded = !expanded"
      >
        <i
          :class="[
            'bi',
            node.children.length
              ? expanded
                ? 'bi-chevron-down'
                : 'bi-chevron-right'
              : 'bi-dot',
          ]"
        ></i>
      </button>

      <i class="bi bi-building"></i>
      <span class="fw-semibold">{{ node.unitCode }}／{{ node.unitName }}</span>
      <span class="badge text-bg-light">{{ node.unitType }}</span>
      <span class="badge text-bg-secondary">v{{ node.currentVersionNo }}</span>
      <span v-if="node.isVirtual === 'Y'" class="badge text-bg-info">虛擬</span>
      <span v-if="node.status !== 'ACTIVE'" class="badge text-bg-danger">
        已停用
      </span>

      <button
        type="button"
        class="btn btn-outline-secondary btn-sm ms-auto"
        @click.stop="emit('edit', node.oid)"
      >
        <i class="bi bi-pen"></i>
        編輯
      </button>
    </div>

    <ul v-if="expanded && node.children.length" class="org-tree-children">
      <OrgTreeNode
        v-for="child in node.children"
        :key="child.orgUnitId"
        :node="child"
        @move="forwardMove"
        @edit="emit('edit', $event)"
      />
    </ul>
  </li>
</template>

<style scoped>
.org-tree-node {
  list-style: none;
  margin: 0.35rem 0;
}

.org-tree-children {
  margin: 0.25rem 0 0 1.4rem;
  padding-left: 1rem;
  border-left: 1px dashed #adb5bd;
}

.node-content {
  min-height: 2.6rem;
  padding: 0.4rem 0.65rem;
  border: 1px solid #dee2e6;
  border-radius: 0.375rem;
  background: #ffffff;
  cursor: grab;
}

.node-content:hover {
  border-color: #0d6efd;
  background: #f8fbff;
}

.node-content.inactive {
  opacity: 0.65;
  background: #f8f9fa;
}

.tree-toggle {
  width: 1.25rem;
  color: #495057;
}
</style>
