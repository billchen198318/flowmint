import { defineStore } from "pinia";
import {
  getInitGridConfigVariable,
  type GridConfig,
} from "@/components/GridHelper";

export const useFmProg002d0001Store = defineStore("fm_prog002d0001", {
  state: () => ({
    gridConfig: getInitGridConfigVariable() as GridConfig,
    queryParam: {
      tenantId: "",
      employeeNo: "",
      account: "",
      displayName: "",
      status: "",
    },
  }),
});
