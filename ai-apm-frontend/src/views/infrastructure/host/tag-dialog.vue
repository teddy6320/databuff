<template>
  <el-dialog
    :visible.sync="showDialog"
    :title="$t('modules.views.infrastructure.host.s_736eaaae')"
    width="480px"
    :before-close="cancelHandle"
    :close-on-click-modal="false"
    class="dialog-modal">
    <el-form ref="tagForm" :model="tagForm" :rules="tagRules" size="small" label-width="0">
      <div class="tag-form-cont flex-h">
        <el-form-item prop="key" class="tag-form-item">
          <el-input
            v-model="tagForm.key"
            :maxlength="30"
            placeholder="Key"
            class="tag-key-input db" />
        </el-form-item>

        <span class="tag-form-dot">:</span>

        <el-form-item prop="value" class="tag-form-item flex-1">
          <el-input
            v-model="tagForm.value"
            :maxlength="100"
            placeholder="Value"
            class="db" />
        </el-form-item>

        <el-button
          @click="addTagHandle"
          size="small" icon="db-icon-add"
          class="tag-key-add"></el-button>
      </div>
    </el-form>

    <div class="tag-list">
      <span v-for="tag,idx in tagList" :key="idx"
        class="tag-item ell">{{ tag }}
        <i class="tag-close-icon db-icon-close" @click.stop="deleteTagHandle(idx)"></i>
      </span>
    </div>

    <div slot="footer">
      <el-button size="small" :disabled="postLoading" @click="cancelHandle">{{ $t('modules.components.dialog-template.s_625fb26b') }}</el-button>
      <el-button
        :type="tagList.length ? 'primary' : 'info'"
        :disabled="!tagList.length"
        :loading="postLoading"
        @click="saveHandle"
        size="small">{{ $t('modules.views.configInstall.plugin.s_be5fbbe3') }}</el-button>
    </div>
  </el-dialog>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import { Form } from 'element-ui'
import { toAsyncWait } from '@/utils/common';

