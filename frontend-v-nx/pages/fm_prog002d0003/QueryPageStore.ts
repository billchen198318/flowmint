import { defineStore } from "pinia";
import {
  getInitGridConfigVariable,
  type GridConfig,
} from "@/components/GridHelper";
export const useStore = defineStore("fm_prog002d0003", {
  state: () => ({
    gridConfig: getInitGridConfigVariable() as GridConfig,
    queryParam: { tenantId: "", schemeCode: "", schemeName: "", status: "" },
  }),
});
