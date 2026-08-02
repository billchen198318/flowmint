import { defineStore } from "pinia";

export const useStore = defineStore("fm_prog003d0001", {
  state: () => ({
    queryParam: {
      tenantId: "",
      groupCode: "",
      groupName: "",
      assignmentMode: "",
      status: "",
    },
    gridConfig: {} as any,
  }),
});
