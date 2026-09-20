import type { GroovyBinding } from "./groovyContract";

interface BindingContext {
  element: any;
  binding: GroovyBinding;
  previous?: GroovyBinding;
  appliedBinding?: GroovyBinding;
}

class UpdateGroovyBindingHandler {
  execute(context: BindingContext) {
    context.previous = context.element.businessObject.$groovyBinding;
    context.appliedBinding ||= structuredClone(context.binding);
    context.element.businessObject.$groovyBinding = structuredClone(
      context.appliedBinding,
    );
    return [context.element];
  }

  revert(context: BindingContext) {
    context.element.businessObject.$groovyBinding = context.previous;
    return [context.element];
  }
}

export default class GroovyBindingCommand {
  constructor(commandStack: any) {
    commandStack.registerHandler(
      "flowmint.updateGroovyBinding",
      UpdateGroovyBindingHandler,
    );
  }
}
(GroovyBindingCommand as any).$inject = ["commandStack"];
