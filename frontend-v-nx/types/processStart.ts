export interface RuntimeTenant {
  tenantId: string;
  tenantCode: string;
  tenantName: string;
  defaultTenant?: boolean;
}

export interface ProcessStartApplicant {
  account: string;
  displayName: string;
  primaryOrgUnitName?: string | null;
  self: boolean;
}

export interface ProcessStartCatalogItem {
  processDefId: string;
  processKey: string;
  processName: string;
  categoryCode: string;
  categoryLabel: string;
  categoryIcon: string;
  categorySortOrder: number;
  processSortOrder: number;
  description?: string | null;
  versionNo: number;
}

export interface ProcessStartCategory {
  code: string;
  label: string;
  icon: string;
  processes: ProcessStartCatalogItem[];
}
