import { defineStore } from "pinia";

export const useStore = defineStore("fm_prog002d0006", {
  state: () => ({
    queryParam: {
      tenantId: "",
      orgUnitId: "",
      dutyCode: "",
      dutyName: "",
      dutyType: "",
      status: "",
    },
    gridConfig: {} as any,
  }),
});
