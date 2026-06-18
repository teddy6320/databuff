<template>
  <div class="authorization scroll_bar_style" v-if="finalStatus">
    <div class="top_bg" :style="{ backgroundImage: `url(${authBg})` }">{{ productNameEn ? $t('modules.views.authorization.s_f1e9fa54', { value0: productNameEn }) : '' }}</div>
    <el-button
      v-if="![1, 4].includes(finalStatus)"
      @click="logoutHandle"
      type="danger" size="small"
      class="logout-btn">{{ $t('modules.views.authorization.s_44efd179') }}</el-button>

    <div class="card-box">
      <el-card class="box-card" style="padding-top:20px;">
        <!-- 单机版，未授权 1 -->
        <div slot="header" v-if="[1, 4].includes(finalStatus)">
          <el-steps :active="active" finish-status="success" style="width: 600px;margin: 0 auto;">
            <el-step :title="$t('modules.views.authorization.s_2cdbd96a')"></el-step>
            <el-step :title="$t('modules.views.authorization.s_1a0c1515')"></el-step>
          </el-steps>
        </div>

        <!-- 单机版，已过期 2 -->
        <div class="license-tip-info" v-if="finalStatus === 2">
          <div class="license-tip-alert">{{ $t('modules.views.authorization.s_27fa6c62') }}</div>
          <br />
          <div class="license-tip-alert">{{ $t('modules.views.authorization.s_52afcb3e', { value0: licenseSerialnum }) }}</div>
          <br />
          <div v-if="!isAdmin" class="license-tip-alert">{{ $t('modules.views.authorization.s_f716f713') }}</div>
        </div>

        <!-- 单机版，重新授权 3 -->
        <div class="license-tip-info" v-if="finalStatus === 3 && !isAdmin">
          <div class="license-tip-alert">{{ $t('modules.views.authorization.s_018c61c5') }}</div>
        </div>

        <div v-if="active === 0" class="up-box">
          <!-- 未授权 or 重新授权 -->
          <div v-if="finalStatus === 1 || (finalStatus !== 2 && isAdmin)">
            <p style="margin-top:20px;text-align:center;">{{ $t('modules.views.authorization.s_0d46cf7e') }}</p>
            <img :src="`/webapi/user/qrCode?times=${times}`" @click="getTimes" width="160px" :alt="$t('modules.views.authorization.s_22b03c02')" />
            <p style="margin-top: 30px;text-align:center;">{{ $t('modules.views.authorization.s_39ecc12d') }}</p>
          </div>

          <!-- 未授权 or 已过期 or 重新授权 -->
          <div v-if="finalStatus === 1 || isAdmin" class="upload">
            <el-upload
              class="upload-demo"
              action="/webapi/user/lisupload"
              :headers="uploadHeaders"
              :before-upload="beforeUpload"
              :on-success="uploadSuccessHandle"
              :on-error="uploadErrorHandle"
              accept=".lic"
              :show-file-list="false"
              :max-size="50"
            >
              <span class="btn">{{ $t('modules.views.authorization.s_ba266496') }}</span>
              <div slot="tip" class="el-upload__tip">{{ $t('modules.views.authorization.s_77d898e3') }}</div>
            </el-upload>
          </div>
        </div>

        <div v-if='active === 1' class="account-box">
          <el-form @submit.native.prevent ref="createForm" size="small" :model="createForm" :rules="createRule" label-width="100px">
            <p class="title">{{ $t('modules.views.authorization.s_302ff00d') }}</p>
            <el-form-item :label="$t('modules.views.authorization.s_819767ad')">
              <span>Admin</span>
            </el-form-item>
            <el-form-item :label="$t('modules.views.authorization.s_a8105204')" prop="pass">
              <el-input type="password" show-password maxlength="100" v-model="createForm.pass" :placeholder="$t('modules.views.authorization.s_e39ffe99')" />
            </el-form-item>
            <el-form-item :label="$t('modules.views.authorization.s_3fbdde13')" prop="checkPass">
              <el-input type="password" show-password maxlength="100" v-model="createForm.checkPass" :placeholder="$t('modules.views.authorization.s_a24ab09b')" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="postHandle" :loading="postLoading" class="btn-confirm">{{ $t('modules.views.authorization.s_939d5345') }}</el-button>
              <el-button @click="resetHandle" class="btn-cancel">{{ $t('modules.views.authorization.s_4b9c3271') }}</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script lang="ts">import i18n from '@/i18n';

  import { Vue, Component, Watch } from 'vue-property-decorator';
  import { namespace } from 'vuex-class';
  import md5 from 'md5';
  import { Form } from 'element-ui';
  import SystemApi from '@/api/system';
  import UserApi from '@/api/user';
  import { getRequestHeaders, removeTokenAndCid } from '@/utils/jsCookie';
  import AuthBg from '@/assets/img/auth/auth_bg.jpg'

  const pwdReg = new RegExp(/^(?=.*[0-9].*)(?=.*[A-Z].*)(?=.*[a-z].*).{6,20}$/);
  const UserModel = namespace('User');
  @Component
  export default class AuthorizationPage extends Vue {
    @UserModel.State('userInfo') private userInfo!: any;

    public $refs!: {
      createForm: Form;
    };

    private active: number = 0; // 当前的steps
    private licenseSerialnum: string = ''; // 序列号

    private authBg = AuthBg;

    private createForm = {
      pass: '',
      checkPass: '',
    };

    private createRule = {
      pass: [
        {
          validator: (rule: any, value: any, cb: any) => {
            if (!value) {
              cb(new Error(i18n.t('modules.views.authorization.s_e39ffe99') as string));
            } else if (value.length < 6) {
              return cb(new Error(i18n.t('modules.views.authorization.s_ba992c5e') as string));
            } else if (!pwdReg.test(value)) {
              return cb(new Error(i18n.t('modules.views.authorization.s_8abda7be') as string));
            }
            cb();
          },
          trigger: 'blur',
        },
      ],
      checkPass: [
        {
          validator: (rule: any, value: any, cb: any) => {
            if (!value) {
              cb(new Error(i18n.t('modules.views.authorization.s_a24ab09b') as string));
            } else if (value !== this.createForm.pass) {
              return cb(new Error(i18n.t('modules.views.authorization.s_7dbe3fe2') as string));
            }
            cb();
          },
          trigger: 'blur',
        },
      ],
    };

    private postLoading: boolean = false;

    private times: number = new Date().valueOf();

    get finalStatus () {
      return this.$store.state.finalStatus
    }

    get isAdmin () {
      return (this.userInfo || {}).account === 'Admin';
    }

    get productNameEn () {
      return this.$store.getters['User/getLogoConfig']?.productNameEn || '';
    }

    get uploadHeaders () {
      return getRequestHeaders()
    }

    @Watch('userInfo', { deep: true, immediate: true })
    private onUserInfoChange (newVal: any) {
      if (newVal && newVal.account) {
        this.getLicenseSerialnum();
      }
    }

    private created() {
      const status = this.finalStatus
      if (status === 0) {
        this.$router.replace('/');
      } else if (status && ![1, 4].includes(status) && !this.userInfo.account) {
        this.$router.replace('/login');
      } else if (status === 4) {
        this.active = 1
      }
    }

    private beforeUpload(file: File) {
      if (file.size / 1024 > 50) {
        this.$message.error(i18n.t('modules.views.authorization.s_5c560ec8') as string);
        return false;
      }
    }
    private uploadSuccessHandle(res: any) {
      // console.log('upload success', res)
      if (res.status === 200) {
        if (this.finalStatus === 1) {
          this.next();
        } else {
          this.$message.success(i18n.t('modules.views.authorization.s_0c55ecb1') as string);
          setTimeout(() => {
            // this.$router.replace('/login');
            window.location.reload();
          }, 2000);
        }
      } else {
        this.$message.error(res.message || i18n.t('modules.views.authorization.s_1df41258') as string);
      }
    }
    private uploadErrorHandle() {
      this.$message.error(i18n.t('modules.views.authorization.s_5d2b9e71') as string);
    }
    private next() {
      if (this.active++ > 2) {
        this.active = 0;
      }
    }

    private postHandle() {
      this.$refs.createForm.validate((valid?: boolean) => {
        if (valid) {
          this.postLoading = true;
          const params = {
            admin: {
              account: 'Admin',
              password: md5(this.createForm.pass),
            },
          };
          SystemApi.createAdminAduit(params)
            .then((rst: any) => {
              if (rst.status === 200) {
                this.$message.success(i18n.t('modules.views.authorization.s_a3cafbf1') as string);
                setTimeout(() => {
                  // this.$router.replace('/login');
                  window.location.reload();
                }, 3000);
              } else {
                this.$message.error(i18n.t('modules.views.authorization.s_e3bb31f9') as string);
              }
            })
            .finally(() => {
              this.postLoading = false;
            });
        }
      });
    }

    private resetHandle() {
      this.$refs.createForm.resetFields();
    }

    private getTimes() {
      const times = new Date().valueOf();
      this.times = times;
    }

    private getLicenseSerialnum() {
      UserApi.getLicenseSerialnum().then((rst) => {
        if (rst.status === 200) {
          this.licenseSerialnum = rst.data;
        }
      });
    }

    private logoutHandle () {
      UserApi.logoutHandle()
        .then(rst => {
          // clear cookie
          this.$router.replace({
            query: {
              ...this.$route.query,
              __redirect: 'false',
            }
          });
          removeTokenAndCid()
          // clear store
          // reload login page
          // 避免产生redirect url
          sessionStorage.setItem('DB_TIMEINFO_SHOW', '')
          // this.$router.replace('/login')
          window.location.reload()
        })
    }
  }
