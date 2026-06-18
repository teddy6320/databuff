<template>
  <div class="base-setting flex-v scroll_bar_style">
    <div class="item flex-v">
      <div class="item-tit datetime">{{ $t('modules.views.sysManage.basic.s_8619627a') }}</div>

      <el-form @submit.native.prevent size="small" label-position="top" class="item-form">
        <el-form-item label-width="0" class="m-0">
          <el-radio-group v-model="radio">
            <el-radio :label="0">{{ $t('modules.views.layout.s_87a29491') }}</el-radio>
            <el-radio :label="1">{{ $t('modules.views.sysManage.basic.s_57ae4dbe') }}</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="radio === 0" :label="$t('modules.views.sysManage.basic.s_0c3bf4fc')">
          <el-date-picker
            v-model="date"
            type="datetime"
            prefix-icon="el-icon-date"
            :placeholder="$t('modules.views.dataReport.report.s_f7ebeb55')"
            class="form-input" />
        </el-form-item>

        <el-form-item v-else :label="$t('modules.views.sysManage.basic.s_f22a0444')">
          <el-input v-model="ntpServer" maxlength="100" :placeholder="$t('modules.views.sysManage.basic.s_c9fde7bc')" class="form-input" />
        </el-form-item>

        <el-button type="primary" size="small" @click="radio === 0 ? setSystemDate() : setNtpServer()">{{ $t('modules.views.configManage.login.s_74d9faed') }}</el-button>
      </el-form>
    </div>

    <div class="item flex-v">
      <div class="item-tit overtime">{{ $t('modules.views.sysManage.basic.s_c173a333') }}</div>

      <el-form @submit.native.prevent size="small" label-position="top" class="item-form">
        <el-form-item :label="$t('modules.views.sysManage.basic.s_56071a4f')">
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

        <el-button type="primary" size="small" @click="setPageTimeOut">{{ $t('modules.views.configManage.login.s_74d9faed') }}</el-button>
      </el-form>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component } from 'vue-property-decorator';
import i18n from '@/i18n';
import dayjs from 'dayjs';
import SystemApi from '@/api/system';
@Component
export default class BasicConfig extends Vue {
  private radio: number = 0;
  private date: string = '';
  private ntpServer: string = '';
  private pageTimeOut: number = 0;

  private created() {
    this.getsysdate();
    this.getSystemBase();
  }

  // 获取服务器时间
  private getsysdate(): void {
    SystemApi.getsysdate()
      .then((rst: any) => {
        if (rst && rst.status === 200) {
          this.date = rst.data;
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

  // 获取系统基础设置
  private getSystemBase(): void {
    SystemApi.getSystemBase()
      .then((rst: any) => {
        if (rst && rst.status === 200 && rst.data) {
          const data = rst.data;
          this.radio = data.ntpAuto || 0;
          this.ntpServer = data.ntpServer;
          this.pageTimeOut = data.pageTimeOut;
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

  // 设置服务器时间
  private setSystemDate(): void {
    if (!this.date) {
      this.$message.error(i18n.t('modules.views.dataReport.report.s_f7ebeb55') as string);
      return;
    }
    const params: any = {
      date: dayjs(new Date(this.date)).format('YYYY-MM-DD HH:mm:ss'),
    };

    SystemApi.setsysdate(params)
      .then((rst: any) => {
        if (rst && rst.status === 200) {
          this.$message.success(i18n.t('modules.views.appMonitor.service.s_55aa6366') as string);
        } else {
          throw new Error(rst.message);
        }
      })
      .catch((err: any) => {
        if (err.message !== 'interrupt') {
          this.$message.error(err.message || i18n.t('modules.views.configInstall.dataAccess.s_5fa802be') as string);
        }
      });
  }

  // 设置NTP服务器
  private setNtpServer(): void {
    if (!this.ntpServer) {
      this.$message.error(i18n.t('modules.views.sysManage.basic.s_c9fde7bc') as string);
      return;
    }
    const params: any = {
      ntpServer: this.ntpServer,
      ntpAuto: 1,
    };

    SystemApi.setNtpServer(params)
      .then((rst: any) => {
        if (rst && rst.status === 200) {
          this.$message.success(i18n.t('modules.views.appMonitor.service.s_55aa6366') as string);
        } else {
          throw new Error(rst.message);
        }
      })
      .catch((err: any) => {
        if (err.message !== 'interrupt') {
          this.$message.error(err.message || i18n.t('modules.views.configInstall.dataAccess.s_5fa802be') as string);
        }
      });
  }

  // 设置页面超时时间
  private setPageTimeOut(): void {
    if (!this.pageTimeOut) {
      this.$message.error(i18n.t('modules.views.configManage.login.s_718cdc4c') as string);
      return;
    }
    const params: any = {
      pageTimeOut: this.pageTimeOut,
    };

    SystemApi.updatePageTimeOut(params)
      .then((rst: any) => {
        if (rst && rst.status === 200) {
          this.$message.success(i18n.t('modules.views.appMonitor.service.s_55aa6366') as string);
        } else {
          throw new Error(rst.message);
        }
      })
      .catch((err: any) => {
        if (err.message !== 'interrupt') {
          this.$message.error(err.message || i18n.t('modules.views.configInstall.dataAccess.s_5fa802be') as string);
        }
      });
  }
}
</script>

<style lang="scss" scoped>
.base-setting {
  height: 100%;
  overflow: hidden;
  overflow-y: auto;
  font-size: 13px;
  line-height: 20px;

  .item {
    width: 100%;
    background-color: var(--bg-color);
    & + .item {
      margin-top: 40px;
    }
    .item-tit {
      margin-bottom: 10px;
      font-size: 14px;
      font-weight: 500;
      line-height: 22px;
    }
  }
}

.item-form {
  .form-input,
  .form-select {
    width: 480px;
    vertical-align: top;
  }

  :deep(.el-form-item__label),
  :deep(.el-radio__label) {
    font-size: 13px;
  }

  :deep(.el-form-item__label) {
    width: auto;
    text-align: left;
  }
}
</style>
