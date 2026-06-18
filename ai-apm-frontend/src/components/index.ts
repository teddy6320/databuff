import DbTabnav from './db-tabnav/index.vue';
import DbIconButton from './DbIconButton/index.vue'
import ScrollSelect from './scroll-select.vue';
import DbTable from './db-table';
import DbRadio from './db-radio/index.vue';
import BasicChart from './charts/basic-chart.vue';
import DbQuery from './query-filter/index.vue';

const components: any = {
  DbTabnav,
  ScrollSelect,
  DbTable,
  DbRadio,
  DbIconButton,
  BasicChart,
  DbQuery,
};

const componentsName: string[] = Object.keys(components);
export default {
  install: (vue: any) => {
    componentsName.forEach((i) => {
      vue.component(i, components[i]);
    });
  },
};
