export interface BpmnGuideSection {
  id: string;
  title: string;
  intro: string;
  steps?: string[];
  rows?: Array<[string, string]>;
  notes?: string[];
  examples?: Array<{ title: string; code: string }>;
}

export const bpmnDesignerGuide: BpmnGuideSection[] = [
  {
    id: "quick-start",
    title: "從零建立流程",
    intro:
      "先準備表單與簽核人，再畫流程、配置每個關卡並發布。圖上的方塊與連線決定路徑，右側面板決定表單、操作權限與實際簽核人。",
    steps: [
      "先在同一 Tenant 建立並發布 Form；確認員工帳號、任職、部門主管、簽核群組與層級資料有效。需要自動執行節點時，先發布 Data Action。",
      "新增流程：選擇 Tenant，填寫流程代碼、名稱、分類、分類內排序與說明，按「儲存」後進入編輯頁。流程代碼建立後不可修改。",
      "在「BPMN 流程版本」建立或開啟 DRAFT。使用左側工具拖入節點，選取節點後用連線工具接到下一個節點；可雙擊圖上標籤修改名稱。",
      "一般 Task 請使用節點旁的替換工具改成 User Task；自動執行請直接使用專用 Data Action Task 工具。逐一選取 User Task，設定已發布表單、欄位權限、派送方式、簽核政策與解析方式。",
      "需要分流時加入 Gateway，點選其出線，在右側設定條件或 Default Flow，再按「套用流程條件」。",
      "在「流程啟動規則」加入允許起單對象，儲存草稿，再用「簽核人解析預覽」輸入測試申請人及測試表單資料。",
      "確認每條分支、補件回路及結束點後，按「發布草稿」。發布成功後，到 Workspace 使用真實帳號驗證起單與各關簽核。",
    ],
    notes: [
      "本指南說明 FlowMint 現行支援範圍。工具列可畫出的標準 BPMN 元件，不一定能在 FlowMint 發布。",
      "說明視窗可在草稿與已發布版本開啟；閱讀說明不會儲存或修改流程。",
    ],
  },
  {
    id: "events",
    title: "Start Event、End Event 與連線",
    intro:
      "Start Event 表示起點，End Event 表示路徑結束，Sequence Flow 箭頭表示執行方向。先以「開始 → 主管審核 → 結束」建立最小流程。",
    rows: [
      [
        "Start Event（細圓圈）",
        "使用一般開始事件，連到第一個關卡。起單者資格由下方的流程啟動規則設定，不在圓圈上設定帳號。",
      ],
      [
        "End Event（粗圓圈）",
        "使用一般結束事件。平行流程中，一條支線到達 End 不代表其他支線也完成；需要一起完成再續行時，先設正確的匯合 Gateway。",
      ],
      [
        "Sequence Flow（箭頭）",
        "從來源連至目標，確認方向、名稱與端點。條件面板只支援 Exclusive／Inclusive Gateway 的出線。",
      ],
      [
        "節點代碼與名稱",
        "代碼用於關卡綁定，名稱供人員辨識。User Task 右側代碼／名稱為唯讀，名稱可在圖上編輯。刪除重建節點後，重新核對表單、政策及簽核人配置。",
      ],
    ],
    notes: [
      "確認沒有孤立節點、接反箭頭或無法到達的分支。補件節點可以在退回時才進入，但重送後仍需有明確的續行路徑。",
    ],
  },
  {
    id: "user-task",
    title: "User Task：表單與欄位權限",
    intro:
      "User Task 是需要人員處理的關卡。每個關卡都要選取同 Tenant 的已發布表單固定版本；改選會取代原綁定，並重設欄位權限，請重新確認。",
    rows: [
      [
        "顯示表單",
        "決定此關待辦顯示哪個已發布 Form 版本。沒有選項時，先到表單設計器發布表單，再重新載入流程。",
      ],
      [
        "READ",
        "顯示但不可修改。一般審核關卡可把 default 設為 READ，只開放需要填寫的欄位。",
      ],
      [
        "EDIT",
        "允許修改。核准與補件重送會提交表單並執行驗證；表單原有的必填與型別要求仍需通過。",
      ],
      [
        "HIDDEN／NONE",
        "目前畫面都會隱藏並停用欄位；不可把隱藏當成機密資料已從回應中移除，也不可藉此更改受保護欄位。",
      ],
      [
        "default／fields",
        "default 控制未逐一指定的欄位；fields 使用元件 key，不是中文標籤。Grid 子欄位使用 items[].acceptedQty 這類路徑。",
      ],
    ],
    examples: [
      {
        title:
          "欄位權限 JSON：只開放審核備註與品項驗收數量（請替換成實際 key）",
        code: JSON.stringify(
          {
            default: "READ",
            fields: {
              reviewNote: "EDIT",
              "items[].acceptedQty": "EDIT",
              internalMemo: "HIDDEN",
            },
          },
          null,
          2,
        ),
      },
    ],
    notes: [
      "駁回／退回不執行完整表單送出驗證，也不保存畫面中尚未送出的修改；操作意見與退回理由仍依後端規則檢查。",
    ],
  },
  {
    id: "assignment",
    title: "派送方式：單人、候選、會簽與補件",
    intro:
      "先決定如何找人，再選擇這批人如何處理同一關卡。正式全員會簽與執行中臨時加簽有不同用途。",
    rows: [
      [
        "ASSIGNEE／單一簽核人",
        "由解析結果中的第一位人員處理。請搭配可明確解析一人的規則；需要每人都簽時應改用 ALL 或 SEQUENTIAL。",
      ],
      [
        "CANDIDATE／候選人承接",
        "候選人共用一筆待辦，由其中一人承接處理；不是每個人都要簽。適合財務窗口等擇一處理情境。",
      ],
      [
        "ALL／全員會簽",
        "依解析結果同時建立各人的待辦，全員核准才離開此關。適合固定的一組人都必須簽核。",
      ],
      [
        "SEQUENTIAL／依序簽核",
        "依解析結果順序逐一建立待辦，前一位完成才到下一位。逐級主管或 UP_TO_LEVEL 通常搭配此模式。",
      ],
      [
        "APPLICANT_CORRECTION／申請人補件",
        "直接指派表單申請人，不需設定一般簽核人 Resolver。需設定可編輯欄位並安排重送後的出線。",
      ],
      [
        "平行 Gateway 與 ALL 的差別",
        "不同部門各有不同關卡、欄位權限或政策時，用平行分支；同一關卡需要同一批人全員簽核時，用 ALL。",
      ],
    ],
  },
  {
    id: "resolvers",
    title: "簽核人解析方式（Resolver）",
    intro:
      "Resolver 把業務規則轉成有效登入帳號。畫面上的「申請人」是表單申請人；代起單者不會因此變成主管鏈的起點。帳號需符合 Tenant membership、員工與有效期間檢查。",
    rows: [
      [
        "DIRECT_MANAGER／直屬主管",
        "沿申請人的任職與主管設定找直屬主管。先確認有效任職與主管來源。",
      ],
      [
        "INITIATOR_ORG_HEAD／所屬單位主管",
        "找申請人所屬單位的有效主管；與個別指定的直屬主管可能不同。",
      ],
      [
        "PARENT_ORG_HEAD／上一層單位主管",
        "依部門樹找上一層單位主管；不要把它當成第二位直屬主管。",
      ],
      [
        "NEXT_HIGHER_LEVEL_HEAD／下一個較高層級主管",
        "尋找符合較高簽核層級條件的主管；必須先維護層級及主管資料。",
      ],
      ["ROOT_ORG_HEAD／最高層單位主管", "沿部門樹找最高層單位主管。"],
      [
        "MANAGER_CHAIN／逐級直屬主管",
        "沿直屬主管鏈取得簽核人，通常搭配 SEQUENTIAL。",
      ],
      [
        "LEVEL_HEAD_CHAIN／逐級單位主管",
        "沿組織層級取得單位主管；與直屬主管鏈的結果可能不同。",
      ],
      [
        "FIXED_ACCOUNT／指定帳號",
        "從帳號清單選取；Ctrl 可多選。多人時依需求搭配 CANDIDATE、ALL 或 SEQUENTIAL。",
      ],
      [
        "APPROVAL_GROUP／簽核群組",
        "選取已維護的 FlowMint 簽核群組，解析有效成員。QIFU4 的程式 Role 不是簽核群組。",
      ],
      [
        "TARGET_LEVEL_HEAD／指定層級主管",
        "選取目標簽核層級及匹配模式，詳見下一節。",
      ],
      [
        "ORG_TITLE／組織職稱",
        "選取職稱，解析申請人主要部門具有該職稱的有效任職者。",
      ],
      [
        "ORG_DUTY／組織職務",
        "選取組織職務，依有效職務配置找人；無選項時先維護職務。",
      ],
      [
        "APPROVAL_AUTHORITY／簽核權限",
        "選取或新增核決權限，以金額、分類等表單條件決定簽核目標。",
      ],
      [
        "FORM_ACCOUNT_FIELD／表單選擇簽核人",
        "選取人員帳號欄位，讀取單一帳號、帳號陣列或受支援的 {value,label} 值。不要填姓名或員工編號；Runtime 仍會驗證帳號資格。",
      ],
      [
        "FORM_ACCOUNT_FIELD_MANAGER／表單人員的直屬主管",
        "選取存放人員帳號的欄位，改找該人員的直屬主管。",
      ],
      [
        "最多結果數／狀態",
        "結果上限為 1～1000，依業務所需設定並保持規則啟用；結果上限不是投票比例。",
      ],
    ],
    notes: [
      "預覽為空時，先檢查測試申請人、任職、主管、群組有效期、層級及表單測試值，再檢查自簽／重複政策。不要用其他 Tenant 的帳號補足結果。",
    ],
  },
  {
    id: "authority",
    title: "層級匹配與核決權限",
    intro:
      "固定簽到某個層級可使用 TARGET_LEVEL_HEAD；依金額或其他條件選擇核決對象，可使用 APPROVAL_AUTHORITY。",
    rows: [
      ["EXACT／精確層級", "要求命中指定層級，不把其他層級自動視為相同。"],
      ["EXACT_OR_HIGHER", "尋找指定層級；不足時可採第一位更高層級主管。"],
      [
        "UP_TO_LEVEL",
        "沿主管鏈逐級取得人員，直到指定或更高層級；通常搭配 SEQUENTIAL。層級高低依方案設定，不依 L1、L5 字面數字推斷。",
      ],
      [
        "核決權限主檔",
        "維護權限代碼、名稱、啟用狀態、生效／失效日及適用表單。先綁定表單才能以其欄位配置條件。",
      ],
      [
        "核決規則",
        "逐筆設定條件、簽核目標類型與目標。目標可為簽核層級、職稱、職務、簽核群組或指定帳號。",
      ],
      [
        "符合後停止",
        "用於控制核決規則匹配後是否停止後續規則處理；不是立即結束 BPMN 流程。儲存後以邊界金額預覽實際結果。",
      ],
    ],
    notes: [
      "例如以 100,000 為門檻，至少測試 99,999、100,000、100,001；避免條件重疊、漏掉等於邊界，或没有任何規則符合。",
    ],
  },
  {
    id: "policies",
    title: "自簽、重複簽核、操作與期限",
    intro:
      "Task Policy 決定本關允許的行為。勾選操作只代表此關允許，實際執行仍需符合當前任務狀態與操作者資格。",
    rows: [
      ["自簽 ALLOW", "允許申請人簽自己的申請。"],
      [
        "自簽 SKIP_TO_NEXT",
        "從解析結果排除申請人本人，使用剩餘合格人員；不會自動新增未解析出的上級主管，也不會直接核准。排除後無人會產生指派異常。",
      ],
      [
        "自簽 REQUIRE_ALTERNATE／INCIDENT",
        "前者要求找到替代人，後者遇自簽建立指派異常；無合格人員時需處理異常。",
      ],
      ["KEEP_EACH_LEVEL", "每層保留簽核，不因同一人重複出現就視為完成。"],
      [
        "MERGE_CONSECUTIVE",
        "依最近一次核准者排除連續重複簽核。若排除後無人，需處理指派異常，不能假設節點會自動通過。",
      ],
      [
        "SKIP_ALREADY_APPROVED",
        "依已核准紀錄略過符合條件的人員；重送及不同路徑情境仍應實測。",
      ],
      [
        "意見必填",
        "NEVER 不額外要求一般意見；ALWAYS 每次要求；ON_REJECT_RETURN 駁回／退回時要求。各操作的必要理由仍依其專用規則驗證。",
      ],
      [
        "允許駁回／退回",
        "駁回結束申請；退回送到同一發布版本的申請人補件節點。請先完成補件配置。",
      ],
      [
        "允許轉派",
        "把目前待辦交給其他合格人員，不推進流程。管理員改派另有專用權限。",
      ],
      [
        "允許循序加簽",
        "允許執行中加請人員處理，與設計時固定的 SEQUENTIAL 會簽不同。",
      ],
      [
        "允許平行加簽",
        "原簽核人臨時徵詢多人意見，全部回覆後仍由原簽核人核決。一人不同意不會自動駁回整張單。單批上限可設 1～20 人。",
      ],
      [
        "處理期限／提前提醒",
        "期限為 Task 建立後的曆時小時，範圍 1～8760；不是工作日。提前提醒須小於期限，未設期限不執行提醒。逾時不表示自動核准。",
      ],
    ],
  },
  {
    id: "gateways",
    title: "Gateway：排他、包含與平行",
    intro:
      "Gateway 決定分流或匯合，不負責指定簽核人。先選擇需要「擇一」、「符合者都走」還是「全部都走」。",
    rows: [
      [
        "Exclusive Gateway／排他（X）",
        "分流只走一條符合條件的路，適合金額級距或是／否選擇。請把條件設計成互斥，避免靠線條順序決定業務結果。排他匯合不等待其他分支。",
      ],
      [
        "Inclusive Gateway／包含（○）",
        "分流啟動所有符合條件的路，適合可能同時需要法務與財務審查。對應匯合等待本次實際啟動的路徑。",
      ],
      [
        "Parallel Gateway／平行（＋）",
        "分流啟動所有出線，不設定條件；匯合等待所有必要入線。適合固定同時進行的多部門作業。",
      ],
      [
        "Default Flow／預設路徑",
        "Exclusive／Inclusive 有多條出線時，FlowMint 要求一條 Default Flow。只有其他條件都不成立才走預設；預設線不可同時帶条件。",
      ],
      [
        "分流與匯合配對",
        "不要把 Exclusive 擇一分支接到要求全部入線的 Parallel 匯合，否則可能一直等待未啟動的分支。",
      ],
    ],
    examples: [
      {
        title: "擇一分流：金額加簽",
        code: "開始 → 主管審核 → 排他分流\n  金額 >= 100000 → 財務審核 → 結束\n  Default Flow → 結束",
      },
      {
        title: "固定兩部門審查後續行",
        code: "開始 → 平行分流\n  → 財務審核 → 平行匯合\n  → 法務審核 → 平行匯合\n平行匯合 → 最終核決 → 結束",
      },
    ],
  },
  {
    id: "conditions",
    title: "Sequence Flow：設定分流條件",
    intro:
      "條件設定在 Gateway 的出線，不是在 User Task 上撰寫 method。請使用右側結構化條件編輯器產生受控表達式。",
    steps: [
      "先替流程的 User Task 綁定已發布表單，讓条件面板取得可用欄位。不同表單綁定時，只提供共同可用欄位。",
      "點選 Exclusive／Inclusive Gateway 的出線，輸入線條顯示名稱；空白時會依條件產生標籤。",
      "選擇「全部符合（AND）」或「任一符合（OR）」，新增欄位、比較方式及比較值。數字可比較大小；文字／選項／布林以等於或不等於比較。",
      "按「套用流程條件」，再儲存草稿。只編輯條件輸入框而未套用，不會更新圖上的條件。",
      "選取另一條出線，勾選「設為 Default Flow」，按套用；同一 Gateway 只能有一條預設線，重新指定會取代原設定。",
    ],
    examples: [
      {
        title: "由編輯器產生的條件格式（欄位必須存在於實際表單）",
        code: '${flowmintFormData.totalAmountTwd >= 100000}\n${flowmintFormData.category == "IT" && flowmintFormData.totalAmountTwd > 50000}\n${flowmintFormData.requiresLegal == true}',
      },
    ],
    notes: [
      "目前使用單一 AND 或 OR 組合；不要混合兩者或填入函式、任意 JavaScript、JUEL method、括號運算及未支援的欄位路徑。文字值使用雙引號。",
      "Grid 明細不可直接逐列寫 Gateway 條件。先在表單準備彙總金額或布林欄位，再用該欄位分流。",
      "若欄位清單為空，先確認已發布表單綁定、欄位 key 與共同可用欄位；看到「既有條件不是受支援的結構化格式」時，重新配置受支援條件再套用。",
    ],
  },
  {
    id: "correction",
    title: "退回補件與重新送出",
    intro:
      "退回不是在圖上任意跳回前一位簽核者。FlowMint 使用同一發布版本中專用的申請人補件 User Task。",
    steps: [
      "建立名為「申請人補件」的 User Task，把派送方式設為 APPLICANT_CORRECTION。",
      "綁定已發布表單，將允許補正的欄位設為 EDIT，其餘設為 READ 或其他適當權限。",
      "把補件節點的出線接到重新審核的關卡。正常起單主線通常不需先經補件節點，退回時才由 Runtime 進入。",
      "在可退回的審核關卡勾選「允許退回」。執行時選擇合法補件目標並填寫必要理由。",
      "申請人在補件待辦修改資料並重送；系統重新驗證表單、保存修訂與快照，完成補件節點後沿出線繼續。",
    ],
    examples: [
      {
        title: "補件回路",
        code: "正常：開始 → 主管審核 → 結束\n退回操作：主管審核 ⇢ 申請人補件\n重送出線：申請人補件 → 主管審核\n（⇢ 表示執行時退回操作，不是條件連線。）",
      },
    ],
    notes: [
      "第一次退回時補件節點可能尚未執行，這是合法情境。請另測駁回、連續退回、修改金額後重新分流及多人會簽中的退回行為。",
    ],
  },
  {
    id: "service-task",
    title: "Service Task／Data Action Task",
    intro:
      "FlowMint 的 System Task 目前只有 DATA_ACTION 類型，用來執行已發布的受控資料服務。它自動執行，不會產生供使用者簽核的待辦。",
    steps: [
      "先在 Data Action 管理完成並發布所需 QUERY、COMMAND 或 TRANSACTION，確認 Request Schema 與回傳欄位。",
      "從 BPMN 專用 Palette 建立 Data Action Task，連接前後節點，再點選節點開啟屬性面板。不要使用未配置的普通 Service Task。",
      "設定節點名稱並選擇同 Tenant 的已發布 Data Action。固定版本由選取結果帶入並唯讀顯示；新發布的 Action 不會自動取代既有流程綁定。",
      "依面板 Request keys／Response keys 填寫兩份 Mapping JSON。切換 Action 後重新核對固定版本、參數名稱、型別及舊 Mapping。",
      "儲存並發布草稿。發布會檢查 Action、固定版本、必要參數、回傳欄位與表單路徑；請用實際資料驗證自動執行後表單及 Gateway 的結果。",
    ],
    rows: [
      [
        "Request Mapping",
        "左側是 Action 的 request key，右側是來源字串。支援 FORM_DATA.path、PROCESS_CONTEXT.name、CONSTANT:value。必填參數不可漏填。",
      ],
      [
        "FORM_DATA.path",
        "讀取目前已保存的表單值。可使用物件 dot path；不支援陣列索引、萬用字元或計算式。",
      ],
      [
        "PROCESS_CONTEXT.name",
        "可用名稱只有 tenantId、processDefId、processVersionNo、initiatorAccount、businessKey、processInstanceId。initiatorAccount 是實際發起人，代起單時不一定等於申請人。",
      ],
      [
        "CONSTANT:value",
        "目前冒號後內容以字串傳入。例如 CONSTANT:TWD 得到字串 TWD；CONSTANT:100 不是數字 100，需符合 Action Request Schema。",
      ],
      [
        "Response Mapping",
        "左側是 Action 結果欄位／物件路徑，右側只能是 FORM_DATA.path。目標必須存在於綁定表單；沒有要寫回的結果可用空物件 {}。",
      ],
      [
        "執行與重試",
        "以非同步工作執行。QUERY 使用平台重試；COMMAND／TRANSACTION 不自動重試。異動失敗時由管理員先確認外部結果，再決定人工處理方式。",
      ],
    ],
    examples: [
      {
        title: "Request Mapping JSON（參數需存在於所選 Action）",
        code: JSON.stringify(
          {
            employeeId: "FORM_DATA.applicantId",
            reference: "PROCESS_CONTEXT.businessKey",
            currency: "CONSTANT:TWD",
          },
          null,
          2,
        ),
      },
      {
        title: "Response Mapping JSON（假設 Action 回傳 employee 物件）",
        code: JSON.stringify(
          { "employee.name": "FORM_DATA.employeeName" },
          null,
          2,
        ),
      },
    ],
    notes: [
      "目前為 Runtime MVP：尚未提供節點 timeout 設定、System Task 專屬執行紀錄／Incident、表單快照及完整維運能力，真實流程 E2E 仍待完成。正式異動流程需先完成環境驗證。",
      "不可在節點貼入 SQL、URL、密碼、Java class、delegate expression 或 Script。這裡也不使用表單的 ctx.executeDataAction() method。",
    ],
  },
  {
    id: "publish",
    title: "啟動規則、版本、預覽與發布",
    intro:
      "草稿儲存、簽核人解析預覽與發布是不同動作。已發布版本供執行使用，變更需要新版本。",
    rows: [
      [
        "流程主檔",
        "Tenant 決定資料範圍，流程代碼是穩定識別；分類與排序影響申請中心入口，說明供使用者理解流程用途。",
      ],
      [
        "DRAFT",
        "可修改流程圖及關卡配置。已有草稿時，先處理該草稿；畫面不再提供另一個「建立新版本」入口。",
      ],
      [
        "PUBLISHED／RETIRED",
        "版本僅供檢視；需要調整時建立新草稿，不直接修改既有發布內容。",
      ],
      [
        "流程啟動規則",
        "至少一筆允許規則。對象可為 ALL、ACCOUNT、APPROVAL_GROUP、ORG_UNIT；指定對象需選取相應資料。拒絕規則優先，ALL 仍受 Tenant 與起單資格限制。",
      ],
      [
        "簽核人解析預覽",
        "使用已儲存的規則。先儲存草稿，輸入測試申請人帳號及 JSON 表單值，再按開始預覽，核對節點、Resolver、結果與簽核人。",
      ],
      [
        "發布草稿",
        "會驗證 BPMN 允許元件、流程代碼、表單固定版本、Task Policy、Assignment、啟動規則、Gateway 及 Data Action 引用，成功才成為可執行版本。",
      ],
      [
        "SHA-256",
        "版本內容的識別摘要，供追蹤比對；不需自行填寫，也不代表真實流程驗收已完成。",
      ],
    ],
    examples: [
      {
        title: "簽核人解析預覽的測試表單資料",
        code: JSON.stringify(
          {
            totalAmountTwd: 100000,
            category: "IT",
            reviewers: ["reviewer01", "reviewer02"],
          },
          null,
          2,
        ),
      },
    ],
    notes: [
      "Preview 只協助檢查簽核人解析，不會模擬整條 Gateway 路徑或執行 Data Action。發布後仍需以真實帳號測試各路徑、補件、附件、通知及多人併發。",
    ],
  },
  {
    id: "troubleshooting",
    title: "發布檢查與常見問題",
    intro: "按錯誤所指的節點或連線回到對應面板修正，儲存後再發布。",
    rows: [
      [
        "不允許 BPMN 元件",
        "目前只支援一般 Start／End、User Task、受控 Data Action Service Task、Exclusive／Inclusive／Parallel Gateway 與 Sequence Flow。不支援普通 Task、Script／Manual／Send／Receive／Business Rule Task、Subprocess、Call Activity、Boundary／Timer／Message Event、Event-based Gateway、Pool／Lane 等配置。工具列顯示不等於可發布。",
      ],
      [
        "Process ID 與流程代碼不符",
        "BPMN 必須只有一個 Process，且其 ID 等於主檔流程代碼。避免直接混入其他流程的 XML。",
      ],
      [
        "Gateway 必須設定 Default Flow",
        "點選 Exclusive／Inclusive 的預設出線，勾選 Default Flow 並套用；其他出線設定有效條件，預設線移除條件。",
      ],
      [
        "Parallel Gateway 不可設定條件",
        "平行出線全部執行；需要條件分流時改用 Exclusive 或 Inclusive。",
      ],
      [
        "找不到表單或條件欄位",
        "先確認同 Tenant 已發布的 Form 固定版本、每個 User Task 的綁定及欄位 key。不要拿畫面標籤當欄位名稱。",
      ],
      [
        "簽核人未解析成功",
        "先儲存規則再預覽，檢查申請人帳號、任職／主管／群組有效性、自簽政策、層級與表單帳號值。",
      ],
      [
        "不能退回或補件不可編輯",
        "確認允許退回、同版本有 APPLICANT_CORRECTION 節點、表單綁定及 EDIT 欄位權限，並確認重送出線。",
      ],
      [
        "Data Action 發布失敗",
        "核對同 Tenant、啟用狀態、固定 Published 版本、Request 必填與型別、Response key 及合法 Form path。兩份 Mapping 必須是 JSON object。",
      ],
      [
        "流程停在匯合或自動節點",
        "先檢查是否還有未完成的分支／會簽；自動節點需查工作執行失敗。異動型 Action 不可因畫面未前進就重複發單。",
      ],
    ],
    notes: [
      "驗收至少涵蓋正常通過、金額邊界、預設路徑、多條包含分支、退回重送、駁回、無簽核人與 Data Action 失敗。",
    ],
  },
];
