import { defineStore } from "pinia";
export const useStore = defineStore("fm_prog002d0004_query", {
  state: () => ({
    queryParam: {
      tenantId: "",
      orgUnitId: "",
      titleCode: "",
      titleName: "",
      status: "",
    },
    gridConfig: {} as any,
  }),
});
