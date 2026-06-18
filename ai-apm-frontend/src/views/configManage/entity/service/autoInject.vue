<template>
  <div class="auto-inject">
    <div class="font-13 mb-10">{{ $t('modules.views.configManage.entity.s_11157639') }}<span class="describe">{{ $t('modules.views.configManage.entity.s_ca8e39c8') }}</span></div>
    <div class="flex-h font-13 lh-26 pb-4">{{ $t('modules.views.configManage.entity.s_40c1c946') }}
      <i
        @click="addConditionItemHandle"
        class="add-btn db-icon-add ml-10"></i>
    </div>
    <div v-for="item, index in conditionList" :key="index"
      :class="{ multiple: item.field === '$pname$cmd'}"
      class="condition-item-wrap">
      <div class="condition-item">
        <el-select
          v-if="item.field !== '$pname$cmd'"
          v-model="item.field"
          @change="fieldChangeHandle(item)"
          size="small"
          :class="item.field === '$env' ? 'w100' : 'w210'">
          <el-option :label="$t('modules.views.configManage.entity.s_650470c5')" value="$user" />
          <el-option label="PName" value="$pname" />
          <el-option :label="$t('modules.views.configManage.entity.s_6a790111')" value="$pname$cmd" />
          <el-option :label="$t('modules.views.configManage.entity.s_e9557aa9')" value="$group" />
          <el-option :label="$t('modules.views.configManage.entity.s_2f325f26')" value="$path" />
          <el-option :label="$t('modules.views.configManage.entity.s_3867e350')" value="$env" />
        </el-select>
        <el-input
          v-else
          value="PName"
          disabled
          size="small" :placeholder="$t('modules.components.matching-criteria.s_02cc4f8f')"
          class="w210" />

        <el-input
          v-if="item.field === '$env'"
          v-model="item.env"
          size="small" :placeholder="$t('modules.components.matching-criteria.s_02cc4f8f')"
          class="w100 ml-10" />

        <el-select
          v-model="item.symbol"
          size="small"
          class="w100 ml-10">
          <el-option :label="$t('modules.components.matching-criteria.s_4c35bf2e')" value="==" />
          <el-option :label="$t('modules.components.matching-criteria.s_14a8af58')" value="!=" />
          <el-option :label="$t('modules.components.matching-criteria.s_e13556bb')" value="~=" />
          <el-option :label="$t('modules.components.matching-criteria.s_da0291f4')" value="^=" />
        </el-select>

        <el-input
          v-model="item.value"
          size="small" :placeholder="$t('modules.components.matching-criteria.s_02cc4f8f')"
          class="w210 ml-10" />

        <i
          v-if="item.field !== '$pname$cmd'"
          @click="deleteConditionItemHandle(index)"
          class="delete-btn flex-none db-icon-minus ml-10"></i>
      </div>
      <div v-if="item.field === '$pname$cmd'" class="condition-item">
        <el-input
          :value="$t('modules.views.configManage.entity.s_3d0a2df9')"
          disabled
          size="small" :placeholder="$t('modules.components.matching-criteria.s_02cc4f8f')"
          class="w210" />

        <el-select
          v-model="item.symbol2"
          size="small"
          class="w100 ml-10">
          <el-option :label="$t('modules.components.matching-criteria.s_4c35bf2e')" value="==" />
          <el-option :label="$t('modules.components.matching-criteria.s_14a8af58')" value="!=" />
          <el-option :label="$t('modules.components.matching-criteria.s_e13556bb')" value="~=" />
          <el-option :label="$t('modules.components.matching-criteria.s_da0291f4')" value="^=" />
        </el-select>

        <el-input
          v-model="item.value2"
          size="small" :placeholder="$t('modules.components.matching-criteria.s_02cc4f8f')"
          class="w210 ml-10" />
      </div>
      <i
        v-if="item.field === '$pname$cmd'"
        @click="deleteConditionItemHandle(index)"
        class="delete-btn flex-none db-icon-minus ml-10"></i>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import { toAsyncWait } from '@/utils/common';
import ConfigApi from '@/api/config';

// 去掉字符串前后引号
const removeQuotes = (val: string) => {
  if (val.length >= 2 && ((val.startsWith('"') && val.endsWith('"')) || (val.startsWith('\'') && val.endsWith('\'')))) {
    return val.slice(1, -1);
  }
  return val;
};
const parseConditionString = (str: string) => {
  const fields = ['$user', '$group', '$path', '$env', '$pname'];
  const symbols = ['==', '!=', '~=', '^='];

  const conditions = (str || '').split(/\s*\|\|\s*/);
  const result: any[] = [];
  for (const condition of conditions) {
    const symbol = symbols.find(t => condition.includes(t));
    if (symbol) {
      const parts = condition.split(symbol);
      let field = parts[0].trim();
      const value = removeQuotes(parts[1].trim());
      if (fields.includes(field) || (field.startsWith('$env(') && field.endsWith(')'))) {
        let env = '';
        if (!fields.includes(field)) {
          env = removeQuotes(field.slice(5, -1));
          field = '$env';
        }
        result.push({ field, env, symbol, value, symbol2: '', value2: '' });
      } else if (condition.startsWith('(') && condition.endsWith(')') && condition.includes('$pname') && condition.includes('$cmd')) {
        const innerConditions = condition.slice(1, -1).split(/\s*\&\&\s*/);
        let pnameCondition: any = null;
        let cmdCondition: any = null;

        for (const innerCondition of innerConditions) {
          const innerSymbol = symbols.find(t => innerCondition.includes(t));
          if (!innerSymbol) {
            continue;
          }
          const innerParts = innerCondition.split(innerSymbol);
          const innerField = innerParts[0].trim();
          const innerValue = removeQuotes((innerParts[1] || '').trim());

          if (innerField === '$pname') {
            pnameCondition = { symbol: innerSymbol, value: innerValue };
          } else if (innerField === '$cmd') {
            cmdCondition = { symbol: innerSymbol, value: innerValue };
          }
        }

        if (pnameCondition && cmdCondition) {
          result.push({
            field: '$pname$cmd',
            env: '',
            symbol: pnameCondition.symbol,
            value: pnameCondition.value,
            symbol2: cmdCondition.symbol,
            value2: cmdCondition.value,
          });
        }
      }
    }
  }
  return result;
}

