<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from "vue";

const visible = ref(false);
let originalBodyOverflow = "";

const close = () => {
  visible.value = false;
};

const onKeydown = (event: KeyboardEvent) => {
  if (event.key === "Escape" && visible.value) close();
};

watch(visible, (value) => {
  if (value) {
    originalBodyOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";
  } else {
    document.body.style.overflow = originalBodyOverflow;
  }
});

onMounted(() => window.addEventListener("keydown", onKeydown));

onBeforeUnmount(() => {
  window.removeEventListener("keydown", onKeydown);
  document.body.style.overflow = originalBodyOverflow;
});
</script>

<template>
  <button
    type="button"
    class="btn btn-sm btn-outline-info"
    @click="visible = true"
  >
    <i class="bi bi-question-circle me-1"></i>操作指南
  </button>

  <Teleport to="body">
    <div
      v-if="visible"
      class="form-guide-modal"
      role="dialog"
      aria-modal="true"
      aria-labelledby="form-guide-title"
      tabindex="-1"
      @click.self="close"
    >
      <div class="form-guide-dialog">
        <header class="form-guide-header">
          <div>
            <h2 id="form-guide-title" class="h5 mb-1">Form Designer 操作指南</h2>
            <div class="small text-muted">建立、試跑、發布與 Runtime 使用方式</div>
          </div>
          <button type="button" class="btn-close" aria-label="關閉" @click="close"></button>
        </header>

        <div class="form-guide-body">
          <section>
            <h3 class="h6">1. 建立與版本流程</h3>
            <ol>
              <li>先建立表單主檔並儲存；表單代碼建立後不可修改。</li>
              <li>建立或選取 <code>DRAFT</code> 版本，在「設計」模式編輯 Form.io Schema。</li>
              <li>儲存草稿後切到「試跑」，輸入資料並按「驗證送出」。試跑不會建立正式單據。</li>
              <li>確認欄位、Data Action 與 JavaScript 後發布。已發布／退役版本只能唯讀預覽；修改須建立新草稿版本。</li>
            </ol>
          </section>

          <section>
            <h3 class="h6">2. 拖拉欄位與欄位代碼</h3>
            <ul>
              <li>從左側工具箱拖入元件，點選元件設定 Label、Key、Default Value、Validation 與顯示條件。</li>
              <li><strong>Key 是 API、流程條件、欄位權限、Mapping 與 JavaScript 使用的穩定識別</strong>；發布後不要任意改名或重複。</li>
              <li>Label 只供畫面顯示，不可拿 Label 當資料欄位名稱。需要改顯示文字時優先改 Label。</li>
              <li>必填、數值範圍、字數與格式應設定在元件 Validation；重要規則仍須由後端驗證。</li>
            </ul>
          </section>

          <section>
            <h3 class="h6">3. Container、Grid 與附件</h3>
            <div class="table-responsive">
              <table class="table table-sm table-bordered align-middle">
                <thead><tr><th>元件</th><th>資料形狀與注意事項</th></tr></thead>
                <tbody>
                  <tr><td>Container</td><td>產生物件；子欄位使用 <code>containerKey.childKey</code> dot path。</td></tr>
                  <tr><td>Data Grid／Edit Grid</td><td>產生物件陣列；每列必須符合 Grid 內定義的子欄位 Schema。流程條件不支援陣列索引或萬用字元。</td></tr>
                  <tr><td>File</td><td>設定 Required、Multiple、File Types、單檔大小及數量；總大小可在 Custom Properties 設定 <code>flowmintMaxTotalSize</code>，例如 <code>20MB</code>。試跑不會上傳實體附件。</td></tr>
                </tbody>
              </table>
            </div>
          </section>

          <section>
            <h3 class="h6">4. FlowMint 系統欄位</h3>
            <ul>
              <li>「加入單據編號欄位」會加入固定 Key <code>documentNumber</code>；正式送出後由後端依流程文件類型與編號規則產生。</li>
              <li>「加入申請人情境欄位」用於顯示或保存 FlowMint 提供的申請人資訊；不要另建同 Key 欄位。</li>
              <li>系統識別、Tenant、流程狀態與正式單號不可依靠前端 JavaScript 竄改。</li>
            </ul>
          </section>

          <section>
            <h3 class="h6">5. Data Action Binding</h3>
            <ol>
              <li>先在 Data Action 管理建立並發布 Action，再回表單選取同 Tenant 的固定發布版本。</li>
              <li>選擇觸發事件，將表單欄位映射為 Request；Response 只回填明確設定的目標欄位。</li>
              <li>狀態與錯誤目標欄位不可和回填目標衝突。切換 Action 或版本後，重新核對 Request／Response keys。</li>
              <li>QUERY 可用於查詢選項；異動型 Action 不應因畫面沒有更新就直接重送，須先確認前次執行結果。</li>
            </ol>
          </section>

          <section>
            <h3 class="h6">6. Custom JavaScript</h3>
            <ul>
              <li>JavaScript 適合欄位連動、計算、動態選項與送出前檢查；資料庫操作使用 Data Action，不把 SQL、密碼或 Token 寫進表單 Script。</li>
              <li>先按「檢查 JavaScript」，再到試跑驗證 <code>onFormLoad</code>、<code>onFieldChange</code> 與 <code>beforeSubmit</code>。</li>
              <li>「系統 API 說明」列出目前可用的 lifecycle、Context 與 helper；未列出的欄位不可假設存在。</li>
              <li><code>afterSubmit</code> 發生在後端交易成功之後，錯誤不會回滾已完成的送出。</li>
            </ul>
          </section>

          <section>
            <h3 class="h6">7. 試跑、發布與驗收</h3>
            <ul>
              <li>試跑只在瀏覽器驗證目前草稿，不儲存、不送出正式資料，也不代表流程中的欄位權限已驗收。</li>
              <li>發布會固定 Schema、UI Schema、Data Action Binding、Custom JavaScript 與內容 SHA-256，發布版本不可直接修改。</li>
              <li>發布前至少測試：必填與格式、Container／Grid、Data Action 成功與失敗、JavaScript lifecycle、附件限制及重設試跑。</li>
              <li>發布後須在 Workspace 以真實申請及待辦驗證起單、APPROVE／RESUBMIT、READ_ONLY、欄位權限、附件與多帳號操作。</li>
            </ul>
          </section>

          <section>
            <h3 class="h6">常見問題</h3>
            <dl class="row mb-0">
              <dt class="col-md-4">流程找不到欄位</dt><dd class="col-md-8">確認 Form 已發布、User Task 綁定正確固定版本，並使用欄位 Key 而非 Label。</dd>
              <dt class="col-md-4">試跑正常，正式送出失敗</dt><dd class="col-md-8">檢查後端驗證、欄位權限、附件及 Runtime Context；試跑不等於正式流程驗收。</dd>
              <dt class="col-md-4">Data Action 沒有觸發</dt><dd class="col-md-8">確認事件名稱、固定版本、Request Mapping、目標欄位及 Action 權限。</dd>
              <dt class="col-md-4">發布後不能修改</dt><dd class="col-md-8">這是版本不可變規則；請建立新草稿版本再調整與發布。</dd>
            </dl>
          </section>
        </div>

        <footer class="form-guide-footer">
          <button type="button" class="btn btn-secondary" @click="close">關閉</button>
        </footer>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.form-guide-modal {
  position: fixed;
  inset: 0;
  z-index: 1080;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
  background: rgb(15 23 42 / 55%);
}

.form-guide-dialog {
  display: flex;
  flex-direction: column;
  width: min(980px, 100%);
  max-height: calc(100vh - 2rem);
  overflow: hidden;
  background: #fff;
  border-radius: 0.5rem;
  box-shadow: 0 1rem 3rem rgb(0 0 0 / 25%);
}

.form-guide-header,
.form-guide-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: 1rem 1.25rem;
  border-bottom: 1px solid #dee2e6;
}

.form-guide-footer {
  justify-content: flex-end;
  border-top: 1px solid #dee2e6;
  border-bottom: 0;
}

.form-guide-body {
  overflow-y: auto;
  padding: 1.25rem;
}

.form-guide-body section + section {
  margin-top: 1.5rem;
}

.form-guide-body li + li {
  margin-top: 0.35rem;
}
</style>
