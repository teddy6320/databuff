import { Vue, Component } from 'vue-property-decorator';

declare module 'vue/types/vue' {
  interface Vue {
    dispatch(componentName: string, eventName: string, params: any): void;
  }
}

@Component
export default class Emitter extends Vue {
  public dispatch(componentName: string, eventName: string, params: any): void {
    let parent = this.$parent || this.$root;
    let name = parent.$options.name;

    while (parent && (!name || name !== componentName)) {
      parent = parent.$parent as any;

      if (parent) {
        name = parent.$options.name;
      }
    }
    if (parent) {
      parent.$emit.apply(parent, ([eventName] as any).concat(params));
    }
  }
}
