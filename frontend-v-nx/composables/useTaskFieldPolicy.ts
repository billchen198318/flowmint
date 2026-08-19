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
  const visit = (components: any[] = []) => {
    for (const component of components) {
      const access = normalize(fields[component.key] || defaultAccess);
      if (component.key) {
        component.hidden = access === "HIDDEN" || access === "NONE";
        component.disabled = access !== "EDIT";
      }
      visit(component.components);
      for (const column of component.columns || []) visit(column.components);
      for (const row of component.rows || []) {
        for (const cell of row || []) visit(cell.components);
      }
    }
  };
  visit(schema?.components);
  return schema;
};
