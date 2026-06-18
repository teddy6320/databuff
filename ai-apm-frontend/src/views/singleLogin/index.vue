<template>
  <div class="single-login-wrap" v-loading="true">
    <!-- 单点登录中间页 -->
  </div>
</template>

<script lang="ts">
import { Vue, Component } from 'vue-property-decorator'
import UserApi from '@/api/user';
import i18n from '@/i18n';
import { setTokenAndCid, setDsi, setOi, setDbh } from '@/utils/jsCookie';
import { namespace } from 'vuex-class';
const UserModel = namespace('User');

@Component
export default class SingleLogin extends Vue {
  @UserModel.Mutation('SET_FIRST_LOGIN') private setFirstLogin!: (userInfo?: any) => void

  private timer: any = null

  private created () {
    const { query, params } = this.$route
    UserApi.singleAuthLogin({
      tenant: query.tenant,
      code: query.code,
      from: params.type,
    })
      .then((rst: any) => {
        if (rst.status === 200 && rst.data) {
          const { token, cid, ds = {} } = rst.data
          // 设置cookie
          setTokenAndCid(token, cid);
          this.setFirstLogin(false); // 单点登录不需要判断首次登录修改密码
          this.$store.commit('User/SET_DS_INFO', ds);
          setDsi(ds.id || 1);
          setOi(ds.orgId || 1);
          setDbh(ds.dashboardUId || '');
          this.$router.push({ path: '/', query: { __ps: 'm' } })
        } else {
          throw new Error(rst.message);
        }
      })
      .catch((err) => {
        this.$message.error(i18n.t('modules.views.singleLogin.s_1eeb8269', { value0: err.message }) as string);
        this.timer = setTimeout(() => {
          this.$router.push('/404');
        }, 2000)
      });
  }

  private beforeDestroy () {
    if (this.timer) {
      window.clearTimeout(this.timer);
      this.timer = null;
    }
  }
}
</script>

<style lang="scss" scoped>
.single-login-wrap {
  width: 100%;
  height: 100%;

  :deep(.el-loading-mask) {
    background-color: transparent;
  }
}
</style>
