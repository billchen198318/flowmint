type FieldAccess = "HIDDEN" | "READ" | "EDIT" | "NONE";

interface FieldPolicy {
  default?: FieldAccess;
  fields?: Record<string, FieldAccess>;
}

const normalize = (value: unknown): FieldAccess => {
  const access = String(value || "READ").toUpperCase();
  return ["HIDDEN", "READ", "EDIT", "NONE"].includes(access)
    ? (access as FieldAccess)
    : "READ";
};

export const applyTaskFieldPolicy = (schema: any, content?: string) => {
  let policy: FieldPolicy = { default: "READ", fields: {} };
  try {
    policy = JSON.parse(content || "{}") as FieldPolicy;
  } catch {
    policy = { default: "READ", fields: {} };
  }
  const defaultAccess = normalize(policy.default);
  const fields = policy.fields || {};
  const visit = (components: any[] = [], gridPrefix = "") => {
    if (!Array.isArray(components)) return;
    for (const component of components) {
      const fieldPath = gridPrefix && component.key
        ? `${gridPrefix}.${component.key}`
        : component.key;
      const nestedGridPrefix = component.key
        && ["datagrid", "editgrid"].includes(component.type)
        ? `${fieldPath}[]`
        : gridPrefix;
      const hasEditableGridChild = nestedGridPrefix !== gridPrefix
        && Object.entries(fields).some(([path, access]) =>
          path.startsWith(`${nestedGridPrefix}.`) && normalize(access) === "EDIT");
      const access = normalize(
        fields[fieldPath] || (hasEditableGridChild ? "EDIT" : defaultAccess),
      );
      if (component.key) {
        component.hidden = access === "HIDDEN" || access === "NONE";
        component.disabled = access !== "EDIT";
      }
      visit(component.components, nestedGridPrefix);
      if (Array.isArray(component.columns)) {
        for (const column of component.columns) visit(column.components, gridPrefix);
      }
      if (Array.isArray(component.rows)) {
        for (const row of component.rows) {
          if (!Array.isArray(row)) continue;
          for (const cell of row) visit(cell.components, gridPrefix);
        }
      }
    }
  };
  visit(schema?.components);
  return schema;
};
