<template>
  <div class="license-cont flex-v scroll_bar_style">
    <div class="sub-cont flex-v">
      <div class="sub-cont-header">{{ $t('modules.views.sysManage.license.s_01a49752') }}</div>
      <div class="sub-cont-body">
        <div class="base-info-item">
          <label>{{ $t('modules.views.sysManage.license.s_ff685527') }}</label>
          <span>{{ productNameEn || '-' }}</span>
        </div>
        <div class="base-info-item">
          <label>{{ $t('modules.views.sysManage.license.s_1ba29ed6') }}</label>
          <span>{{ licenseInfo.productVersion || '-' }}</span>
        </div>
        <div class="base-info-item">
          <label>{{ $t('modules.views.sysManage.license.s_e778fb24') }}</label>
          <span>{{ licenseInfo.licenseProductSerialnum || '-' }}</span>
        </div>
        <div class="base-info-item">
          <label>{{ $t('modules.views.aiPlatform.experts.s_3fea7ca7') }}</label>
          <span>
            <template>{{ licenseInfo.licenseProductStatus | statusFilter}}</template>
            <template v-if="!licenseInfo.isPermanent">{{ licenseInfo.licenseProductEndtime | endTimeFilter }}</template>
            <template v-else>{{ $t('modules.views.sysManage.license.s_38df62e6') }}</template>
          </span>
        </div>
      </div>
    </div>

    <div class="sub-cont flex-v">
      <div class="sub-cont-header">{{ $t('modules.views.sysManage.license.s_0b71e15d') }}</div>
      <div class="sub-cont-body license-upload-cont">
        <el-upload
          action="/webapi/user/lisupload"
          :headers="uploadHeaders"
          :on-success="uploadSuccessHandle"
          :on-error="uploadErrorHandle"
          accept=".lic"
        >
          <div class="license-upload-mask">
            <el-button type="primary" class="license-upload-btn">{{ $t('modules.views.aiPlatform.chat.s_a6fc9e3a') }}</el-button>
          </div>
        </el-upload>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component } from 'vue-property-decorator';
import i18n from '@/i18n';
import dayjs from 'dayjs';
import { getLicenseInfo, logoutHandle } from '@/api/user';
import { getRequestHeaders, removeTokenAndCid } from '@/utils/jsCookie';
import { LensVersionReg } from '@/utils/regexp';

// 永久授权结束时间
const PermanentEndTime = '2999-12-31'

@Component({
  filters: {
    statusFilter (val: number) {
      return val === 1 ? i18n.t('modules.views.sysManage.license.s_1324e4e4') as string : val === 0 ? i18n.t('modules.views.sysManage.license.s_076fb3a3') as string : '-';
    },
    endTimeFilter (times: number) {
      if (times) {
        return i18n.t('modules.views.sysManage.license.s_11427485', { value0: dayjs(new Date(times)).format('YYYY-MM-DD') }) as string;
      } else {
        return '';
      }
    },
  },
})
export default class LicenseManage extends Vue {
  get productNameEn () {
    return this.$store.getters['User/getLogoConfig']?.productNameEn || '';
  }

  private licenseInfo = {
    licenseProductName: '',
    licenseProductVersion: '',
    licenseProductSerialnum: '',
    licenseProductStatus: '',
    licenseProductEndtime: 0,
    productVersion: '',
    isPermanent: false, // 是否永久授权
  };

  private uploadHeaders = getRequestHeaders()

  private created() {
    getLicenseInfo().then((rst: any) => {
      if (rst.status === 200 && rst.message === 'SUCCESS' && rst.data) {
        const {
          licenseProductName,
          licenseProductVersion,
          licenseProductSerialnum,
          licenseProductStatus,
          licenseProductEndtime,
          productVersion
        } = rst.data;
        this.licenseInfo.licenseProductName = licenseProductName;
        this.licenseInfo.licenseProductVersion = licenseProductVersion;
        this.licenseInfo.licenseProductSerialnum = licenseProductSerialnum;
        this.licenseInfo.licenseProductStatus = licenseProductStatus;
        this.licenseInfo.licenseProductEndtime = licenseProductEndtime;
        // this.licenseInfo.productVersion = productVersion.replace('DataBuff|', '');
        const originVersion = (productVersion || '').split('|')[1] || '';
        this.licenseInfo.productVersion = LensVersionReg.test(originVersion) ? `${originVersion.substring(0, 1).toUpperCase()}${originVersion.substring(1)}` : originVersion
        this.licenseInfo.isPermanent = licenseProductEndtime && +new Date(licenseProductEndtime) >= +new Date(PermanentEndTime)
      }
    });
  }

  private uploadSuccessHandle(res: any) {
    if (res.status === 200 && res.message === 'SUCCESS') {
      this.$message.success(i18n.t('modules.views.sysManage.license.s_051fd7dd') as string);
      setTimeout(() => {
        logoutHandle().then((rst) => {
          // clear cookie
          this.$router.replace({
            query: {
              ...this.$route.query,
              __redirect: 'false',
            }
          });
          removeTokenAndCid();
          // clear store
          // reload login page
          // 避免产生redirect url
          // this.$router.replace('/login');
          window.location.reload();
        });
      }, 1000);
    } else {
      this.$message.error(i18n.t('modules.views.sysManage.license.s_45b96b44', { value0: res.message }) as string);
    }
  }

  private uploadErrorHandle(error: any) {
    // console.log(error)
    this.$message.error(i18n.t('modules.views.sysManage.license.s_18ec4ae8') as string);
  }
}
</script>

<style lang="scss" scoped>
.license-cont {
  height: 100%;
  overflow: hidden;
  overflow-y: auto;
  font-size: 13px;
  line-height: 20px;

  .sub-cont {
    width: 100%;
    & + .sub-cont {
      margin-top: 40px;
    }

    .sub-cont-header {
      margin-bottom: 10px;
      font-size: 14px;
      font-weight: 500;
      line-height: 22px;
    }
  }

  .base-info-item {
    margin-bottom: 10px;
    display: flex;
    &:last-child {
      margin-bottom: 0;
    }

    & > label {
      width: 120px;
      color: var(--color-text-secondary);
    }
  }

  .license-upload-cont {
    .license-upload-mask {
      width: 480px;
      height: 32px;
      border: 1px solid var(--border-color-base);
      text-align: right;
      border-radius: 4px;
      cursor: pointer;
      position: relative;

      &::before {
        content: '请选择License文件，单次只能上传一个文件';
        position: absolute;
        left: 8px;
        top: 8px;
        line-height: 1;
        color: var(--color-text-placeholder);
      }

      .license-upload-btn {
        margin: -1px -1px 0 0;
        line-height: 30px;
        box-shadow: none;
        padding: 0 10px;
        border-radius: 0 4px 4px 0;
      }
    }
  }
}
</style>
