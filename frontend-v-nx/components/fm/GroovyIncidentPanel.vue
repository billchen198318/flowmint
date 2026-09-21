<script setup lang="ts">
import { onBeforeUnmount, ref, watch } from "vue";

const props = defineProps<{ tenantId: string }>();
type Incident = {
  incidentId: string;
  invocationId: string;
  processInstanceId: string;
  processDefId: string;
  versionNo: number;
  nodeId: string;
  attemptNo: number;
  failureCategory: string;
  executionStatus?: string;
  parentInvocationId?: string;
};
const items = ref<Incident[]>([]);
const selected = ref<Incident | null>(null);
const offset = ref(0);
const hasMore = ref(false);
const loaded = ref(false);
const busy = ref(false);
const message = ref("");
type Handling = {
  status: string;
  revision: number;
  hasMore: boolean;
  history: {
    revision: number;
    fromStatus: string;
    toStatus: string;
    reason: string;
    actor: string;
    date: string;
    actionType?: string;
    targetInvocationId?: string;
  }[];
};
const handling = ref<Handling | null>(null);
const handlingMessage = ref("");
const historyOffset = ref(0);
const reason = ref("");
type RetryPreview = {
  invocationId: string;
  revision: number;
  generation: number;
  inputRevision: number;
  inputSha256: string;
  bindingSha256: string;
  remainingAttempts: number;
};
const retryPreview = ref<RetryPreview | null>(null);
const retryMessage = ref("");
type RecalculatePreview = {
  invocationId: string;
  revision: number;
  generation: number;
  previousFormRevision: number;
  formRevision: number;
  formLock: number;
  inputSha256: string;
  bindingSha256: string;
};
const recalculatePreview = ref<RecalculatePreview | null>(null);
const recalculateMessage = ref("");
const successorInvocation = ref("");
let pendingRecalculate: any = null;
const clearRecalculate = () => {
  recalculatePreview.value = null;
  recalculateMessage.value = "";
  successorInvocation.value = "";
  pendingRecalculate = null;
};
let pendingRetry: any = null;
const clearRetry = () => {
  retryPreview.value = null;
  retryMessage.value = "";
  pendingRetry = null;
};
const executionLabel = (status?: string) =>
  ({
    READY: "等待執行",
    RUNNING: "執行中",
    FAILED: "失敗",
    SUCCEEDED: "已成功",
    CANCELLED: "已取消",
    RETRY_ACCEPTED: "重試已受理，待查詢",
  })[status || "FAILED"] || status;
const targetStatus = ref("IGNORED");
let pendingCommand: any = null;
const statusLabel = (status: string) =>
  ({ OPEN: "待處理", RESOLVED: "已處理", IGNORED: "已忽略" })[status] || status;
let generation = 0;
const reset = () => {
  clearRetry();
  clearRecalculate();
  generation++;
  items.value = [];
  selected.value = null;
  offset.value = 0;
  hasMore.value = false;
  loaded.value = false;
  busy.value = false;
  message.value = "";
  handling.value = null;
  handlingMessage.value = "";
  reason.value = "";
  pendingCommand = null;
};
watch(() => props.tenantId, reset, { flush: "sync" });
onBeforeUnmount(() => generation++);

async function request(pageOffset: number, invocationId?: string) {
  if (!props.tenantId || busy.value) return;
  clearRetry();
  clearRecalculate();
  const requestGeneration = ++generation;
  const tenant = props.tenantId;
  busy.value = true;
  message.value = "";
  selected.value = null;
  handling.value = null;
  handlingMessage.value = "";
  reason.value = "";
  pendingCommand = null;
  try {
    const response: any = await useApi(
      `/fm/operations/system-tasks/${invocationId ? "detail" : "incidents"}`,
      {
        method: "POST",
        headers: { "X-FlowMint-Tenant": tenant },
        body: invocationId ? { invocationId } : { offset: pageOffset },
      },
    );
    if (requestGeneration !== generation || tenant !== props.tenantId) return;
    if (response?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      message.value = "無法查詢，請確認 Groovy 維運權限及服務是否已啟用。";
      items.value = [];
      hasMore.value = false;
      loaded.value = false;
      return;
    }
    if (invocationId) {
      selected.value = response.value;
      await loadHandling(0, requestGeneration);
    } else {
      items.value = response.value.items;
      hasMore.value = response.value.hasMore;
      offset.value = pageOffset;
      loaded.value = true;
    }
  } catch {
    if (requestGeneration === generation) {
      message.value = "查詢失敗，請稍後重試。";
    }
  } finally {
    if (requestGeneration === generation) busy.value = false;
  }
}

