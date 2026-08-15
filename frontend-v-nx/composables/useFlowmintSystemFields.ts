export const FLOWMINT_SYSTEM_FIELDS = {
  documentNumber: "documentNumber",
  applicantAccount: "applicantAccount",
  applicantAssignmentId: "applicantAssignmentId",
  applicantOrgId: "applicantOrgId",
} as const;

export const FLOWMINT_DOCUMENT_NUMBER_KEY =
  FLOWMINT_SYSTEM_FIELDS.documentNumber;

export const withFlowmintSystemFields = (
  data: Record<string, unknown> | null | undefined,
  documentNumber: string | null | undefined,
) => ({
  ...(data || {}),
  [FLOWMINT_DOCUMENT_NUMBER_KEY]: documentNumber || "",
});

export const withoutFlowmintDisplayFields = (
  data: Record<string, unknown> | null | undefined,
) => {
  const value = { ...(data || {}) };
  delete value[FLOWMINT_DOCUMENT_NUMBER_KEY];
  return value;
};
