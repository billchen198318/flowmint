import { defineStore } from "pinia";

export const useFmProg002d0002Store = defineStore("fm_prog002d0002", {
  state: () => ({
    queryParam: {
      tenantId: "",
      unitCode: "",
      unitName: "",
      status: "",
    },
    gridConfig: {} as any,
  }),
});
