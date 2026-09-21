import GroovyBindingCommand from "./GroovyBindingCommand";
import { newGroovyBinding } from "./groovyContract";

class FlowmintPaletteProvider {
  private readonly create: any;
  private readonly elementFactory: any;
  private readonly groovyAvailable: () => boolean;

  constructor(palette: any, create: any, elementFactory: any, config: any) {
    this.create = create;
    this.elementFactory = elementFactory;
    this.groovyAvailable = config?.groovyAvailable || (() => false);
    palette.registerProvider(this);
  }

  getPaletteEntries() {
    const createDataActionTask = (event: Event) => {
      const shape = this.elementFactory.createShape({
        type: "bpmn:ServiceTask",
      });
      const businessObject = shape.businessObject;
      businessObject.name = "Data Action Task";
      businessObject.taskType = "DATA_ACTION";
      businessObject.requestMapping = "{}";
      businessObject.responseMapping = "{}";
      this.create.start(event, shape);
    };

    const entries: Record<string, any> = {
      "create.flowmint-data-action-task": {
        group: "activity",
        className: "bpmn-icon-service-task",
        title: "建立 FlowMint Data Action Task",
        action: {
          dragstart: createDataActionTask,
          click: createDataActionTask,
        },
      },
    };
    if (this.groovyAvailable()) {
      const createGroovy = (event: Event) => {
        if (!this.groovyAvailable()) return;
        const shape = this.elementFactory.createShape({
          type: "bpmn:ServiceTask",
        });
        const binding = newGroovyBinding(shape.id);
        Object.assign(shape.businessObject, {
          name: "Groovy 腳本工作",
          taskType: "GROOVY",
          bindingId: binding.bindingId,
          $groovyBinding: binding,
        });
        this.create.start(event, shape);
      };
      entries["create.flowmint-groovy-task"] = {
        group: "activity",
        className: "bpmn-icon-script-task",
        title: "建立 FlowMint Groovy System Task（草稿）",
        action: { dragstart: createGroovy, click: createGroovy },
      };
    }
    return entries;
  }

}

(FlowmintPaletteProvider as any).$inject = [
  "palette",
  "create",
  "elementFactory",
  "config.flowmint",
];

export default {
  __init__: ["flowmintPaletteProvider", "groovyBindingCommand"],
  groovyBindingCommand: ["type", GroovyBindingCommand],
  flowmintPaletteProvider: ["type", FlowmintPaletteProvider],
};