@Component
export default class AutoInject extends Vue {

  private globalConfig: any = null;

  private conditionList: any[] = [{
    field: '',
    env: '',
    symbol: '',
    value: '',
    symbol2: '',
    value2: '',
  }]

  private created() {
    this.getConfig()
  }

  private addConditionItemHandle () {
    this.conditionList.push({
      field: '',
      env: '',
      symbol: '',
      value: '',
      symbol2: '',
      value2: '',
    });
  }

  private deleteConditionItemHandle (index: number) {
    this.conditionList.splice(index, 1);
  }

  private fieldChangeHandle (item: any) {
    item.env = '';
    // item.value = '';
    item.symbol2 = '';
    item.value2 = '';
  }

  private async getConfig () {
    const { result, error } = await toAsyncWait(ConfigApi.getConfigList('/oneagent/global'))
    if (!error) {
      const data: any[] = result?.data || [];
      const globalConfig = data.find(t => t.key === 'global');
      const process_inject_rules = (globalConfig || {}).value?.process_inject_rules || '';
      this.conditionList = parseConditionString(process_inject_rules);
      this.globalConfig = globalConfig || null;
    } else if (error.message !== 'interrupt') {
      this.$message.error(error.message);
    }
  }

  public validate () {
    return this.conditionList.every(item => {
      const pnameValid = item.symbol2 && item.value2;
      return item.field && item.symbol && item.value && (item.field !== '$env' || item.env) && (item.field !== '$pname$cmd' || pnameValid);
    });
  }

  public async saveConfig () {
    const process_inject_rules = this.conditionList.map(item => {
      if (item.field === '$env' && item.env) {
        return `${item.field}('${item.env}') ${item.symbol} '${item.value}'`;
      } else if (item.field === '$pname$cmd') {
        return `($pname ${item.symbol} '${item.value}' && $cmd ${item.symbol2} '${item.value2}')`;
      }
      return `${item.field} ${item.symbol} '${item.value}'`;
    }).join(' || ');
    const params: any = {
      builtIn: true,
      desc: this.globalConfig?.desc || '',
      key: this.globalConfig?.key || 'global',
      path: this.globalConfig?.path || '/oneagent/global',
      value: { ...this.globalConfig?.value, process_inject_rules }
    }
    if (!this.globalConfig) {
      params.add = true
    }
    const { result, error } = await toAsyncWait(ConfigApi.saveConfig(params));
    if (!error) {
      delete params.add;
      this.globalConfig = { ...this.globalConfig, ...params };
    } else if (error.message !== 'interrupt') {
      // this.$message.error(error.message);
    }
  }

  public async resetConfig () {
    const params: any = {
      builtIn: true,
      desc: this.globalConfig?.desc || '',
      key: this.globalConfig?.key || 'global',
      path: this.globalConfig?.path || '/oneagent/global',
      value: { ...this.globalConfig?.value, process_inject_rules: '' }
    }
    if (!this.globalConfig) {
      params.add = true
    }
    const { result, error } = await toAsyncWait(ConfigApi.saveConfig(params));
    if (!error) {
      delete params.add;
      this.globalConfig = { ...this.globalConfig, ...params };
      this.conditionList = [];
    } else if (error.message !== 'interrupt') {
      // this.$message.error(error.message);
    }
  }
}
</script>

<style lang="scss" scoped>
.auto-inject {
  .w100 {
    width: 100px;
  }
  .w210 {
    width: 210px;
  }

  .add-btn,
  .delete-btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    box-sizing: border-box;
    width: 20px;
    height: 20px;
    border: 1px solid var(--border-color-base);
    border-radius: 4px;
    cursor: pointer;
    color: var(--color-primary);
  }

  .delete-btn {
    color: var(--color-text-primary);
  }

  .condition-item {
    display: flex;
    align-items: center;
    margin-bottom: 10px;
  }

  .condition-item-wrap {
    margin-left: 20px;
    &.multiple {
      margin-bottom: 10px;
      width: 540px;
      padding: 10px 10px 0;
      border: 1px dashed var(--border-color-base);
      border-radius: 4px;
      position: relative;
      .delete-btn {
        margin-top: -10px;
        position: absolute;
        top: 50%;
        right: -30px;
      }
      .w210 {
        width: 199px;
      }
    }
  }
}
</style>