async function loadHandling(pageOffset = 0, expectedGeneration = generation) {
  const invocationId = selected.value?.invocationId;
  const tenant = props.tenantId;
  if (!invocationId) return;
  try {
    const response: any = await useApi(
      "/fm/operations/system-tasks/handling/detail",
      {
        method: "POST",
        headers: { "X-FlowMint-Tenant": tenant },
        body: { invocationId, offset: pageOffset },
      },
    );
    if (
      expectedGeneration !== generation ||
      tenant !== props.tenantId ||
      invocationId !== selected.value?.invocationId
    )
      return;
    if (response?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      handling.value = null;
      handlingMessage.value = "處理功能尚未啟用，或目前無法讀取處理紀錄。";
      return;
    }
    handling.value = response.value;
    historyOffset.value = pageOffset;
    targetStatus.value = response.value.status === "OPEN" ? "IGNORED" : "OPEN";
    handlingMessage.value = "";
  } catch {
    if (expectedGeneration === generation) {
      handling.value = null;
      handlingMessage.value = "無法讀取處理紀錄，請重新查詢。";
    }
  }
}

async function historyPage(pageOffset: number) {
  if (busy.value) return;
  busy.value = true;
  const current = ++generation;
  try {
    await loadHandling(pageOffset, current);
  } finally {
    if (current === generation) busy.value = false;
  }
}

async function handle() {
  if (busy.value || !selected.value || !handling.value || !reason.value.trim())
    return;
  const tenant = props.tenantId;
  clearRetry();
  clearRecalculate();
  const payload = {
    invocationId: selected.value.invocationId,
    expectedRevision: handling.value.revision,
    targetStatus: targetStatus.value,
    reason: reason.value.trim(),
  };
  const key = JSON.stringify([tenant, payload]);
  if (pendingCommand?.key !== key)
    pendingCommand = {
      key,
      body: { ...payload, requestId: crypto.randomUUID() },
    };
  const current = ++generation;
  busy.value = true;
  handlingMessage.value = "";
  try {
    const response: any = await useApi(
      "/fm/operations/system-tasks/handling/handle",
      {
        method: "POST",
        headers: { "X-FlowMint-Tenant": tenant },
        body: pendingCommand.body,
      },
    );
    if (current !== generation || tenant !== props.tenantId) return;
    if (response?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      handlingMessage.value =
        "處理未完成：狀態可能已變更，或流程尚未結束而不能標記已處理。請重新查詢。";
      return;
    }
    reason.value = "";
    pendingCommand = null;
    await loadHandling(0, current);
  } catch {
    if (current === generation)
      handlingMessage.value = "操作結果尚未確認，可重試相同操作或重新查詢。";
  } finally {
    if (current === generation) busy.value = false;
  }
}
async function checkRetry() {
  if (busy.value || !selected.value || !handling.value) return;
  clearRetry();
  clearRecalculate();
  const current = ++generation;
  const tenant = props.tenantId;
  const invocationId = selected.value.invocationId;
  busy.value = true;
  try {
    const response: any = await useApi(
      "/fm/operations/system-tasks/retry/preview",
      {
        method: "POST",
        headers: { "X-FlowMint-Tenant": tenant },
        body: { invocationId },
      },
    );
    if (current !== generation || tenant !== props.tenantId) return;
    if (
      response?.success !== import.meta.env.VITE_SUCCESS_FLAG ||
      response.value?.invocationId !== invocationId
    ) {
      retryMessage.value =
        "目前不能重試。請確認功能已啟用、流程仍在執行、原輸入未變更且重試次數未用盡。";
      return;
    }
    retryPreview.value = response.value;
  } catch {
    if (current === generation)
      retryMessage.value = "無法檢查重試條件，請稍後重試。";
  } finally {
    if (current === generation) busy.value = false;
  }
}