const labelKeyReg = new RegExp(/^[a-zA-Z0-9_]{2,30}$/);
const labelValueReg = new RegExp(/^(?:[\u3400-\u4DB5\u4E00-\u9FEA\uFA0E\uFA0F\uFA11\uFA13\uFA14\uFA1F\uFA21\uFA23\uFA24\uFA27-\uFA29]|[\uD840-\uD868\uD86A-\uD86C\uD86F-\uD872\uD874-\uD879][\uDC00-\uDFFF]|\uD869[\uDC00-\uDED6\uDF00-\uDFFF]|\uD86D[\uDC00-\uDF34\uDF40-\uDFFF]|\uD86E[\uDC00-\uDC1D\uDC20-\uDFFF]|\uD873[\uDC00-\uDEA1\uDEB0-\uDFFF]|\uD87A[\uDC00-\uDFE0]|[a-zA-Z0-9_.~\/!@#$%&\*\?]){2,100}$/);

@Component
export default class TagDialog extends Vue {
  @Prop({ default: () => ({}) }) private params!: any;
  @Prop({ required: true }) private saveTagApi!: (params: any) => Promise<any>;

  public $refs!: {
    tagForm: Form
  }

  private showDialog = false;
  private postLoading = false;

  private tagList: any[] = [];

  private tagForm = {
    key: '',
    value: '',
  }
  get tagRules () {
    const validateKey = (rule: any, value: any, cb: any) => {
      const tagKey = value.trim()
      if (!tagKey) {
        cb(new Error(i18n.t('modules.views.infrastructure.host.s_37f79c4a') as string));
      } else if (!labelKeyReg.test(tagKey)) {
        if (tagKey.length < 2 || tagKey.length > 30) {
          cb(new Error(i18n.t('modules.views.infrastructure.host.s_f5db9393') as string));
        } else {
          cb(new Error(i18n.t('modules.views.infrastructure.host.s_62f8aa65') as string));
        }
      } else {
        cb();
      }
    };
    const validateValue = (rule: any, value: any, cb: any) => {
      const tagValue = this.tagForm.value.trim()
      if (!tagValue) {
        cb(new Error(i18n.t('modules.views.infrastructure.host.s_18529c95') as string));
      } else if (!labelValueReg.test(tagValue)) {
        if (tagValue.length < 2 || tagValue.length > 100) {
          cb(new Error(i18n.t('modules.views.infrastructure.host.s_25e65acc') as string));
        } else {
          cb(new Error(i18n.t('modules.views.infrastructure.host.s_0fa538a6') as string));
        }
      } else {
        cb();
      }
    };
    return {
      key: [
        { required: true, validator: validateKey, trigger: 'blur' },
        { required: true, validator: validateKey, trigger: 'change' },
      ],
      value: [
        { required: true, validator: validateValue, trigger: 'blur' },
        { required: true, validator: validateValue, trigger: 'change' },
      ],
    }
  }

  // 添加标签
  private addTagHandle () {
    this.$refs.tagForm.validate((valid: any) => {
      if (valid) {
        const tagKey = this.tagForm.key.trim()
        const tagValue = this.tagForm.value.trim()
        this.tagList.push(`${tagKey}:${tagValue}`);
        this.clearFormHandle()
      }
    })
  }

  // 删除标签
  private deleteTagHandle (index: number) {
    this.tagList.splice(index, 1);
  }

  // 清空表单
  private clearFormHandle () {
    this.tagForm.key = '';
    this.tagForm.value = '';
    this.$refs.tagForm.resetFields();
  }

  // 显示弹框
  public showHandle () {
    this.showDialog = true;
  }

  // 保存
  private async saveHandle () {
    if (typeof this.saveTagApi !== 'function') {
      return;
    }
    const params = {
      ...this.params,
      cover: 0, // 追加模式
      tags: this.tagList,
    }
    this.postLoading = true;
    const { result, error } = await toAsyncWait(this.saveTagApi(params));
    this.postLoading = false;
    if (!error) {
      this.$message.success(i18n.t('modules.views.configManage.alarm.s_3fdaeadf') as string);
      this.cancelHandle();
      this.$emit('on-saved', params);
    } else if (error.message !== 'interrupt') {
      this.$message.error(error.message || i18n.t('modules.views.dataReport.report.s_6452a055') as string)
    }
  }

  // 隐藏弹框
  private cancelHandle () {
    this.clearFormHandle();
    this.tagList = [];
    this.postLoading = false;
    this.showDialog = false;
  }
}
</script>

<style lang="scss" scoped>
.tag-form-cont {
  display: flex;
  align-items: flex-start;
  line-height: 32px;
  font-size: 12px;
  .tag-key-input {
    width: 130px;
  }
  .tag-key-value {
    width: 150px;
  }
  .tag-form-dot {
    width: 18px;
    text-align: center;
  }
  .tag-key-add {
    min-width: 32px;
    margin-left: 10px;
  }
}

.tag-list {
  margin-top: 10px;
  font-size: 12px;
  .tag-item {
    box-sizing: border-box;
    margin: 0 5px 5px 0;
    padding: 0 18px 0 5px;
    max-width: 100%;
    display: inline-block;
    vertical-align: top;
    line-height: 22px;
    background-color: var(--background-color-base);
    border-radius: 2px;
    position: relative;
    .tag-close-icon {
      height: 16px;
      width: 16px;
      font-size: 12px;
      font-weight: bold;
      line-height: 16px;
      text-align: center;
      transform: translate(0, -50%) scale(0.8);
      transition: background-color .3s ease, color .3s ease;
      opacity: 0.6;
      cursor: pointer;
      position: absolute;
      right: 2px;
      top: 50%;
      &:hover {
        opacity: 1;
      }
    }
  }
}
</style>
