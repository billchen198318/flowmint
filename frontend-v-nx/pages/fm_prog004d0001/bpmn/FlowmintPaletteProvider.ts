class FlowmintPaletteProvider {
  private readonly create: any;
  private readonly elementFactory: any;

  constructor(palette: any, create: any, elementFactory: any) {
    this.create = create;
    this.elementFactory = elementFactory;
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

    return {
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
  }
}

(FlowmintPaletteProvider as any).$inject = [
  "palette",
  "create",
  "elementFactory",
];

export default {
  __init__: ["flowmintPaletteProvider"],
  flowmintPaletteProvider: ["type", FlowmintPaletteProvider],
};
