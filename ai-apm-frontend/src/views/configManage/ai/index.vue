<template>
  <div class="config-wrapper" v-loading="isLoading">
    <div class="config-content">
      <div class="fw-500 mb-10">{{ $t('modules.views.configManage.ai.s_467839ff') }}</div>
      <el-form :model="configForm" :rules="configRules" ref="configForm"
        label-position="top" size="small" class="form-box">
        <el-form-item :label="$t('modules.views.configManage.ai.s_1199ddd4')" prop="open">
          <el-switch v-model="configForm.open"></el-switch>
        </el-form-item>

        <el-form-item :label="$t('modules.views.configManage.ai.s_920fe38e')" prop="model">
          <el-input
            v-model="configForm.model"
            :placeholder="$t('modules.views.configManage.ai.s_6bb5addf')"
            :maxlength="300"
            class="config-input" />
        </el-form-item>

        <el-form-item :label="$t('modules.views.configManage.ai.s_a73fb26d')" prop="url">
          <el-input
            v-model="configForm.url"
            :placeholder="$t('modules.views.configManage.ai.s_8d299ca2')"
            :maxlength="300"
            class="config-input" />
        </el-form-item>

        <el-form-item label="API Key" prop="apiKey">
          <el-input
            v-model="configForm.apiKey"
            :placeholder="$t('modules.views.configManage.ai.s_823df9c1')"
            :maxlength="300"
            class="config-input" />
        </el-form-item>
      </el-form>
    </div>

    <div class="pt-10">
      <el-button
        :loading="resetLoading"
        :disabled="!hasEntityManageAuth || postLoading || testLoading"
        @click="getConfig(true)"
        size="small">{{ $t('modules.views.authorization.s_4b9c3271') }}</el-button>
      <el-button
        :loading="testLoading"
        :disabled="!hasEntityManageAuth || resetLoading || postLoading"
        @click="testHandle"
        size="small">{{ $t('modules.views.configManage.ai.s_db06c78d') }}</el-button>
      <el-button
        :loading="postLoading"
        :disabled="!hasEntityManageAuth || resetLoading || testLoading"
        @click="saveHandle"
        type="primary" size="small">{{ $t('modules.views.configInstall.plugin.s_be5fbbe3') }}</el-button>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component } from 'vue-property-decorator';
import i18n from '@/i18n';
import { Form } from 'element-ui'
import { toAsyncWait } from '@/utils/common';
import ConfigApi from '@/api/config';

@Component
export default class AiConfig extends Vue {
  public $refs!: {
    configForm: Form
  }

  private configForm: any = {
    open: false,
    model: '',
    url: '',
    apiKey: '',
  }

  private configRules = {
    model: [
      { required: true, trigger: 'blur', message: i18n.t('modules.views.configManage.ai.s_6bb5addf') as string, messageKey: 'modules.views.configManage.ai.s_6bb5addf' },
      { required: true, trigger: 'change', message: i18n.t('modules.views.configManage.ai.s_6bb5addf') as string, messageKey: 'modules.views.configManage.ai.s_6bb5addf' },
    ],
    url: [
      { required: true, trigger: 'blur', message: i18n.t('modules.views.configManage.ai.s_8d299ca2') as string, messageKey: 'modules.views.configManage.ai.s_8d299ca2' },
      { required: true, trigger: 'change', message: i18n.t('modules.views.configManage.ai.s_8d299ca2') as string, messageKey: 'modules.views.configManage.ai.s_8d299ca2' },
    ],
    apiKey: [
      { required: true, trigger: 'blur', message: i18n.t('modules.views.configManage.ai.s_823df9c1') as string, messageKey: 'modules.views.configManage.ai.s_823df9c1' },
      { required: true, trigger: 'change', message: i18n.t('modules.views.configManage.ai.s_823df9c1') as string, messageKey: 'modules.views.configManage.ai.s_823df9c1' },
    ],
  }

  private detail: any = {}
  private isLoading = false // 配置获取中
  private postLoading = false // 配置获取中
  private resetLoading = false // 配置重置中
  private testLoading = false // 配置测试中

  private created () {
    this.getConfig()
  }

  private async getConfig (reset?: boolean) {
    if (!reset) {
      this.isLoading = true
    } else {
      this.resetLoading = true
    }
    const { result, error } = await toAsyncWait(ConfigApi.getAIConfig())
    if (!reset) {
      this.isLoading = false
    } else {
      this.resetLoading = false
    }
    if (!error) {
      this.$store.commit('User/SET_AI_ENABLED', !!result?.data?.open);
      this.detail = result?.data || {};
      if (reset) {
        this.$refs.configForm.resetFields();
      }
      this.initForm()
    } else if (error.message !== 'interrupt') {
      this.$message.error(error.message);
    }
  }

  private initForm () {
    for (const key in this.configForm) {
      if (this.detail.hasOwnProperty(key)) {
        if (key === 'open') {
          this.configForm.open = !!this.detail.open
        } else {
          this.configForm[key] = this.detail[key] || ''
        }
      }
    }
  }

  private saveHandle () {
    this.$refs.configForm.validate(async (valid: boolean) => {
      if (valid) {
        const params = { ...this.configForm }
        this.postLoading = true;
        const { error } = await toAsyncWait(ConfigApi.updateAIConfig(params));
        this.postLoading = false;
        if (!error) {
          this.$store.commit('User/SET_AI_ENABLED', !!params.open);
          this.$message.success(i18n.t('modules.views.appMonitor.service.s_55aa6366') as string);
          this.detail = { ...params }
        } else if (error.message !== 'interrupt') {
          this.$message.error(error.message || i18n.t('modules.views.cockpit.tab.s_6de920b4') as string);
        }
      }
    })
  }

  private testHandle () {
    this.$refs.configForm.validate(async (valid: boolean) => {
      if (valid) {
        const params = { ...this.configForm }
        this.testLoading = true;
        const { error } = await toAsyncWait(ConfigApi.testAIConfig(params));
        this.testLoading = false;
        if (!error) {
          this.$message.success(i18n.t('modules.views.configManage.ai.s_86fecf36') as string);
          this.detail = { ...params }
        } else if (error.message !== 'interrupt') {
          this.$message.error(i18n.t('modules.views.configManage.ai.s_ca14f914') as string);
        }
      }
    })
  }
}
</script>

<style lang="scss" scoped>
.config-wrapper {
  flex: 1;
  height: 100%;
  overflow: hidden;
  overflow-y: auto;
  display: flex;
  flex-direction: column;

  .config-content {
    flex: 1;
    overflow: auto;
  }

  .config-select,
  .config-input {
    width: 480px;
  }

  .slow-sql-input,
  .slow-sql-select {
    width: 235px;
  }

  .input-number :deep(.el-input__inner) {
    text-align: left;
  }

  :deep(.el-form-item.is-required>.el-form-item__label:before) {
    display: none;
  }

  .m-0 {
    margin: 0 !important;
  }
}
</style>
