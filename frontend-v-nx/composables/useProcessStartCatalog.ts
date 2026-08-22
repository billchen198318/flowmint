import { ref, watch } from "vue";
import { toast } from "vue3-toastify";
import type {
  ProcessStartApplicant,
  ProcessStartCatalogItem,
  RuntimeTenant,
} from "@/types/processStart";

export const useProcessStartCatalog = (defaultAccount: string) => {
  const tenants = ref<RuntimeTenant[]>([]);
  const tenantId = ref("");
  const applicants = ref<ProcessStartApplicant[]>([]);
  const applicantAccount = ref(defaultAccount);
  const processes = ref<ProcessStartCatalogItem[]>([]);
  const loading = ref(false);
  const errorMessage = ref("");
  let preferredApplicant = defaultAccount;
  let catalogGeneration = 0;

  const ok = (response: any) =>
    response?.success === import.meta.env.VITE_SUCCESS_FLAG;
  const runtimePost = (path: string, body: any = {}, headers: any = {}) =>
    useApi(`/fm/requests${path}`, { method: "POST", body, headers });
  const tenantHeaders = () => ({ "X-FlowMint-Tenant": tenantId.value });
  const fail = (response: any, fallback: string) => {
    errorMessage.value = response?.message || fallback;
    toast.warning(errorMessage.value);
  };

  const loadCatalog = async () => {
    const generation = ++catalogGeneration;
    processes.value = [];
    errorMessage.value = "";
    if (!tenantId.value || !applicantAccount.value.trim()) return;
    loading.value = true;
    try {
      const response: any = await runtimePost("/start/catalog", {
        applicantAccount: applicantAccount.value.trim(),
      }, tenantHeaders());
      if (generation !== catalogGeneration) return;
      if (!ok(response)) return fail(response, "無法載入可發起流程");
      processes.value = response.value || [];
    } catch (error) {
      if (generation === catalogGeneration) {
        errorMessage.value = error instanceof Error ? error.message : "無法載入可發起流程";
      }
    } finally {
      if (generation === catalogGeneration) loading.value = false;
    }
  };

  const loadApplicants = async () => {
    applicants.value = [];
    processes.value = [];
    errorMessage.value = "";
    if (!tenantId.value) return;
    loading.value = true;
    try {
      const response: any = await runtimePost(
        "/start/applicants", {}, tenantHeaders(),
      );
      if (!ok(response)) return fail(response, "無法載入申請人清單");
      applicants.value = response.value || [];
      const selected = applicants.value.find(
        (item) => item.account === preferredApplicant,
      ) || applicants.value.find((item) => item.self) || applicants.value[0];
      applicantAccount.value = selected?.account || "";
      preferredApplicant = applicantAccount.value;
      await loadCatalog();
    } catch (error) {
      errorMessage.value = error instanceof Error ? error.message : "無法載入申請人清單";
    } finally {
      loading.value = false;
    }
  };

  const initialize = async (
    requestedTenant: string,
    requestedApplicant: string,
  ) => {
    preferredApplicant = requestedApplicant || defaultAccount;
    loading.value = true;
    try {
      const response: any = await runtimePost("/start/tenants");
      if (!ok(response)) return fail(response, "無法載入公司清單");
      tenants.value = response.value || [];
      const preferred = tenants.value.find(
        (item) => item.tenantId === requestedTenant,
      ) || tenants.value.find((item) => item.defaultTenant);
      tenantId.value = preferred?.tenantId || tenants.value[0]?.tenantId || "";
    } catch (error) {
      errorMessage.value = error instanceof Error ? error.message : "無法載入公司清單";
    } finally {
      loading.value = false;
    }
  };

  watch(tenantId, loadApplicants);

  return {
    tenants,
    tenantId,
    applicants,
    applicantAccount,
    processes,
    loading,
    errorMessage,
    initialize,
    loadCatalog,
  };
};
