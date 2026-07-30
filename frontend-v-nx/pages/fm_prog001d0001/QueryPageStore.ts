import { defineStore } from "pinia";
import {
  getInitGridConfigVariable,
  type GridConfig,
} from "@/components/GridHelper";

export const useFmProg001d0001Store = defineStore("fm_prog001d0001", {
  state: () => ({
    gridConfig: getInitGridConfigVariable() as GridConfig,
    queryParam: { tenantCode: "", tenantName: "", status: "" },
  }),
});
