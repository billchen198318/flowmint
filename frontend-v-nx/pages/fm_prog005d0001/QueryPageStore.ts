import { defineStore } from "pinia";

export const useStore = defineStore("fm_prog005d0001", {
  state: () => ({
    queryParam: { tenantId: "", formCode: "", formName: "", status: "" },
    gridConfig: {} as any,
  }),
});
