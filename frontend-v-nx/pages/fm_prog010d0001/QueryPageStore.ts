import { defineStore } from "pinia";

export const useStore = defineStore("fm_prog010d0001", {
  state: () => ({
    queryParam: { tenantId: "", status: "" },
    gridConfig: {} as any,
  }),
});
