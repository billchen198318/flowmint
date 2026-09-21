import type { GroovyBinding } from "./groovyContract";

export interface GroovyPreviewCommand {
  oid: string;
  expectedLockVersion: number;
  bpmnXml: string;
  groovyBindings: GroovyBinding[];
  nodeId: string;
  sampleInput: string;
}

export interface GroovyPreviewResult {
  status: string;
  errorCode?: string;
  result: unknown;
  logs: string[];
  logsTruncated: boolean;
  diagnostics: { code: string; line: number; column: number }[];
  elapsedMs: number;
  expectedLockVersion: number;
}

export type PrepareGroovyPreview = (
  binding: GroovyBinding,
  sampleInput: string,
) => Promise<GroovyPreviewCommand>;

/** Overlay the dialog copy without applying it to the modeler or saved version. */
export function previewBindings(
  bindings: GroovyBinding[],
  draft: GroovyBinding,
) {
  const matches = bindings.filter(
    (binding) =>
      binding.nodeId === draft.nodeId && binding.bindingId === draft.bindingId,
  );
  if (matches.length !== 1)
    throw new Error("Groovy 節點已變更，請重新開啟編輯器。");
  return JSON.parse(
    JSON.stringify(
      bindings.map((binding) => (binding === matches[0] ? draft : binding)),
    ),
  ) as GroovyBinding[];
}

/** A changed draft stays stale even if undo later restores the same text. */
export function previewGeneration() {
  let generation = 0;
  return {
    invalidate: () => ++generation,
    current: () => generation,
    accepts: (ticket: number) => ticket === generation,
  };
}
