<template>
  <div class="login-setting-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">{{ $t('modules.views.configManage.login.s_2b82e5ed') }}</h2>
        <p class="page-desc">{{ $t('modules.views.configManage.login.s_2cae699f') }}</p>
      </div>
    </div>

    <el-form @submit.native.prevent size="small" label-position="top" class="setting-form">
      <el-form-item :label="$t('modules.views.configManage.login.s_4c06782f')">
        <el-radio-group v-model="localeModel" size="small" @change="switchLocaleHandle">
          <el-radio-button
            v-for="item in localeOptions"
            :key="item.value"
            :label="item.value">{{ $t(item.labelKey) }}</el-radio-button>
        </el-radio-group>
      </el-form-item>

      <el-form-item :label="$t('modules.views.configManage.login.s_c5345632')">
        <el-select v-model="pageTimeOut" :placeholder="$t('modules.views.configManage.login.s_718cdc4c')" class="form-select">
          <el-option :value="60" :label="$t('modules.views.configManage.login.s_76ebb2be')" />
          <el-option :value="300" :label="$t('modules.views.configManage.login.s_b2f296d7')" />
          <el-option :value="900" :label="$t('modules.views.configManage.login.s_f511e9e0')" />
          <el-option :value="1800" :label="$t('modules.views.configManage.login.s_751a79af')" />
          <el-option :value="3600" :label="$t('modules.views.configManage.alarm.s_c658fd73')" />
          <el-option :value="7200" :label="$t('modules.views.configManage.login.s_bb93d235')" />
          <el-option :value="43200" :label="$t('modules.views.configManage.alarm.s_d04708fe')" />
          <el-option :value="86400" :label="$t('modules.views.configManage.login.s_c39e6708')" />
        </el-select>
      </el-form-item>

      <el-button type="primary" size="small" :loading="saving" @click="setPageTimeOut">{{ $t('modules.views.configManage.login.s_74d9faed') }}</el-button>
    </el-form>
  </div>
</template>

<script lang="ts">
import i18n from '@/i18n';
import { Vue, Component } from 'vue-property-decorator';
import SystemApi from '@/api/system';
import { updateSessionIdleSeconds } from '@/utils/sessionIdle';
import locale from 'element-ui/lib/locale';
import elementZh from 'element-ui/lib/locale/lang/zh-CN';
import elementEn from 'element-ui/lib/locale/lang/en';
import { AppLocale, LOCALE_OPTIONS, setAppLocale } from '@/i18n';

@Component
export default class LoginSetting extends Vue {
  private pageTimeOut = 86400;
  private displayLocale: AppLocale = 'zh-CN';
  private saving = false;
  private localeSaving = false;
  private localeOptions = LOCALE_OPTIONS;

  get localeModel(): AppLocale {
    return this.displayLocale;
  }

  set localeModel(next: AppLocale) {
    this.switchLocaleHandle(next);
  }

  private created() {
    this.loadSettings();
  }

  private loadSettings(): void {
    SystemApi.getSystemBase()
      .then((rst: any) => {
        if (rst && rst.status === 200 && rst.data) {
          this.pageTimeOut = rst.data.pageTimeOut || 86400;
          if (rst.data.locale === 'zh-CN' || rst.data.locale === 'en-US') {
            this.displayLocale = rst.data.locale;
          }
        } else {
          throw new Error(rst.message);
        }
      })
      .catch((err) => {
        if (err.message !== 'interrupt') {
          this.$message.error(err.message);
        }
      });
  }

  private setPageTimeOut(): void {
    if (!this.pageTimeOut) {
      this.$message.error(i18n.t('modules.views.configManage.login.s_718cdc4c') as string);
      return;
    }
    this.saving = true;
    SystemApi.updatePageTimeOut({ pageTimeOut: this.pageTimeOut })
      .then((rst: any) => {
        if (rst && rst.status === 200) {
          updateSessionIdleSeconds(this.pageTimeOut);
          this.$message.success(i18n.t('modules.views.appMonitor.service.s_55aa6366') as string);
        } else {
          throw new Error(rst.message);
        }
      })
      .catch((err: any) => {
        if (err.message !== 'interrupt') {
          this.$message.error(err.message || i18n.t('modules.views.configInstall.dataAccess.s_5fa802be') as string);
        }
      })
      .finally(() => {
        this.saving = false;
      });
  }

  private switchLocaleHandle(next: AppLocale): void {
    if (next === this.displayLocale || this.localeSaving) {
      return;
    }
    const previous = this.displayLocale;
    this.displayLocale = next;
    this.localeSaving = true;
    SystemApi.updateDisplayLocale({ locale: next })
      .then((rst: any) => {
        if (rst && rst.status === 200) {
          setAppLocale(next);
          locale.use(next === 'zh-CN' ? elementZh : elementEn);
          this.$store.commit('UPDATE_LOCALE', next);
          this.$message.success(i18n.t('modules.views.appMonitor.service.s_55aa6366') as string);
        } else {
          throw new Error(rst.message);
        }
      })
      .catch((err: any) => {
        this.displayLocale = previous;
        if (err.message !== 'interrupt') {
          this.$message.error(err.message || i18n.t('modules.views.configInstall.dataAccess.s_5fa802be') as string);
        }
      })
      .finally(() => {
        this.localeSaving = false;
      });
  }
}
</script>

<style lang="scss" scoped>
.login-setting-page {
  .page-header {
    margin-bottom: 24px;
  }

  .page-title {
    margin: 0 0 8px;
    font-size: 18px;
    font-weight: 500;
    line-height: 26px;
  }

  .page-desc {
    margin: 0;
    color: var(--color-text-secondary);
    font-size: 13px;
    line-height: 20px;
  }

  .setting-form {
    max-width: 480px;
  }

  .form-select {
    width: 100%;
  }
}
</style>
