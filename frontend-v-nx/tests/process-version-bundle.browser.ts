import BpmnModeler from "bpmn-js/lib/Modeler";
import flowmintModule from "../pages/fm_prog004d0001/bpmn/FlowmintPaletteProvider";
import flowmint from "../pages/fm_prog004d0001/bpmn/flowmint-moddle.json";
import { newGroovyBinding } from "../pages/fm_prog004d0001/bpmn/groovyContract";
import {
  createProcessVersionBundle,
  parseProcessVersionBundle,
  serializeProcessVersionBundle,
  stageProcessBundleModeler,
  validateProcessBundleDiagram,
} from "../pages/fm_prog004d0001/bpmn/processVersionBundle";

const assert = (condition: unknown, message: string) => {
  if (!condition) throw new Error(message);
};
const createModeler = (container: HTMLElement) => new BpmnModeler({
  container,
  additionalModules: [flowmintModule],
  moddleExtensions: { flowmint },
  flowmint: { groovyAvailable: () => true },
} as any);

async function run() {
  const host = document.getElementById("canvas")!;
  const original = createModeler(host);
  let restored: any = null;
  try {
    await original.createDiagram();
    const root = (original.get("canvas") as any).getRootElement();
    const modeling = original.get("modeling") as any;
    modeling.updateProperties(root, { id: "P1" });
    const shape = (original.get("elementFactory") as any).createShape({ type: "bpmn:ServiceTask" });
    modeling.createShape(shape, { x: 320, y: 150 }, root);
    const binding = newGroovyBinding(shape.id);
    binding.scriptContent = "// 瀏覽器固定測試樣本\nreturn [:]\n";
    modeling.updateProperties(shape, { taskType: "GROOVY", bindingId: binding.bindingId, name: "計算" });
    shape.businessObject.$groovyBinding = binding;
    const { xml } = await original.saveXML({ format: true });
    const bundle = createProcessVersionBundle("T1", "P1", {
      versionNo: 1, bpmnXml: xml, groovyBindings: [binding],
    });
    const restoredBundle = parseProcessVersionBundle(serializeProcessVersionBundle(bundle), "T1", "P1");
    await validateProcessBundleDiagram(restoredBundle, original.get("moddle"), true);
    restored = await stageProcessBundleModeler(restoredBundle, () => createModeler(document.createElement("div")));
    const restoredTask = restored.get("elementRegistry").get(shape.id);
    assert(restoredTask.businessObject.$groovyBinding.scriptContent === binding.scriptContent, "script lost on import");
    restoredTask.businessObject.$groovyBinding.timeoutMs = 500;
    assert(binding.timeoutMs === 3000 && restoredBundle.groovyBindings[0].timeoutMs === 3000, "binding aliasing");
    const saved = await restored.saveXML({ format: true });
    assert(!saved.xml.includes("固定測試樣本"), "script leaked into BPMN XML");

    const originalUndo = (original.get("commandStack") as any).canUndo();
    const broken = { ...restoredBundle, bpmnXml: '<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"><process id="P1" /></definitions>' };
    let rejected = false;
    try {
      await stageProcessBundleModeler(broken, () => createModeler(document.createElement("div")));
    } catch {
      rejected = true;
    }
    assert(rejected, "diagram without graphical data accepted");
    assert((original.get("elementRegistry") as any).get(shape.id) === shape, "failed import cleared active diagram");
    assert((original.get("commandStack") as any).canUndo() === originalUndo, "failed import changed undo stack");

    restored.attachTo(host);
    original.destroy();
    restored.get("canvas").zoom("fit-viewport");
    assert(host.querySelectorAll(".djs-container").length === 1, "canvas replacement failed");
    restored.get("commandStack").execute("flowmint.updateGroovyBinding", {
      element: restoredTask, binding: { ...binding, scriptContent: "return [changed: true]" },
    });
    restored.get("commandStack").undo();
    assert(restoredTask.businessObject.$groovyBinding.scriptContent === binding.scriptContent, "imported binding undo failed");
    restored.get("commandStack").redo();
    assert(restoredTask.businessObject.$groovyBinding.scriptContent === "return [changed: true]", "imported binding redo failed");
    return { success: true, checks: ["round-trip", "detached rendering", "independent binding", "XML separation", "failed import preservation", "canvas replacement", "undo/redo"] };
  } finally {
    restored?.destroy();
    // destroy() is safe for the original only before the explicit replacement.
    if (host.querySelector(".djs-container")) original.destroy();
  }
}

run().then(
  (result) => fetch("/result", { method: "POST", body: JSON.stringify(result) }),
  (error) => fetch("/result", { method: "POST", body: JSON.stringify({ success: false, error: String(error?.stack || error) }) }),
);
