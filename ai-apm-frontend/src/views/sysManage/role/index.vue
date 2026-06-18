<template>
  <div class="user-role-cont flex-v">
    <div class="flex-h">
      <db-query
        v-model="queryParams"
        :filter-list="filterList"
        :updateRoute='true'
        @on-change="searchChangeHandle"
        @on-remove-tag="searchChangeHandle"
        class="flex-1" />
      <el-button
        @click="toggleRoleModalHandle(true)"
        type="primary" size="small" class="ml-10 flex-none">
        <i class="db-icon-add"></i> {{ $t('modules.views.sysManage.role.s_d29cf59c') }}</el-button>
    </div>

    <div class="role-list" v-loading='isLoading'>
      <db-table
        :data='roleList'
        :total='roleListTotal'
        :columnConfig='columnConfig'
        :timeMode='false'
        @on-table-inited='tableInitedHandle'
        row-key="id"
        default-expand-all
        :tree-props="{children: 'children', hasChildren: 'hasChildren'}"
        tableKey='SYSTEM_ROLE_LIST'
        ref='listTable'>
        <template slot="suffix">
          <el-table-column :label="$t('modules.views.aiPlatform.experts.s_2b6bc0f2')" width="295">
            <template slot-scope="{ row, $index }">
              <a @click.stop="togglePermisModalHandle(true, row)" class="blue cp">{{ $t('modules.views.sysManage.role.s_2199b731') }}</a>
              <a v-if='groupEnabled' @click.stop="toggleGroupModalHandle(true, row)" class="blue cp ml-15">{{ $t('modules.views.sysManage.role.s_48deb594') }}</a>
              <a v-if="row.defineType === '自定义'" @click.stop="toggleRoleModalHandle(true, row)" class="blue cp ml-15">{{ $t('modules.views.hide.advancedConfig.s_95b351c8') }}</a>
              <a v-if="row.defineType === '自定义'" @click.stop="deleteRoleHandle(row, $index)" class="blue cp ml-15">{{ $t('modules.views.hide.advancedConfig.s_2f4aaddd') }}</a>
            </template>
          </el-table-column>
        </template>
      </db-table>
    </div>

    <!-- 新建、编辑角色 -->
    <el-dialog
      :visible.sync="showRoleModal"
      :title="this.isEditRole ? $t('modules.views.sysManage.role.s_ac775e9a') : $t('modules.views.sysManage.role.s_d29cf59c')"
      width="480px"
      :before-close="() => toggleRoleModalHandle()"
      :close-on-click-modal="false"
    >
      <el-form @submit.native.prevent class="form-box" ref="roleForm" :model="roleForm" :rules="roleRules" label-width="80px" label-position="left" size="small">
        <el-form-item
          :label="$t('modules.views.sysManage.role.s_10a6f121')"
          prop="roleName"
        >
          <el-input v-model="roleForm.roleName" maxlength="16" :placeholder="$t('modules.views.sysManage.role.s_2a92f526')" class="form-input" />
        </el-form-item>

        <el-form-item :label="$t('modules.views.sysManage.role.s_b6b7666e')" prop='pid'>
          <el-cascader
            v-model="roleForm.pid"
            @change="pidChangeHandle"
            :props="{ label: 'name', value: 'id', checkStrictly: true }"
            :options="roleOptionList"
            clearable
            size="small" filterable
            :placeholder="$t('modules.views.sysManage.role.s_c272d092')"
            class="role-custom-select form-input" />
        </el-form-item>

        <el-form-item :label="$t('modules.views.sysManage.role.s_9036c224')" prop="description">
          <el-input
            v-model="roleForm.description"
            type="textarea"
            :autosize="{ minRows: 2, maxRows: 5 }"
            :placeholder="$t('modules.views.sysManage.role.s_1d296a9b')"
            maxlength="100"
            show-word-limit
            class="form-input"
          />
        </el-form-item>
      </el-form>

      <div slot="footer">
        <el-button size="small" :disabled="saving" @click="toggleRoleModalHandle()">{{ $t('modules.views.aiPlatform.experts.s_625fb26b') }}</el-button>
        <el-button type="primary" size="small" :loading="saving" @click="saveRoleForm">{{ $t('modules.views.alarmCenter.alarmDetail.s_38cf16f2') }}</el-button>
      </div>
    </el-dialog>

    <!-- 查看、配置功能权限 -->
    <el-dialog
      :visible.sync="showPermisModal"
      :title="$t('modules.views.sysManage.role.s_2199b731')"
      width="480px"
      :before-close="() => togglePermisModalHandle()"
      :close-on-click-modal="this.isViewPermis"
    >
      <el-form v-if="showPermisModal" @submit.native.prevent class="form-box" ref="permisForm" :model="permisForm" size="small">
        <el-form-item v-if="!this.isViewPermis" label-width="20px" class="checkall-item">
          <el-checkbox
            v-model="permisCheckAll"
            :indeterminate="isIndeterminateAll"
            @change="permisCheckAllHandle"
            class="check-all">{{ $t('modules.views.sysManage.role.s_66eeacd9') }}</el-checkbox>
        </el-form-item>

        <el-form-item
          label-width="20px"
          prop="permis"
          :rules="!this.isViewPermis ? { required: true, type: 'array', min: 1, message: $t('modules.views.sysManage.role.s_ac19636b'), trigger: 'change' } : null"
        >
          <el-tree
            class="tree scroll_bar_style"
            ref="tree"
            :data="permisList"
            :default-expanded-keys="parentIdList"
            :default-checked-keys="checkedPermisIdList"
            show-checkbox
            node-key="id"
            @check-change="checkPermisChange"
          />
        </el-form-item>
      </el-form>

      <div v-if="!this.isViewPermis && showPermisModal" slot="footer">
        <el-button size="small" :disabled="saving" @click="togglePermisModalHandle()">{{ $t('modules.views.aiPlatform.experts.s_625fb26b') }}</el-button>
        <el-button type="primary" size="small" :loading="saving" @click="savePermisForm">{{ $t('modules.views.alarmCenter.alarmDetail.s_38cf16f2') }}</el-button>
      </div>
    </el-dialog>

    <!-- 绑定管理域 -->
    <el-dialog
      :visible.sync="showGroupModal"
      :title="$t('modules.views.sysManage.role.s_48deb594')"
      width="480px"
      :before-close="() => toggleGroupModalHandle()"
      :close-on-click-modal="false"
      class="role-group-dialog"
    >
      <div v-loading='groupLoading'> 
        <div class="font-13">{{ $t('modules.views.sysManage.role.s_e4916d11') }}</div>
        <div class="group-list-wrapper pl-15">
          <div class="group-list-item mt-8">
            <div class="flex-h">
              <el-checkbox v-model='fullGroupModel.checked' :disabled='true'>{{ fullGroupModel.nameKey ? $t(fullGroupModel.nameKey) : fullGroupModel.name }}</el-checkbox>
              <el-tooltip :content="$t('modules.views.sysManage.role.s_b0237386')" placement="top-start" effect="light" class="ml-10">
                <i class="db-icon-info font-14 describe"></i>
              </el-tooltip>
            </div>
            <div v-show='fullGroupModel.checked' class="ml-20 group-list-item-auth">
              <el-checkbox v-model='fullGroupModel.dataAuth' :disabled="true" class="sm">{{ $t('modules.views.sysManage.role.s_7f71de51') }}</el-checkbox>
              <el-checkbox v-model='fullGroupModel.configAuth' :disabled="true" class="sm">{{ $t('modules.views.sysManage.role.s_722f4e68') }}</el-checkbox>
            </div>
          </div>
          <div v-for='group in groupList' :key='group.id' v-show='!fullGroupModel.checked' class="group-list-item mt-8">
            <div>
              <el-checkbox v-model='group.checked' :disabled='group.disable' class="group-checkbox">{{ group.nameKey ? $t(group.nameKey) : group.name }}</el-checkbox>
            </div>
            <div v-show='group.checked' class="ml-20 group-list-item-auth">
              <el-checkbox v-model='group.dataAuth' :disabled="true" class="sm">{{ $t('modules.views.sysManage.role.s_7f71de51') }}</el-checkbox>
              <el-checkbox v-model='group.configAuth' :disabled="group.configDisable" class="sm">{{ $t('modules.views.sysManage.role.s_722f4e68') }}</el-checkbox>
            </div>
          </div>
        </div>
      </div>

      <div slot="footer">
        <el-button
          @click="toggleGroupModalHandle()"
          :disabled="postGroupLoading"
          size="small">{{ $t('modules.views.aiPlatform.experts.s_625fb26b') }}</el-button>
        <el-button v-if='!isCurrentChooseAdminRole'
          @click="saveGroupConfig"
          :loading="postGroupLoading"
          type="primary" size="small">{{ $t('modules.views.sysManage.role.s_4e868b4b') }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { Vue, Component } from 'vue-property-decorator';
import i18n from '@/i18n';
import { namespace } from 'vuex-class';
import { Form, Tree } from 'element-ui';
import SystemApi from '@/api/system';
import { toAsyncWait } from '@/utils/common';
const UserModel = namespace('User');
import GroupApi from '@/api/group'
import deepClone from 'lodash/cloneDeep';

const filterOptionsById = (list: any[], targetId: number) => {
  return list.filter(item => item.id !== targetId)
    .map(item => {
      if (Array.isArray(item.children)) {
        item.children = filterOptionsById(item.children, targetId);
        if (!item.children.length) {
          delete item.children
        }
      }
      return item;
    });
}

@Component({
  components: {
  },
})
export default class RoleManage extends Vue {
  @UserModel.State private permitTree!: any[];
  @UserModel.State private userInfo!: any;
  @UserModel.Getter private getUserRoleList!: any[];

  public $refs!: {
    roleForm: Form;
    permisForm: Form;
    tree: Tree;
    listTable: any;
  };

  private isLoading: boolean = false;

  private filterListInit: any[] = [
    {
      field: 'name',
      label: i18n.t('modules.views.sysManage.role.s_10a6f121') as string, labelKey: 'modules.views.sysManage.role.s_10a6f121',
      type: 'input',
      children: [],
    },
    {
      field: 'desc',
      label: i18n.t('modules.views.sysManage.role.s_9036c224') as string, labelKey: 'modules.views.sysManage.role.s_9036c224',
      type: 'input',
      children: [],
    },
  ];

  private filterList: any[] = [];

  private queryParams = {
    name: '',
    desc: '',
  }

  private allRoleList: any[] = [];
  private roleList: any[] = [];
  private roleListLoading = false;
  get roleListTotal () {
    let count = 0;
    const traverse = (list: any[]) => {
      list.forEach(item => {
        count++;
        if (item.children && item.children.length > 0) {
          traverse(item.children);
        }
      });
    }
    traverse(this.roleList);
    return count;
  }

  private currRole: any = {};

  private saving: boolean = false;

  private showRoleModal: boolean = false;

  private isEditRole: boolean = false;

  // 是否编辑Admin角色
  get canChoosedFullGroup () {
    return this.currRole?.name === 'Administrator'
  }
  get administratorId () {
    return this.allRoleList.find(item => item.name === 'Administrator')?.id;
  }

  private roleForm: any = {
    roleName: '',
    defineType: i18n.t('modules.views.appMonitor.service.s_f1d4ff50') as string,
    description: '',
    pid: []
  };
  get roleRules(): any {
    const validateInputText = (rule: any, value: string, callback: any) => {
      if (!value.trim()) {
        callback(new Error(rule.message || i18n.t('modules.views.configInstall.dataAccess.s_02cc4f8f') as string));
      } else {
        callback();
      }
    };
    return {
      roleName: [
        { required: true, validator: validateInputText, message: i18n.t('modules.views.sysManage.role.s_2a92f526') as string, messageKey: 'modules.views.sysManage.role.s_2a92f526', trigger: 'blur' },
        { required: true, validator: validateInputText, message: i18n.t('modules.views.sysManage.role.s_2a92f526') as string, messageKey: 'modules.views.sysManage.role.s_2a92f526', trigger: 'change' },
        { min: 4, max: 16, message: i18n.t('modules.views.sysManage.role.s_f9645c2b') as string, messageKey: 'modules.views.sysManage.role.s_f9645c2b', trigger: 'blur' },
        { min: 4, max: 16, message: i18n.t('modules.views.sysManage.role.s_f9645c2b') as string, messageKey: 'modules.views.sysManage.role.s_f9645c2b', trigger: 'change' },
      ],
    }
  }

  private showPermisModal: boolean = false;

  private isViewPermis: boolean = false;

  private permisForm: any = {
    roleid: 0,
    permis: [],
  };

  private permisCheckAll = false; 
  private isIndeterminateAll = false;

  private permisList: any[] = [];
  private checkedPermisIdList: number[] = [];
  private limitPermisList: number[] = [];
  private parentIdList: number[] = [];
  private leafIdList: number[] = [];

  private queryApi = SystemApi.getRoleList;
  private groupEnabled = false;

  private allColumnConfig: any[] = [
    { field: 'name', label: i18n.t('modules.views.sysManage.role.s_10a6f121') as string, labelKey: 'modules.views.sysManage.role.s_10a6f121', minWidth: 200 },
    { field: 'description', label: i18n.t('modules.views.sysManage.role.s_9036c224') as string, labelKey: 'modules.views.sysManage.role.s_9036c224', minWidth: 200 },
    { field: 'defineType', label: i18n.t('modules.views.sysManage.group.s_76e1a31b') as string, labelKey: 'modules.views.sysManage.group.s_76e1a31b', minWidth: 80 },
  ];
  get columnConfig () {
    return this.allColumnConfig;
  }

  private fullGroupList: any[] = []
  private groupList: any[] = []
  private groupLoading = false;

  private fullGroupModel = {
    name: i18n.t('modules.views.sysManage.role.s_f068b6e4') as string, nameKey: 'modules.views.sysManage.role.s_f068b6e4',
    checked: false,
    dataAuth: true,
    configAuth: true
  }

  private roleOptions: any[] = [];
  private roleIdPathMap: { [id: number]: number[] } = {}
  get roleOptionList () {
    if (!this.currRole) {
      return this.roleOptions
    }
    return filterOptionsById(deepClone(this.roleOptions), this.currRole.id)
  }

  get accountUserGroupList () {
    return this.userInfo && this.userInfo.userGroupList || []
  }

  get isCurrentChooseAdminRole () {
    return this.currRole?.name === 'Administrator'
  }

  private async created() {
    const type = this.$route.query.type
    if (type === 'create') {
      this.toggleRoleModalHandle(true)
      this.$router.replace(this.$route.path)
    }
    await this.getEnableStatus();
    if (this.groupEnabled) {
      this.getFullGroupList();
    }

    // 搜索参数回显
    const routerQuery = this.$route.query
    const query: any = { ...this.queryParams }
    Object.keys(query).forEach(k => {
      if (routerQuery[k]) {
        query[k] = decodeURIComponent(String(routerQuery[k]))
      }
    });
    this.queryParams = query;

    this.filterList = [...this.filterListInit];

    this.getRoleList();
  }

  private beforeDestroy () {
    //
  }

  private async getEnableStatus () {
    const { result, error } = await toAsyncWait(GroupApi.getGroupStatus({}))
    if (!error) {
      const { data = 0 } = result || {}
      if (data) {
        this.groupEnabled = true
      }
    }
  }

  private toggleRoleModalHandle(show?: boolean, data?: any) {
    if (show) {
      this.isEditRole = !!data;
      data = data || {};
      this.currRole = data;
      this.roleForm = {
        roleName: data.name || '',
        defineType: data.defineType || '自定义',
        description: data.description || '',
        pid: []
      };
      if (data && data.id) {
        const idPath = [...this.roleIdPathMap[this.currRole.id]];
        idPath.splice(-1, 1);
        this.roleForm.pid = idPath;
      }
      this.showRoleModal = true;
    } else {
      this.roleForm = {
        roleName: '',
        defineType: i18n.t('modules.views.appMonitor.service.s_f1d4ff50') as string,
        description: '',
      }
      this.$refs.roleForm.resetFields();
      this.isEditRole = false;
      this.currRole = {};
      this.showRoleModal = false;
    }
  }

  private async deleteRoleHandle(row: any, index: number) {
    this.$confirm(i18n.t('modules.views.sysManage.role.s_9f6618ba', { value0: row.name, value1: row.description }) as string, i18n.t('modules.views.sysManage.role.s_31d48c75') as string, {
      type: 'warning',
      dangerouslyUseHTMLString: true,
    }).then(() => {
      this.isLoading = true;
      const params: any = {
        roleid: row.id,
      };
      SystemApi.deleteRole(params)
        .then(async (rst: any) => {
          this.isLoading = false;
          if (rst && rst.status === 200) {
            this.$message.success(i18n.t('modules.views.aiPlatform.experts.s_0007d170') as string);
            this.getRoleList();
            const roleIds = (this.userInfo.roleid || '').split(',').map((t: string) => +t);
            if (roleIds.includes(params.roleid)) {
              // 当前用户的角色权限改变，刷新页面，重置路由和菜单权限
              setTimeout(() => {
                window.location.reload()
              }, 500)
              return
            }
            // 更新当前用户的管理域列表
            if (this.groupEnabled) {
              await this.$store.dispatch('User/findRoleGroupByUser');
            }
          } else {
            throw new Error(rst.message);
          }
        })
        .catch((err) => {
          this.isLoading = false;
          if (err.message !== 'interrupt') {
            this.$message.error(err.message);
          }
        });
    }).catch(() => null);
  }

  private async togglePermisModalHandle(show?: boolean, data?: any) {
    if (show) {
      data = data || {};
      this.permisForm.roleid = data.id;
      this.isViewPermis = data.defineType !== i18n.t('modules.views.appMonitor.service.s_f1d4ff50') as string;
      this.checkedPermisIdList = [];
      // console.log(data)
      // 获取父级授权范围
      this.limitPermisList = await this.getPermisByPid(data?.pid || 1);
      this.initPermisTree(data?.pid);
      this.getPermis(data.id);
      this.showPermisModal = true;
    } else {
      this.$refs.permisForm.resetFields();
      this.isViewPermis = false;
      this.showPermisModal = false;
    }
  }

  private saveRoleForm(): void {
    this.$refs.roleForm.validate((valid: boolean) => {
      if (valid) {
        if (this.saving) {
          return;
        }
        this.saving = true;
        if (this.isEditRole) {
          this.editRole();
        } else {
          this.createRole();
        }
      }
    });
  }

  private async createRole() {
    this.saving = true;
    const params = { ...this.roleForm };
    params.pid = Array.isArray(params.pid) && params.pid.length ? Array.from(params.pid).reverse()[0] : 0;
    const { error, result } = await toAsyncWait(SystemApi.createRole(params));
    if (!error) {
      this.toggleRoleModalHandle();
      this.$message.success(i18n.t('modules.views.dataReport.report.s_e477bf35') as string);
      this.getRoleList();
      // 更新当前用户的管理域列表
      if (this.groupEnabled) {
        await this.$store.dispatch('User/findRoleGroupByUser');
      }
    } else {
      this.$message.error(error?.message || i18n.t('modules.views.sysManage.role.s_c71e1b10') as string);

    }
    this.saving = false;
  }

  private async editRole() {
    const params: any = {
      ...this.roleForm,
      roleid: this.currRole.id,
    };
    params.pid = Array.isArray(params.pid) && params.pid.length ? Array.from(params.pid).reverse()[0] : 0;
    const { error, result } = await toAsyncWait(SystemApi.editRole(params));
    if (!error) {
      this.$message.success(i18n.t('modules.views.appMonitor.service.s_55aa6366') as string);
      this.toggleRoleModalHandle();
      this.getRoleList();
      // 更新当前用户的管理域列表
      if (this.groupEnabled) {
        await this.$store.dispatch('User/findRoleGroupByUser');
      }
    } else {
      this.$message.error(error?.message || i18n.t('modules.views.appMonitor.serviceAnalysis.s_930442e2') as string);
    }
    this.saving = false;
  }

  private getPermis(id: number) {
    const params: any = {
      roleid: id,
    };
    SystemApi.getPermis(params)
      .then((rst: any) => {
        if (rst && rst.status === 200 && rst.data) {
          this.permisForm.permis = rst.data.permis || [];
          this.checkedPermisIdList = this.permisForm.permis.filter((t: number) => this.leafIdList.includes(t));
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

  private async getPermisByPid (pid: number) {
    const params: any = {
      roleid: pid,
    };
    const { error, result } = await toAsyncWait(SystemApi.getPermis(params));
    if (!error) {
      return result?.data?.permis
    } else {
      return []
    }
  }

  // 初始化权限树，父节点ids、叶子节点ids
  private initPermisTree(pid?: number) {
    const parentIds: number[] = [];
    const leafIds: number[] = [];
    const formatTree = (data: any[]) => {
      const tree: any[] = [];
      data.filter((item) => ![i18n.t('modules.views.sysManage.role.s_eab5b13a') as string, i18n.t('modules.views.sysManage.role.s_9e53fc91') as string].includes(item.name)).forEach((item) => {
        const tItem: any = {
          id: item.id,
          label: item.name,
          disabled: !pid ? this.isViewPermis : !this.limitPermisList.find((p) => p === item.id),
          children: (item.children || []).length ? formatTree(item.children || []) : [],
        }
        if (tItem.children.length) {
          parentIds.push(tItem.id);
        } else {
          leafIds.push(tItem.id);
          delete tItem.children;
        }
        tree.push(tItem);
      });
      return tree;
    };
    this.permisList = formatTree(this.permitTree);
    this.parentIdList = parentIds;
    this.leafIdList = leafIds;
    // 过滤全部权限中不存在的ID
    this.limitPermisList = this.limitPermisList.filter(id => [...parentIds, ...leafIds].includes(id));
    this.permisCheckAll = false;
    this.isIndeterminateAll = false;
  }

  // 全选
  private permisCheckAllHandle (val: boolean) {
    this.permisForm.permis = val ? [
      ...this.limitPermisList,
    ] : [];
    this.$refs.tree.setCheckedKeys(this.permisForm.permis);
    this.isIndeterminateAll = false;
  }

  private checkPermisChange(): void {
    // 获取已选和半选的数据
    const checkedData: any[] = this.$refs.tree.getCheckedNodes(false, true);
    this.permisForm.permis = checkedData.map((t: any) => t.id);
    const checkedLeng = this.permisForm.permis.length
    // const allLeng = this.parentIdList.length + this.leafIdList.length
    const allLeng = this.limitPermisList.length
    this.permisCheckAll = checkedLeng === allLeng;
    this.isIndeterminateAll = checkedLeng > 0 && checkedLeng < allLeng;
  }

  private savePermisForm(): void {
    this.$refs.permisForm.validate((valid: boolean) => {
      if (valid) {
        if (this.saving) {
          return;
        }
        this.saving = true;
        const params: any = {
          roleid: this.permisForm.roleid,
          permis: this.permisForm.permis.join(','),
        };
        SystemApi.updatePermis(params)
          .then((rst: any) => {
            this.saving = false;
            if (rst && rst.status === 200) {
              this.$message.success(i18n.t('modules.views.appMonitor.service.s_55aa6366') as string);
              const roleIds = (this.userInfo.roleid || '').split(',').map((t: string) => +t)
              if (roleIds.includes(params.roleid)) {
                // 当前用户的角色权限改变，刷新页面，重置路由和菜单权限
                setTimeout(() => {
                  window.location.reload()
                }, 500)
                return
              }
              this.togglePermisModalHandle();
            } else {
              throw new Error(rst.message);
            }
          })
          .catch((err) => {
            this.saving = false;
            if (err.message !== 'interrupt') {
              this.$message.error(err.message);
            }
          });
      }
    });
  }

  private tableInitedHandle () {
    this.$refs.listTable.refresh();
  }

  private showGroupModal = false;
  private postGroupLoading = false;
  // 打开/关闭 角色数据权限弹框
  private async toggleGroupModalHandle (show?: boolean, data?: any) {
    if (show) {
      data = data || {};
      this.showGroupModal = true;
      this.currRole = data;
      await this.getGroupListByRoleId(data)
    } else {
      this.showGroupModal = false;
      this.currRole = {};
    }
  }
  // 保存角色管理域绑定
  private async saveGroupConfig () {
    const params: any[] = this.groupList.filter((g) => g.checked).map((g) => {
      return {
        roleId: this.currRole.id,
        gid: g.id,
        dataAuth: true,
        configAuth: g.configAuth,
      }
    });
    this.postGroupLoading = true;
    if (params.length === 0 && this.userInfo?.currentRole?.roleId) {
      const { error, result } = await toAsyncWait(GroupApi.unbindRoleGroup({ roleId: this.userInfo?.currentRole?.roleId }))
      if (!error) {
        this.$message.success(i18n.t('modules.views.appMonitor.service.s_55aa6366') as string);
        // this.getFullGroupList();
        // 更新当前用户的管理域列表
        if (this.groupEnabled) {
          await this.$store.dispatch('User/findRoleGroupByUser');
        }
        this.$refs.listTable.refresh();
        this.toggleGroupModalHandle();
      } else {
        if (error.message !== 'interrupt') {
          this.$message.error(error.message || i18n.t('modules.views.appMonitor.serviceAnalysis.s_930442e2') as string);
        }
      }
      this.postGroupLoading = false;
      return
    }
    GroupApi.bindRoleGroup(params)
      .then(async (rst: any) => {
        this.postGroupLoading = false;
        if (rst && rst.status === 200) {
          this.$message.success(i18n.t('modules.views.appMonitor.service.s_55aa6366') as string);
          this.getFullGroupList();
          // 更新当前用户的管理域列表
          if (this.groupEnabled) {
            await this.$store.dispatch('User/findRoleGroupByUser');
          }
          this.$refs.listTable.refresh();
          this.toggleGroupModalHandle();
        } else {
          throw new Error(rst.message);
        }
      })
      .catch((err) => {
        this.postGroupLoading = false;
        if (err.message !== 'interrupt') {
          this.$message.error(err.message);
        }
      });
  }

  private async getGroupListByRoleId (role: any) {
    if (!role) {
      return;
    }
    this.groupLoading = true
    const { pid, id } = role || {};
    const groupList = deepClone(this.fullGroupList);
    let enabledParentGroupIds: number[] = [];
    const enabledParentGroupMap: any = {};
    // 获取父级分配的权限
    if (pid === 1) {
      enabledParentGroupIds = groupList.map((g) => g.id);
      groupList.forEach(g => {
        g.disable = false
      })
    } else if (pid !== 0) {
      const { result: presult, error: perror } = await toAsyncWait(GroupApi.getGroupListByRole(pid))
      if (!perror) {
        const { data = [] } = presult || {};
        if (Array.isArray(data)) {
          enabledParentGroupIds = data.map((i) => {
            enabledParentGroupMap[i.gid] = i;
            return i.gid;
          })
          groupList.forEach(g => {
            g.disable = !(enabledParentGroupIds.includes(g.id))
          })
        }
      }
    } else {
      groupList.forEach(g => {
        g.disable = false
      });
    }
    let enabledGroupIds: number[] = [];
    const enabledGroupMap: any = {};
    // 获取自身分配的权限
    const { result, error } = await toAsyncWait(GroupApi.getGroupListByRole(id))
    if (!error) {
      const { data = [] } = result || {};
      if (Array.isArray(data)) {
        enabledGroupIds = data.map((i) => {
          enabledGroupMap[i.gid] = i;
          return i.gid
        });
      }
    }
    groupList.forEach((g) => {
      const isEnabled = enabledGroupIds.includes(g.id);

      if (isEnabled) {
        g.checked = true;
        g.dataAuth = true;
        g.configAuth = enabledGroupMap[g.id].configAuth;
      }
      if (pid) {
        g.dataDisable = true;
        g.configDisable = pid === 1 ? false : (!enabledParentGroupMap[g.id] || !enabledParentGroupMap[g.id].configAuth);
      }
    });
    if (role.id === 1 && role.name === 'Administrator') {
      if (enabledGroupIds.length === 0) {
        this.fullGroupModel.checked = true
        this.fullGroupModel.configAuth = true
      }
    } else {
      this.fullGroupModel.checked = false
    }
    this.groupList = [...groupList]
    this.groupLoading = false;
  }

  private async getFullGroupList () {
    this.groupLoading = true
    const { result, error } = await toAsyncWait(GroupApi.getGroupList({}))
    if (!error) {
      const { data = [] } = result || {};
      this.fullGroupList = data.map((i: any) => {
        return {
          ...i,
          checked: false,
          dataAuth: true,
          configAuth: false,
          disable: true,
          dataDisable: false,
          configDisable: false,
        }
      })
    }
    this.groupLoading = false;
  }

  private pidChangeHandle () {
    //
  }

  private async getRoleList () {
    this.roleListLoading = true
    const params: any = {
      pageNum: 1,
      pageSize: 9999
    }
    const { error, result } = await toAsyncWait(SystemApi.getRoleList(params))
    if (!error) {
      const data = result?.data || [];
      if (Array.isArray(data)) {
        const formatList = (list: any[]) => {
          return list.map((t: any) => {
            if (t.children?.length) {
              t.children = formatList(t.children);
            }
            return t;
          })
        }
        this.allRoleList = formatList(data);
        this.searchChangeHandle();

        this.roleOptions = deepClone(data);

        const _data = deepClone(data || []);
        
        const traverse = (nodes: any[], _idPath: number[]) => {
          if (nodes.length) {
            nodes.forEach((n) => {
              n._idPath = [..._idPath, n.id];
              this.roleIdPathMap[n.id] = [...n._idPath]
              if (Array.isArray(n.children)) {
                traverse(n.children, n._idPath);
              }
            })
          }
        };
        traverse(_data, []);

      }
    }
    this.roleListLoading = false;
  }

  private searchChangeHandle () {
    const name = (this.queryParams.name || '').toLocaleLowerCase();
    const desc = (this.queryParams.desc || '').toLocaleLowerCase();
    if (name || desc) {
      const matchFun = (n: string, d: string) => {
        const _n = (n || '').toLocaleLowerCase();
        const _d = (d || '').toLocaleLowerCase();
        return (!!name && _n.indexOf(name) !== -1) || (!!desc && _d.indexOf(desc) !== -1)
      }
      const filterListFun = (list: any[]) => {
        const _list: any[] = []
        list.forEach(item => {
          let children: any[] = [];
          if (item.children && item.children.length) {
            children = filterListFun(item.children);
          }
          if (matchFun(item.name, item.description) || children.length) {
            _list.push({ ...item, children });
          }
        })
        return _list
      }
      this.roleList = filterListFun(this.allRoleList)
    } else {
      this.roleList = [...this.allRoleList]
    }
  }

  // 全屏遮罩层
  private getFullScreenMask (text?: string) {
    return this.$loading({
      lock: true,
      text,
      background: 'rgba(18,19,23,.5)'
    });
  }
}
</script>

<style lang="scss" scoped>
.user-role-cont {
  flex: 1;
  height: 100%;
  display: flex;
  flex-direction: column;
  color: var(--color-text-regular);

  .role-list {
    height: calc(100% - 32px);
  }
}

.form-box {
  :deep(.el-input.is-disabled .el-input__inner) {
    color: inherit;
  }

  .form-input {
    width: 100%;
  }

  .tree {
    max-height: 400px;
    overflow: auto;
    line-height: 21px;
  }

  .checkall-item {
    margin-bottom: 10px;
    :deep(.el-form-item__content) {
      line-height: 26px;
    }
    .check-all {
      padding-left: 24px;
      &:hover {
        background-color: var(--background-color-base);
      }
    }
  }
}

.role-group-dialog {
  :deep(.el-dialog__body) {
    max-height: calc(100vh - 126px);
    overflow: auto;
  }
}

.group-list-wrapper {
  :deep(.group-list-item .el-checkbox__label) {
    font-size: 13px;
  }
  :deep(.group-list-item .group-checkbox .el-checkbox__label) {
    max-width: 410px;
    text-overflow: ellipsis;
    white-space: nowrap;
    overflow: hidden;
    vertical-align: middle;
  }
}
</style>
