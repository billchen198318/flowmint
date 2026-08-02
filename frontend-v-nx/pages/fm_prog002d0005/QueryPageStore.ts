import { defineStore } from "pinia";

export const useStore = defineStore("fm_prog002d0005", {
  state: () => ({
    queryParam: { tenantId: "", orgUnitId: "", headType: "", status: "" },
    gridConfig: {} as any,
  }),
});