async function retry() {
  if (
    busy.value ||
    !retryPreview.value ||
    !selected.value ||
    !reason.value.trim()
  )
    return;
  const tenant = props.tenantId;
  const payload = {
    invocationId: selected.value.invocationId,
    expectedRevision: retryPreview.value.revision,
    expectedGeneration: retryPreview.value.generation,
    reason: reason.value.trim(),
  };
  const key = JSON.stringify([tenant, payload]);
  if (pendingRetry?.key !== key)
    pendingRetry = {
      key,
      body: { ...payload, requestId: crypto.randomUUID() },
    };
  const current = ++generation;
  busy.value = true;
  retryMessage.value = "";
  try {
    const response: any = await useApi("/fm/operations/system-tasks/retry", {
      method: "POST",
      headers: { "X-FlowMint-Tenant": tenant },
      body: pendingRetry.body,
    });
    if (current !== generation || tenant !== props.tenantId) return;
    if (response?.success !== import.meta.env.VITE_SUCCESS_FLAG) {
      retryMessage.value = "重試未受理，資料或狀態可能已變更。請重新查詢。";
      retryPreview.value = null;
      pendingRetry = null;
      return;
    }
    clearRetry();
    reason.value = "";
    retryMessage.value = "重試已受理；請重新查詢實際執行結果。";
    selected.value = { ...selected.value, executionStatus: "RETRY_ACCEPTED" };
    await loadHandling(0, current);
  } catch {
    if (current === generation)
      retryMessage.value = "重試結果尚未確認，可重送相同操作或重新查詢。";
  } finally {
    if (current === generation) busy.value = false;
  }
}
async function checkRecalculate() {
  if (busy.value || !selected.value || !handling.value) return;
  clearRetry();
  clearRecalculate();
  const current = ++generation;
  const tenant = props.tenantId;
  const invocationId = selected.value.invocationId;
  busy.value = true;
  try {
    const response: any = await useApi(
      "/fm/operations/system-tasks/recalculate/preview",
      {
        method: "POST",
        headers: { "X-FlowMint-Tenant": tenant },
        body: { invocationId },
      },
    );
    if (current !== generation || tenant !== props.tenantId) return;
    if (
      response?.success !== import.meta.env.VITE_SUCCESS_FLAG ||
      response.value?.invocationId !== invocationId
    ) {
      recalculateMessage.value =
        "目前不能重算。請確認已有較新的表單修訂、流程仍在執行且功能已啟用。";
      return;
    }
    recalculatePreview.value = response.value;
  } catch {
    if (current === generation)
      recalculateMessage.value = "無法檢查最新表單，請稍後重試。";
  } finally {
    if (current === generation) busy.value = false;
  }
}

async function recalculate() {
  if (
    busy.value ||
    !recalculatePreview.value ||
    !selected.value ||
    !reason.value.trim()
  )
    return;
  const tenant = props.tenantId;
  const preview = recalculatePreview.value;
  const payload = {
    invocationId: selected.value.invocationId,
    expectedRevision: preview.revision,
    expectedGeneration: preview.generation,
    formRevision: preview.formRevision,
    formLock: preview.formLock,
    inputSha256: preview.inputSha256,
    bindingSha256: preview.bindingSha256,
    reason: reason.value.trim(),
  };
  const key = JSON.stringify([tenant, payload]);
  if (pendingRecalculate?.key !== key)
    pendingRecalculate = {
      key,
      body: { ...payload, requestId: crypto.randomUUID() },
    };
  const current = ++generation;
  busy.value = true;
  recalculateMessage.value = "";
  try {
    const response: any = await useApi(
      "/fm/operations/system-tasks/recalculate",
      {
        method: "POST",
        headers: { "X-FlowMint-Tenant": tenant },
        body: pendingRecalculate.body,
      },
    );
    if (current !== generation || tenant !== props.tenantId) return;
    if (
      response?.success !== import.meta.env.VITE_SUCCESS_FLAG ||
      typeof response.value !== "string"
    ) {
      recalculatePreview.value = null;
      pendingRecalculate = null;
      recalculateMessage.value =
        "重算未受理，資料或狀態可能已變更。請重新檢查最新表單。";
      return;
    }
    clearRecalculate();
    reason.value = "";
    successorInvocation.value = response.value;
    recalculateMessage.value =
      "重算已受理；原紀錄保留並標記已忽略，可查看新工作的執行結果。";
    await loadHandling(0, current);
  } catch {
    if (current === generation)
      recalculateMessage.value = "重算結果尚未確認，可重送相同操作或重新查詢。";
  } finally {
    if (current === generation) busy.value = false;
  }
}
</script>

