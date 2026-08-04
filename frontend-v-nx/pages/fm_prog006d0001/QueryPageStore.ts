import { defineStore } from "pinia";

export const useStore = defineStore("fm_prog006d0001", {
  state: () => ({
    queryParam: { tenantId: "" },
    gridConfig: {} as any,
  }),
});
