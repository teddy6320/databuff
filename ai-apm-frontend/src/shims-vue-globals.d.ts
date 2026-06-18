import 'vue';

declare module 'vue/types/vue' {
  interface Vue {
    $eventBus: any;
    $i18n: any;
    $message: any;
    $route: any;
  }
}
