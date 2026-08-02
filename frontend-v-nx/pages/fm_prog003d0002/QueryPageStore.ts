import { defineStore } from "pinia";

export const useStore = defineStore("fm_prog003d0002_query", {
  state: () => ({
    queryParam: {
      tenantId: "",
      principalAccount: "",
      delegateAccount: "",
      scopeType: "",
      status: "",
    },
    gridConfig: {} as any,
  }),
});