</script>

<style lang="scss" scoped>
  .authorization {
    min-width: 980px;
    min-height: 600px;
    padding-bottom: 50px;
    position: relative;
    overflow: auto;
    .top_bg {
      padding-top: 134px;
      height: 336px;
      background-repeat: no-repeat;
      background-position: center;
      background-size: cover;
      text-align: center;
      color: #ffffff;
      font-size: 20px;
      letter-spacing: 1px;
      font-weight: 400;
      user-select: none;
    }
    .card-box {
      margin: -96px auto 0;
      width: 90%;
      max-width: 1280px;
    }
    .up-box {
      text-align: center;
      width: 350px;
      margin: 0 auto;
    }
    .btn {
      display: inline-block;
      padding: 8px 20px;
      border: 1px solid var(--color-primary);
      border-radius: 4px;
      color: var(--color-primary);
      cursor: pointer;
      font-size: 16px;
      line-height: 22px;
    }
    .account-box {
      width: 420px;
      margin: 0 auto;
    }
    .title {
      position: relative;
      margin-top: 20px;
      margin-bottom: 16px;
      margin-left: 10px;
      line-height: 22px;
    }
    .title:after {
      content: '';
      position: absolute;
      width: 3px;
      height: 16px;
      left: -8px;
      top: 3px;
      background: #ff6a00;
    }
    .license-tip-info {
      margin-bottom: 16px;
      color: #262626;
      font-size: 14px;
      text-align: center;
      .license-tip-alert {
        margin-bottom: 10px;
        display: inline-block;
        width: auto;
        text-align: center;
        padding: 8px 16px;
        border: 1px solid #ffb08f;
        background-color: #ffefe6;
        border-radius: 4px;
      }
    }
    .upload-demo {
      .el-upload__tip {
        margin-top: 16px;
      }
    }

    .logout-btn {
      position: absolute;
      top: 20px;
      right: 20px;
      color: #fff;
    }
  }
</style>
