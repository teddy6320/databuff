<template>
  <div class="person-cont flex-v">
    <div class="person-baseinfo mb-15 bg-color p20">
      <div class="flex-h">
        <div class="user-avatar-icon db-blue">
          {{ (userInfo.account || '').slice(0, 1) | FirstLetterCapital }}
        </div>

        <div class="ml-20">
          <p class="fw-500 font-18 mb-12">{{ userInfo.account }}</p>
          <p class="describe">{{ getRoleName }}</p>
        </div>
      </div>

      <div>
        <h5 class="fw-500">{{ $t('personal.basicInfo') }}</h5>

        <el-row :gutter="20">
          <el-col :span="8" class="flex-h">
            <span class="describe w100 flex-0">{{ $t('personal.username') }}</span>
            <span class="flex-1 ell">{{ userInfo.account }}</span>
          </el-col>
          <el-col :span="8" class="flex-h">
            <span class="describe w100 flex-0">{{ $t('personal.role') }}</span>
            <span class="flex-1 ell">{{ getRoleName }}</span>
          </el-col>
          <el-col :span="8" class="flex-h">
            <span class="describe w100 flex-0">{{ $t('personal.responsible') }}</span>
            <span class="flex-1 ell">{{ userInfo.responsible || '-' }}</span>
          </el-col>
          <el-col :span="8" class="flex-h mt-24">
            <span class="describe w100 flex-0">{{ $t('personal.password') }}</span>
            <span class="flex-1">
              <span class="vm">* * * * * * * *</span>
            </span>
          </el-col>
        </el-row>
      </div>
    </div>

    <div class="person-config bg-color p20">
      <h5 class="fw-500 m0">{{ $t('personal.contact') }}</h5>
      <p class="mt-15 mb-20">{{ $t('personal.contactDesc') }}</p>

      <div class="person-config-item describe flex-h mb-20">
        <label class="w100">{{ $t('personal.email') }}</label>
        <span>{{ userInfo.email || $t('personal.unbound') }}</span>
      </div>

      <div class="person-config-item describe flex-h mb-20">
        <label class="w100">{{ $t('personal.mobile') }}</label>
        <span>{{ userInfo.mobile || $t('personal.unbound') }}</span>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import { namespace } from 'vuex-class';

const UserModel = namespace('User');

@Component({})
export default class PersonalCenter extends Vue {
  @UserModel.State private userInfo!: any;

  get getRoleName () {
    return this.userInfo?.currentRole?.roleName
  }
}
</script>

<style lang="scss" scoped>
.person-cont{
  flex: 1;
  padding: 16px;
  height: 100%;

  .user-avatar-icon {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    background-color: rgba(41, 98, 255, .1);
    text-align: center;
    line-height: 80px;
    font-size: 40px;
  }
  p {
    line-height: 1;
  }
  .mb-12 {
    margin-bottom: 12px !important;
  }
  .mt-24 {
    margin-top: 24px !important;
  }
  .w100 {
    width: 100px;
  }
}
</style>
