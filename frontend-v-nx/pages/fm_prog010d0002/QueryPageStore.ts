import { defineStore } from "pinia";

export const useStore = defineStore("fm_prog010d0002", {
  state: () => ({
    queryParam: {
      tenantId: "",
      clientCodeLike: "",
      clientNameLike: "",
      systemType: "",
      status: "",
    },
    gridConfig: {} as any,
  }),
});