<template>
  <section class="card border-0 shadow-sm mt-4" aria-label="Groovy 執行異常">
    <div class="card-body">
      <div class="d-flex justify-content-between align-items-center gap-3">
        <h3 class="h5 mb-0">Groovy 執行異常</h3>
        <button
          class="btn btn-outline-primary"
          :disabled="busy || !tenantId"
          @click="request(0)"
        >
          {{ busy ? "查詢中…" : "查詢執行異常" }}
        </button>
      </div>
      <p class="text-muted mt-2">
        查看腳本工作失敗紀錄及後續重算；已終止流程的紀錄仍會保留。
      </p>
      <p v-if="message" role="alert" class="text-danger">{{ message }}</p>
      <p v-if="loaded && !items.length" class="text-muted">
        目前沒有失敗紀錄。
      </p>
      <div v-if="items.length" class="table-responsive">
        <table class="table align-middle">
          <thead>
            <tr>
              <th>流程實例</th>
              <th>節點</th>
              <th>原因</th>
              <th>目前執行狀態</th>
              <th>明細</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in items" :key="item.invocationId">
              <td>{{ item.processInstanceId }}</td>
              <td>{{ item.nodeId }}</td>
              <td>{{ item.failureCategory }}</td>
              <td>{{ executionLabel(item.executionStatus) }}</td>
              <td>
                <button
                  class="btn btn-sm btn-outline-secondary"
                  :disabled="busy"
                  @click="request(offset, item.invocationId)"
                >
                  查看
                </button>
              </td>
            </tr>
          </tbody>
        </table>
        <div class="d-flex gap-2">
          <button
            class="btn btn-sm btn-outline-secondary"
            :disabled="busy || offset === 0"
            @click="request(offset - 50)"
          >
            上一頁
          </button>
          <button
            class="btn btn-sm btn-outline-secondary"
            :disabled="busy || !hasMore"
            @click="request(offset + 50)"
          >
            下一頁
          </button>
        </div>
      </div>
      <dl v-if="selected" class="mt-3 mb-0">
        <dt>紀錄編號</dt>
        <dd>{{ selected.incidentId }}</dd>
        <dt>流程版本</dt>
        <dd>{{ selected.processDefId }} / {{ selected.versionNo }}</dd>
        <dt>執行次數</dt>
        <dd>{{ selected.attemptNo }}</dd>
        <dt>原因</dt>
        <dd>{{ selected.failureCategory }}</dd>
        <dt>目前執行狀態</dt>
        <dd>{{ executionLabel(selected.executionStatus) }}</dd>
        <template v-if="selected.parentInvocationId">
          <dt>原失敗工作</dt>
          <dd>
            <button
              class="btn btn-link p-0"
              :disabled="busy"
              @click="request(offset, selected.parentInvocationId)"
            >
              {{ selected.parentInvocationId }}
            </button>
          </dd>
        </template>
      </dl>
      <div v-if="selected" class="mt-3">
        <p v-if="handlingMessage" role="status">{{ handlingMessage }}</p>
        <template v-if="handling">
          <h4 class="h6">處理狀態：{{ statusLabel(handling.status) }}</h4>
          <p class="text-muted">
            標記不會重試腳本。「已處理」適用於已成功的工作或已結束的流程。
          </p>
          <label class="form-label" for="groovy-incident-status"
            >處理方式</label
          >
          <select
            id="groovy-incident-status"
            v-model="targetStatus"
            class="form-select"
            :disabled="busy"
          >
            <template v-if="handling.status === 'OPEN'">
              <option value="IGNORED">標記已忽略</option>
              <option value="RESOLVED">標記已處理</option>
            </template>
            <option v-else value="OPEN">重新開啟</option>
          </select>
          <label class="form-label mt-2" for="groovy-incident-reason"
            >處理理由（必填）</label
          >
          <textarea
            id="groovy-incident-reason"
            v-model="reason"
            maxlength="1000"
            rows="3"
            class="form-control"
            :disabled="busy"
          />
          <button
            class="btn btn-primary mt-2"
            :disabled="
              busy ||
              !reason.trim() ||
              ['READY', 'RUNNING', 'RETRY_ACCEPTED'].includes(
                selected.executionStatus || '',
              )
            "
            @click="handle"
          >
            儲存處理紀錄
          </button>
          <div class="mt-3">
            <button
              class="btn btn-outline-warning"
              :disabled="
                busy ||
                handling.status !== 'OPEN' ||
                (selected.executionStatus &&
                  selected.executionStatus !== 'FAILED')
              "
              @click="checkRetry"
            >
              檢查原輸入重試條件
            </button>
            <p v-if="retryMessage" class="mt-2" role="status">
              {{ retryMessage }}
            </p>
            <div v-if="retryPreview" class="mt-2">
              <p>
                沿用表單修訂
                {{ retryPreview.inputRevision }}，不套用最新表單變更。剩餘
                {{ retryPreview.remainingAttempts }} 次嘗試。
              </p>
              <p class="text-break">
                腳本版本：{{ retryPreview.bindingSha256 }}
              </p>
              <p>將重新執行腳本，成功後流程會繼續。請填寫上方理由後確認。</p>
              <button
                class="btn btn-warning"
                :disabled="busy || !reason.trim()"
                @click="retry"
              >
                確認沿用原輸入重試
              </button>
            </div>
          </div>
          <div class="mt-3">
            <button
              class="btn btn-outline-warning"
              :disabled="
                busy ||
                handling.status !== 'OPEN' ||
                (selected.executionStatus &&
                  selected.executionStatus !== 'FAILED')
              "
              @click="checkRecalculate"
            >
              檢查最新表單重算條件
            </button>
            <p v-if="recalculateMessage" class="mt-2" role="status">
              {{ recalculateMessage }}
            </p>
            <button
              v-if="successorInvocation"
              class="btn btn-outline-secondary"
              :disabled="busy"
              @click="request(offset, successorInvocation)"
            >
              查看重算工作
            </button>
            <div v-if="recalculatePreview" class="mt-2">
              <p>
                表單修訂：原 {{ recalculatePreview.previousFormRevision }} →
                最新 {{ recalculatePreview.formRevision }}。
              </p>
              <p class="text-break">
                沿用腳本版本：{{ recalculatePreview.bindingSha256 }}
              </p>
              <p>
                確認後將以此修訂建立新工作，保留原失敗紀錄並標記已忽略。若表單再次變更，必須重新檢查。請填寫上方理由後確認。
              </p>
              <button
                class="btn btn-warning"
                :disabled="busy || !reason.trim()"
                @click="recalculate"
              >
                確認依此表單修訂重算
              </button>
            </div>
          </div>
          <h4 class="h6 mt-4">操作歷程</h4>
          <p v-if="!handling.history.length" class="text-muted">
            尚無人工處理紀錄。
          </p>
          <ol>
            <li
              v-for="action in handling.history"
              :key="action.revision"
              class="mb-2"
            >
              <div>{{ action.actor }} · {{ action.date }}</div>
              <div v-if="action.actionType === 'RETRY'">申請沿用原輸入重試</div>
              <div v-else-if="action.actionType === 'RECALCULATE'">
                依最新表單重算，原紀錄已忽略。
                <button
                  v-if="action.targetInvocationId"
                  class="btn btn-link p-0"
                  :disabled="busy"
                  @click="request(offset, action.targetInvocationId)"
                >
                  查看重算工作
                </button>
              </div>
              <div v-else>
                {{ statusLabel(action.fromStatus) }} →
                {{ statusLabel(action.toStatus) }}
              </div>
              <div class="text-break" style="white-space: pre-wrap">
                {{ action.reason }}
              </div>
            </li>
          </ol>
          <div class="d-flex gap-2">
            <button
              class="btn btn-sm btn-outline-secondary"
              :disabled="busy || historyOffset === 0"
              @click="historyPage(historyOffset - 50)"
            >
              較新紀錄
            </button>
            <button
              class="btn btn-sm btn-outline-secondary"
              :disabled="busy || !handling.hasMore"
              @click="historyPage(historyOffset + 50)"
            >
              較舊紀錄
            </button>
          </div>
        </template>
      </div>
    </div>
  </section>
</template>
