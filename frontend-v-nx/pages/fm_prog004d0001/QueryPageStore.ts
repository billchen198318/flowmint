import { defineStore } from "pinia";

export const useStore = defineStore("fm_prog004d0001", {
  state: () => ({
    queryParam: { tenantId: "", processKey: "", processName: "", status: "" },
    gridConfig: {} as any,
  }),
});
