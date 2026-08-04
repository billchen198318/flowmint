import { defineStore } from "pinia";

export const useStore = defineStore("fm_prog006d0002", {
  state: () => ({
    queryParam: {
      tenantId: "",
      actionCodeLike: "",
      status: "",
    },
    gridConfig: {} as {
      page: number;
      row: number;
      [key: string]: unknown;
    },
  }),
});
