<script setup lang="ts">
import { onBeforeUnmount, ref } from "vue";

const dialog = ref<HTMLDialogElement | null>(null);
let previousFocus: HTMLElement | null = null;
let previousOverflow: string | null = null;

const open = () => {
  if (!dialog.value || dialog.value.open) return;
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
</script>

<template>
  <button type="button" class="btn btn-sm btn-outline-info" @click="open">
    <i class="bi bi-question-circle me-1"></i>操作指南
  </button>

  <Teleport to="body">
    <dialog
      ref="dialog"
      class="data-action-guide border rounded shadow"
      aria-labelledby="data-action-guide-title"
      @close="restore"
      @cancel.prevent="close"
      @keydown.stop
    >
      <div class="guide-layout">
        <header class="guide-header border-bottom p-3">
          <div>
            <h2 id="data-action-guide-title" class="h5 mb-1">
              Data Action 操作指南
            </h2>
            <div class="small text-secondary">
              建立 SQL Action、Preview、發布及安全使用方式
            </div>
          </div>
          <button
            type="button"
            class="btn-close"
            aria-label="關閉操作指南"
            @click="close"
          ></button>
        </header>

        <div class="guide-body p-3 p-lg-4">
          <section>
            <h3 class="h6">1. 建立與發布順序</h3>
            <ol>
              <li>先建立並測試同 Tenant 的 DataSource Pool。</li>
              <li>選擇 Tenant、Pool 與 Action Type，設定穩定且唯一的 Action Code。</li>
              <li>定義 Request Mapping，再依執行順序建立一個或多個 SQL Step。</li>
              <li>儲存草稿後執行 Preview；確認結果與 Rollback 行為，再發布固定版本。</li>
              <li>已發布版本不可直接修改；後續異動會在新草稿版本完成並重新發布。</li>
            </ol>
          </section>

          <section>
            <h3 class="h6">2. Action Type</h3>
            <div class="table-responsive">
              <table class="table table-sm table-bordered align-top">
                <thead><tr><th>類型</th><th>用途與限制</th></tr></thead>
                <tbody>
                  <tr><td><code>QUERY</code></td><td>只能使用 SELECT；適合查詢選項或資料。只有暫時性資料庫錯誤可依 Step 設定重試。</td></tr>
                  <tr><td><code>COMMAND</code></td><td>執行單一步驟 INSERT／UPDATE／DELETE。異動結果不確定時不可盲目重送。</td></tr>
                  <tr><td><code>TRANSACTION</code></td><td>同一 Pool 內依序執行多個 Step；任一步失敗時整體回滾。不代表支援跨資料庫原子交易。</td></tr>
                </tbody>
              </table>
            </div>
          </section>

          <section>
            <h3 class="h6">3. Request Mapping 與 Named Parameter</h3>
            <p>
              Request Mapping 是 JSON Object。Key 是 SQL 的 Named Parameter；value
              指定呼叫 request 的 JSON path。
            </p>
            <pre><code>{
  "departmentId": "$.departmentId",
  "minimumAmount": "$.minimumAmount"
}</code></pre>
            <pre><code>SELECT ID, NAME, AMOUNT
FROM purchase_request
WHERE DEPARTMENT_ID = :departmentId
  AND AMOUNT &gt;= :minimumAmount</code></pre>
            <div class="alert alert-info mb-0">
              表單值只能作為 JDBC value parameter；不要用字串拼接資料表、欄位、排序或 SQL 片段。
            </div>
          </section>

          <section>
            <h3 class="h6">4. SQL Step 與 Result Mode</h3>
            <ul>
              <li>Execution Order 決定 Step 順序；Step Code 與 Result Key 應保持唯一且穩定。</li>
              <li><code>OBJECT</code>：單筆物件；無資料為 <code>null</code>。</li>
              <li><code>LIST</code>：物件陣列；無資料為空陣列。</li>
              <li><code>AFFECTED_ROWS</code>：回傳異動筆數。</li>
              <li><code>GENERATED_KEY</code>：INSERT 取得新增鍵值，須以實際資料庫 Driver 驗收。</li>
              <li><code>NONE</code>：不保留該 Step 結果。</li>
            </ul>
          </section>

          <section>
            <h3 class="h6">5. 多 Step 與前一步結果</h3>
            <p>後續 Step 可把前一步結果映射為 Named Parameter：</p>
            <pre><code>{
  "headerId": "${steps.createHeader.generatedKey}",
  "applicant": "$.applicantAccount"
}</code></pre>
            <p class="mb-0">
              <code>createHeader</code> 必須對應前一步的 Result Key；引用前先確認其 Result Mode 與資料形狀。
            </p>
          </section>

          <section>
            <h3 class="h6">6. FOR_EACH 批次執行</h3>
            <p>
              Execution Mode 選擇 <code>FOR_EACH</code> 後，Array Path 必須指向 request
              中的陣列；目前項目使用 <code>${item...}</code> 映射。
            </p>
            <pre><code>{
  "itemCode": "${item.itemCode}",
  "quantity": "${item.quantity}",
  "headerId": "${steps.createHeader.generatedKey}"
}</code></pre>
            <p class="mb-0">批次筆數受「最大回傳筆數」限制；正式發布前應測試空陣列、單筆、多筆與中途失敗。</p>
          </section>

          <section>
            <h3 class="h6">7. Continue Condition</h3>
            <p>留空代表一定執行；條件可讀取 request 或先前 Step 結果：</p>
            <pre><code>${steps.validation.valid} == true &amp;&amp; ${request.amount} &gt;= 1000</code></pre>
            <p class="mb-0">
              支援數值、ISO 日期時間、字串、布林、<code>null</code>、比較運算、
              <code>&amp;&amp;</code> 與 <code>||</code>；目前不支援括號，且 <code>&amp;&amp;</code> 優先於 <code>||</code>。
            </p>
          </section>

          <section>
            <h3 class="h6">8. Timeout、筆數與重試</h3>
            <ul>
              <li>Timeout 是單一 Step 的查詢／異動逾時秒數，不代表能取消資料庫端已接受的外部副作用。</li>
              <li>最大回傳筆數限制查詢結果及 FOR_EACH 規模，避免一次載入過量資料。</li>
              <li>預期異動筆數不符時視為失敗並回滾，可防止條件錯誤造成過量更新。</li>
              <li>Transient Retry 只適用 QUERY 的 SELECT 與暫時性資料庫錯誤；COMMAND／TRANSACTION 不應自動重送異動。</li>
            </ul>
          </section>

          <section>
            <h3 class="h6">9. Preview 與 Rollback</h3>
            <ol>
              <li>先儲存草稿，再輸入符合 Request Mapping 的測試 JSON。</li>
              <li>Preview 預設 <code>commit=false</code>，資料庫異動會 Rollback，但仍應使用隔離測試資料。</li>
              <li>檢查每個 Step 的結果、affected rows、generated key、耗時與錯誤。</li>
              <li>Preview 成功不等於正式 Runtime 已驗收；發布後仍須由 Form Binding 或 BPMN System Task 執行真實流程測試。</li>
            </ol>
          </section>

          <section>
            <h3 class="h6">10. 安全、版本與常見錯誤</h3>
            <ul>
              <li>資料庫密碼只保存在 DataSource Pool，不寫入 SQL、Mapping、表單 JavaScript 或說明欄。</li>
              <li>正式使用者只能引用 Published Action Version；新版本不會自動替換既有固定引用。</li>
              <li>查不到參數：核對 JSON path、Named Parameter 名稱及 Preview Request。</li>
              <li>前一步結果為空：核對 Result Key、Result Mode、Continue Condition 與執行順序。</li>
              <li>正式執行失敗：檢查 Tenant、Pool 狀態、固定版本、權限、Rate Limit、timeout 及資料庫錯誤。</li>
              <li>異動結果不確定時先查資料庫或外部系統狀態，不要只因前端沒有收到成功回應就再次執行。</li>
            </ul>
          </section>
        </div>

        <footer class="guide-footer border-top p-3">
          <span class="small text-secondary">Esc 可關閉並返回原本的建立／編輯畫面。</span>
          <button type="button" class="btn btn-secondary" @click="close">
            關閉
          </button>
        </footer>
      </div>
    </dialog>
  </Teleport>
</template>

<style scoped>
.data-action-guide {
  width: min(1050px, calc(100vw - 2rem));
  max-width: none;
  max-height: calc(100dvh - 2rem);
  padding: 0;
  color: var(--bs-body-color);
  background: var(--bs-body-bg);
}

.data-action-guide::backdrop {
  background: rgb(0 0 0 / 50%);
}

.guide-layout {
  display: flex;
  flex-direction: column;
  height: min(850px, calc(100dvh - 2rem - 2px));
}

.guide-header,
.guide-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.guide-body {
  flex: 1;
  overflow-y: auto;
  overscroll-behavior: contain;
}

.guide-body section + section {
  margin-top: 1.75rem;
}

.guide-body li + li {
  margin-top: 0.4rem;
}

pre {
  padding: 0.85rem;
  overflow: auto;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
  background: var(--bs-tertiary-bg);
  border: 1px solid var(--bs-border-color);
  border-radius: var(--bs-border-radius);
}

@media (max-width: 575px) {
  .guide-footer {
    align-items: flex-end;
  }
}
</style>
