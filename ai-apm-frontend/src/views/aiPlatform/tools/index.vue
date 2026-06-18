<template>
  <ai-platform-page
    :title="$t('modules.views.aiPlatform.tools.s_5f7df379')"
    :subtitle="$t('modules.views.aiPlatform.tools.s_e45a9757')"
    icon="el-icon-s-tools"
    :loading="loading"
  >
    <template slot="stats">
      <stat-pill :label="$t('modules.views.aiPlatform.tools.s_be7fc8d9')" :value="allRows.length" accent />
      <stat-pill :label="$t('modules.views.aiPlatform.tools.s_64729245')" :value="localRows.length" />
      <stat-pill :label="$t('modules.views.aiPlatform.tools.s_9a7ba233')" :value="mcpRows.length" />
      <stat-pill :label="$t('modules.views.help.startGuide.s_53ace430')" :value="enabledCount" />
    </template>

    <template slot="toolbar">
      <div class="toolbar-left">
        <el-radio-group v-model="scopeFilter" size="small" class="segmented-filter scope-filter">
          <el-radio-button label="local">{{ $t('modules.views.aiPlatform.tools.s_64729245') }}</el-radio-button>
          <el-radio-button label="mcp">{{ $t('modules.views.aiPlatform.tools.s_9a7ba233') }}</el-radio-button>
        </el-radio-group>
        <el-button
          v-if="scopeFilter === 'mcp'"
          type="primary"
          size="small"
          icon="el-icon-plus"
          class="ml-10"
          @click="openEditor()"
        >{{ $t('modules.views.aiPlatform.tools.s_b71853a8') }}</el-button>
      </div>
      <div class="toolbar-right">
        <el-input
          v-model="keyword"
          size="small"
          clearable
          prefix-icon="el-icon-search"
          class="filter-input"
          :placeholder="scopeFilter === 'mcp' ? $t('modules.views.aiPlatform.tools.s_ac9f9b9e') : $t('modules.views.aiPlatform.tools.s_31234ee7')"
        />
        <el-radio-group v-model="quickFilter" size="small" class="segmented-filter">
          <el-radio-button label="all">{{ $t('modules.views.aiPlatform.experts.s_a8b0c204') }}</el-radio-button>
          <el-radio-button label="enabled">{{ $t('modules.views.help.startGuide.s_7854b52a') }}</el-radio-button>
          <el-radio-button label="custom">{{ $t('modules.views.aiPlatform.skills.s_f1d4ff50') }}</el-radio-button>
        </el-radio-group>
      </div>
    </template>

    <div class="resource-board">
      <aside class="resource-rail">
        <div class="rail-title">{{ scopeFilter === 'mcp' ? $t('modules.views.aiPlatform.tools.s_16a4a30a') : $t('modules.views.aiPlatform.tools.s_f8365403')  }}</div>
        <button
          v-for="item in railFilters"
          :key="item.value"
          type="button"
          :class="['rail-item', { 'is-active': categoryFilter === item.value }]"
          @click="categoryFilter = item.value"
        >
          <span>{{ item.labelKey ? $t(item.labelKey) : item.label }}</span>
          <span class="rail-count">{{ item.count }}</span>
        </button>
      </aside>
      <section class="resource-main">
        <div class="table-meta-bar">
          <div>
            <div class="table-title">{{ scopeTitle }}</div>
            <div class="table-desc">{{ scopeDesc }}</div>
          </div>
        </div>
        <div v-if="groupedRows.length" class="category-stack">
          <section v-for="group in groupedRows" :key="group.category" class="category-section">
            <div class="category-head">
              <div>
                <div class="category-title">{{ group.category }}</div>
                <div class="category-desc">{{ scopeCategoryDesc }}</div>
              </div>
              <span class="category-count">{{ group.rows.length }}</span>
            </div>
            <el-table :data="group.rows" class="resource-table category-table">
              <el-table-column prop="toolId" :label="scopeFilter === 'mcp' ? 'MCP Tool' : 'Tool'" min-width="230">
                <template slot-scope="{ row }">
                  <div class="primary-cell">
                    <span class="mono-text">{{ row.toolId }}</span>
                    <div class="primary-name">{{ row.nameKey ? $t(row.nameKey) : row.name }}</div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column
                v-if="scopeFilter === 'local'"
                :label="$t('modules.views.aiPlatform.tools.s_b3025625')"
                min-width="280"
                show-overflow-tooltip
              >
                <template slot-scope="{ row }">
                  <span class="mono-text uri-cell">{{ row.implementation || '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column
                v-if="scopeFilter === 'mcp'"
                :label="$t('modules.views.aiPlatform.tools.s_ee24ae97')"
                min-width="300"
                show-overflow-tooltip
              >
                <template slot-scope="{ row }">
                  <span class="mono-text uri-cell">{{ mcpEndpoint(row) || '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column
                :label="scopeFilter === 'mcp' ? $t('modules.views.aiPlatform.tools.s_faa1ad5e') : $t('modules.views.aiPlatform.tools.s_226b0912')"
                width="150"
              >
                <template slot-scope="{ row }">
                  <span class="source-tag">{{ scopeFilter === 'mcp' ? mcpTransport(row) : toolTypeLabel(row) }}</span>
                </template>
              </el-table-column>
              <el-table-column :label="$t('modules.views.aiPlatform.experts.s_3fea7ca7')" width="100">
                <template slot-scope="{ row }">
                  <span :class="['status-tag', row.enabled ? 'is-on' : 'is-off']">{{ row.enabled ? $t('modules.views.aiPlatform.experts.s_7854b52a') : $t('modules.views.aiPlatform.experts.s_5c56a889')  }}</span>
                </template>
              </el-table-column>
              <el-table-column :label="$t('modules.views.aiPlatform.skills.s_26ca20b1')" width="90">
                <template slot-scope="{ row }">
                  <span class="source-tag">{{ row.builtIn ? $t('modules.views.aiPlatform.skills.s_89e180d6') : $t('modules.views.aiPlatform.skills.s_f1d4ff50')  }}</span>
                </template>
              </el-table-column>
              <el-table-column :label="$t('modules.views.aiPlatform.skills.s_3b61c966')" width="90">
                <template slot-scope="{ row }">
                  <span class="action-link" @click="showReferences(row)">{{ $t('modules.views.metrics.list.s_607e7a4f') }}</span>
                </template>
              </el-table-column>
              <el-table-column :label="$t('modules.views.aiPlatform.experts.s_2b6bc0f2')" width="180" fixed="right">
                <template slot-scope="{ row }">
                  <span v-if="scopeFilter === 'mcp'" class="action-link" @click="openEditor(row)">{{ $t('modules.views.hide.advancedConfig.s_95b351c8') }}</span>
                  <span class="action-link" @click="toggleEnable(row)">{{ row.enabled ? $t('modules.views.aiPlatform.experts.s_5c56a889') : $t('modules.views.aiPlatform.experts.s_7854b52a')  }}</span>
                  <span v-if="!row.builtIn" class="action-link danger" @click="removeTool(row)">{{ $t('modules.views.hide.advancedConfig.s_2f4aaddd') }}</span>
                </template>
              </el-table-column>
            </el-table>
          </section>
        </div>
        <div v-else class="category-empty">{{ scopeEmptyText }}</div>
      </section>
    </div>

    <el-drawer
      :visible.sync="drawerVisible"
      :title="editorTitle"
      size="560px"
      :wrapper-closable="false"
      custom-class="ai-platform-drawer"
    >
      <div class="drawer-content">
        <el-form ref="toolForm" :model="form" :rules="rules" label-width="96px" size="small" class="platform-form">
          <div class="form-section">
            <div class="form-section-head">
              <div class="form-section-title">{{ $t('modules.views.aiPlatform.skills.s_6ea1fe6b') }}</div>
              <div class="form-section-desc">{{ $t('modules.views.aiPlatform.tools.s_0718ccb8') }}</div>
            </div>
            <div class="form-grid">
              <el-form-item label="Tool ID" prop="toolId">
                <el-input v-model="form.toolId" :disabled="!!editingId" maxlength="64" />
              </el-form-item>
              <el-form-item :label="$t('modules.views.aiPlatform.experts.s_d7ec2d3f')" prop="name">
                <el-input v-model="form.name" maxlength="64" />
              </el-form-item>
              <el-form-item :label="$t('modules.views.aiPlatform.experts.s_d0771a42')">
                <el-input v-model="form.category" maxlength="32" :placeholder="$t('modules.views.aiPlatform.tools.s_8947f5fe')" />
              </el-form-item>
              <el-form-item :label="$t('modules.views.configInstall.apm.s_3bdd08ad')" class="is-wide">
                <el-input v-model="form.description" type="textarea" :rows="3" :placeholder="$t('modules.views.aiPlatform.tools.s_06eb64b8')" />
              </el-form-item>
              <el-form-item :label="$t('modules.views.aiPlatform.experts.s_3fea7ca7')">
                <el-switch v-model="form.enabled" :active-text="$t('modules.views.help.startGuide.s_7854b52a')" :inactive-text="$t('modules.views.aiPlatform.experts.s_5c56a889')" />
              </el-form-item>
            </div>
          </div>
          <div class="form-section">
            <div class="form-section-head">
              <div class="form-section-title">{{ $t('modules.views.aiPlatform.tools.s_c5013939') }}</div>
              <div class="form-section-desc">{{ $t('modules.views.aiPlatform.tools.s_e876c4b4') }}</div>
            </div>
            <el-form-item :label="$t('modules.views.aiPlatform.experts.s_226b0912')">
              <el-input value="MCP" disabled />
            </el-form-item>
            <el-form-item :label="$t('modules.views.aiPlatform.tools.s_f562f75c')" prop="mcpEndpoint">
              <el-input
                v-model="form.mcpEndpoint"
                :disabled="!!editingBuiltIn"
                :placeholder="$t('modules.views.aiPlatform.tools.s_f101c54f')"
              />
            </el-form-item>
            <el-form-item :label="$t('modules.views.aiPlatform.tools.s_1de6a73a')" prop="mcpTransport">
              <el-select v-model="form.mcpTransport" :disabled="!!editingBuiltIn" class="full-width">
                <el-option label="SSE" value="SSE" />
                <el-option label="Streamable HTTP" value="STREAMABLE_HTTP" />
              </el-select>
            </el-form-item>
            <el-form-item :label="$t('modules.views.aiPlatform.tools.s_be47bd27')" class="is-wide">
              <el-input
                v-model="form.mcpHeaders"
                :disabled="!!editingBuiltIn"
                class="code-textarea"
                type="textarea"
                :rows="4"
                :placeholder="$t('modules.views.aiPlatform.tools.s_48a8463c')"
              />
            </el-form-item>
            <el-form-item :label="$t('modules.views.aiPlatform.tools.s_f564cdc3')" class="is-wide">
              <el-input
                v-model="form.schemaJson"
                :disabled="!!editingBuiltIn"
                class="code-textarea"
                type="textarea"
                :rows="6"
                :placeholder="$t('modules.views.aiPlatform.tools.s_3f98ca7d')"
              />
            </el-form-item>
            <el-form-item v-if="referenceExperts.length" :label="$t('modules.views.aiPlatform.skills.s_bd801c8f')">
              <el-tag v-for="id in referenceExperts" :key="id" size="mini" class="mr-6">{{ id }}</el-tag>
            </el-form-item>
          </div>
        </el-form>
      </div>
      <div class="drawer-footer">
        <el-button size="small" @click="drawerVisible = false">{{ $t('modules.views.aiPlatform.experts.s_625fb26b') }}</el-button>
        <el-button type="primary" size="small" :loading="saving" @click="saveTool">{{ $t('modules.views.configInstall.plugin.s_be5fbbe3') }}</el-button>
      </div>
    </el-drawer>
  </ai-platform-page>
</template>

<script lang="ts">
import { Vue, Component, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import { Form } from 'element-ui';
import AiPlatformPage from '../components/AiPlatformPage.vue';
import StatPill from '../components/stat-pill.vue';
import AiPlatformApi, { AiToolDefinition } from '@/api/aiPlatform';
import { toAsyncWait } from '@/utils/common';

@Component({
  components: { AiPlatformPage, StatPill },
})
export default class AiPlatformTools extends Vue {
  public $refs!: { toolForm: Form }

  private loading = false
  private saving = false
  private drawerVisible = false
  private editingId = ''
  private editingBuiltIn = false
  private referenceExperts: string[] = []
  private keyword = ''
  private quickFilter = 'all'
  private categoryFilter = 'all'
  private scopeFilter: 'local' | 'mcp' = 'local'
  private allRows: AiToolDefinition[] = []
  private form: any = this.emptyForm()

  private rules = {
    toolId: [{ required: true, message: i18n.t('modules.views.aiPlatform.tools.s_46e095ef') as string, messageKey: 'modules.views.aiPlatform.tools.s_46e095ef', trigger: 'blur' }],
    name: [{ required: true, message: i18n.t('modules.views.aiPlatform.experts.s_06e2f88f') as string, messageKey: 'modules.views.aiPlatform.experts.s_06e2f88f', trigger: 'blur' }],
    mcpEndpoint: [{ required: true, message: i18n.t('modules.views.aiPlatform.tools.s_8a77c519') as string, messageKey: 'modules.views.aiPlatform.tools.s_8a77c519', trigger: 'blur' }],
    mcpTransport: [{ required: true, message: i18n.t('modules.views.aiPlatform.tools.s_8f1f134f') as string, messageKey: 'modules.views.aiPlatform.tools.s_8f1f134f', trigger: 'change' }],
  }

  @Watch('scopeFilter')
  private onScopeFilterChange () {
    this.categoryFilter = 'all'
    this.keyword = ''
    this.quickFilter = 'all'
  }

  get editorTitle () {
    return this.editingId ? i18n.t('modules.views.aiPlatform.tools.s_ce1d1fb0') as string : i18n.t('modules.views.aiPlatform.tools.s_538c76a3') as string
  }

  get localRows () {
    return this.allRows.filter(item => this.isLocalTool(item))
  }

  get mcpRows () {
    return this.allRows.filter(item => this.isMcpTool(item))
  }

  get scopeRows () {
    return this.scopeFilter === 'mcp' ? this.mcpRows : this.localRows
  }

  get enabledCount () {
    return this.allRows.filter(item => item.enabled).length
  }

  get scopeTitle () {
    return this.scopeFilter === 'mcp' ? i18n.t('modules.views.aiPlatform.tools.s_82c7ffc9') as string : i18n.t('modules.views.aiPlatform.tools.s_dc01a63c') as string
  }

  get scopeDesc () {
    const total = this.scopeRows.length
    const visible = this.filteredRows.length
    if (this.scopeFilter === 'mcp') {
      return i18n.t('modules.views.aiPlatform.tools.s_ba61a594', { value0: visible, value1: total }) as string
    }
    return i18n.t('modules.views.aiPlatform.tools.s_c7c055a4', { value0: visible, value1: total }) as string
  }

  get scopeCategoryDesc () {
    return this.scopeFilter === 'mcp' ? i18n.t('modules.views.aiPlatform.tools.s_feeba3f5') as string : i18n.t('modules.views.aiPlatform.tools.s_04fcfed1') as string
  }

  get scopeEmptyText () {
    return this.scopeFilter === 'mcp' ? i18n.t('modules.views.aiPlatform.tools.s_9881b096') as string : i18n.t('modules.views.aiPlatform.tools.s_1e595968') as string
  }

  get railFilters () {
    const categories = this.groupRows(this.scopeRows)
    return [{ label: i18n.t('modules.views.aiPlatform.experts.s_1a750305') as string, labelKey: 'modules.views.aiPlatform.experts.s_1a750305', value: 'all', count: this.scopeRows.length }]
      .concat(categories.map(group => ({ label: group.category, value: group.category, count: group.rows.length })))
  }

  get filteredRows () {
    const keyword = this.keyword.trim().toLowerCase()
    return this.scopeRows.filter((item) => {
      const matchesFilter = this.quickFilter === 'all'
        || (this.quickFilter === 'enabled' && item.enabled)
        || (this.quickFilter === 'disabled' && !item.enabled)
        || (this.quickFilter === 'builtIn' && item.builtIn)
        || (this.quickFilter === 'custom' && !item.builtIn)
      const matchesCategory = this.categoryFilter === 'all' || this.categoryOf(item) === this.categoryFilter
      if (!matchesFilter || !matchesCategory) {
        return false
      }
      if (!keyword) {
        return true
      }
      return [
        item.toolId,
        item.name,
        item.description,
        item.type,
        item.implementation,
        item.configJson,
        item.schemaJson,
        this.categoryOf(item),
        this.mcpEndpoint(item),
        this.mcpTransport(item),
      ]
        .some(text => String(text || '').toLowerCase().includes(keyword))
    })
  }

  get groupedRows () {
    return this.groupRows(this.filteredRows)
  }

  private created () {
    this.loadData()
  }

  private emptyForm () {
    return {
      toolId: '',
      name: '',
      category: i18n.t('modules.views.aiPlatform.experts.s_d4296303') as string, categoryKey: 'modules.views.aiPlatform.experts.s_d4296303',
      description: '',
      type: 'MCP',
      mcpEndpoint: '',
      mcpTransport: 'SSE',
      mcpHeaders: '{}',
      schemaJson: '{\n  "type": "object",\n  "properties": {}\n}',
      enabled: true,
    }
  }

  private async loadData () {
    this.loading = true
    const { result, error } = await toAsyncWait(AiPlatformApi.listTools(), false)
    this.loading = false
    if (!error) {
      this.allRows = result || []
    } else if (error.message !== 'interrupt') {
      this.$message.error(error.message)
    }
  }

  private openEditor (row?: AiToolDefinition) {
    this.editingId = row?.toolId || ''
    this.editingBuiltIn = !!row?.builtIn
    this.referenceExperts = []
    const config = row ? this.parseJson(row.configJson, {}) as any : {}
    this.form = row ? {
      toolId: row.toolId,
      name: row.name,
      category: this.categoryOf(row),
      description: row.description || '',
      type: 'MCP',
      mcpEndpoint: config.endpoint || row.implementation || '',
      mcpTransport: config.transport || 'SSE',
      mcpHeaders: this.formatJson(config.headers || {}),
      schemaJson: this.formatJson(this.parseJson(row.schemaJson, {})),
      enabled: row.enabled,
    } : this.emptyForm()
    this.drawerVisible = true
    if (row?.toolId) {
      this.loadReferences(row.toolId)
    }
  }

  private async loadReferences (toolId: string) {
    const { result, error } = await toAsyncWait(AiPlatformApi.toolReferences(toolId), false)
    if (!error) {
      this.referenceExperts = (result as any)?.expertIds || []
    }
  }

  private async showReferences (row: AiToolDefinition) {
    await this.loadReferences(row.toolId)
    const text = this.referenceExperts.length ? this.referenceExperts.join(', ') : i18n.t('modules.views.aiPlatform.skills.s_281fa986') as string
    const label = this.scopeFilter === 'mcp' ? 'MCP' : i18n.t('modules.views.aiPlatform.tools.s_8e7eca1f') as string
    this.$alert(text, i18n.t('modules.views.aiPlatform.tools.s_83a4df89', { value0: label, value1: row.toolId }) as string)
  }

  private saveTool () {
    this.$refs.toolForm.validate(async (valid) => {
      if (!valid) {
        return
      }
      const payload = this.buildPayload()
      if (!payload) {
        return
      }
      this.saving = true
      const action = this.editingId
        ? AiPlatformApi.updateTool(this.editingId, payload)
        : AiPlatformApi.createTool(payload)
      const { error } = await toAsyncWait(action, false)
      this.saving = false
      if (!error) {
        this.$message.success(i18n.t('modules.views.aiPlatform.experts.s_3b108349') as string)
        this.drawerVisible = false
        this.loadData()
      } else if (error.message !== 'interrupt') {
        this.$message.error(error.message)
      }
    })
  }

  private buildPayload (): Partial<AiToolDefinition> | null {
    const endpoint = String(this.form.mcpEndpoint || '').trim()
    const headers = this.parseJsonField(this.form.mcpHeaders, i18n.t('modules.views.aiPlatform.tools.s_be47bd27') as string)
    if (!headers) {
      return null
    }
    const schema = this.parseJsonField(this.form.schemaJson, i18n.t('modules.views.aiPlatform.tools.s_f564cdc3') as string)
    if (!schema) {
      return null
    }
    return {
      toolId: String(this.form.toolId || '').trim(),
      name: String(this.form.name || '').trim(),
      category: this.normalizedCategory(this.form.category),
      description: this.form.description || '',
      type: 'MCP',
      implementation: endpoint,
      schemaJson: this.formatJson(schema),
      configJson: this.formatJson({
        endpoint,
        transport: this.form.mcpTransport || 'SSE',
        category: this.normalizedCategory(this.form.category),
        headers,
      }),
      enabled: !!this.form.enabled,
    }
  }

  private parseJsonField (value: string, label: string): Record<string, unknown> | null {
    const text = String(value || '').trim() || '{}'
    const parsed = this.parseJson(text, null)
    if (!parsed || Array.isArray(parsed) || typeof parsed !== 'object') {
      this.$message.error(i18n.t('modules.views.aiPlatform.tools.s_829ceeba', { value0: label }) as string)
      return null
    }
    return parsed as Record<string, unknown>
  }

  private parseJson (value: string | undefined, fallback: unknown) {
    if (!value) {
      return fallback
    }
    try {
      return JSON.parse(value)
    } catch (error) {
      return fallback
    }
  }

  private formatJson (value: unknown) {
    return JSON.stringify(value || {}, null, 2)
  }

  private isLocalTool (row: AiToolDefinition) {
    return !this.isMcpTool(row)
  }

  private isMcpTool (row: AiToolDefinition) {
    return String(row.type || '').toUpperCase() === 'MCP'
  }

  private toolTypeLabel (row: AiToolDefinition) {
    const type = String(row.type || '').toUpperCase()
    if (type === 'JAVA_BEAN') {
      return i18n.t('modules.views.aiPlatform.tools.s_660972bb') as string
    }
    return type || i18n.t('modules.views.aiPlatform.tools.s_8e7eca1f') as string
  }

  private categoryOf (row: AiToolDefinition) {
    const config = this.parseJson(row.configJson, {}) as any
    if (this.isMcpTool(row)) {
      return this.normalizedCategory(row.category || config.category || (row.builtIn ? i18n.t('modules.views.aiPlatform.tools.s_ad849126') as string : i18n.t('modules.views.aiPlatform.experts.s_d4296303') as string))
    }
    return this.normalizedCategory(row.category || config.category || (row.builtIn ? i18n.t('modules.views.aiPlatform.tools.s_6e0b8595') as string : i18n.t('modules.views.aiPlatform.experts.s_d4296303') as string))
  }

  private normalizedCategory (value: unknown) {
    return String(value || '').trim() || i18n.t('modules.views.aiPlatform.experts.s_d4296303') as string
  }

  private groupRows (rows: AiToolDefinition[]) {
    const map = new Map<string, AiToolDefinition[]>()
    rows.forEach((row) => {
      const category = this.categoryOf(row)
      map.set(category, [...(map.get(category) || []), row])
    })
    return Array.from(map.entries()).map(([category, groupRows]) => ({ category, rows: groupRows }))
  }

  private mcpEndpoint (row: AiToolDefinition) {
    const config = this.parseJson(row.configJson, {}) as any
    return config.endpoint || row.implementation || ''
  }

  private mcpTransport (row: AiToolDefinition) {
    const config = this.parseJson(row.configJson, {}) as any
    return config.transport || 'SSE'
  }

  private async toggleEnable (row: AiToolDefinition) {
    const action = row.enabled ? AiPlatformApi.disableTool(row.toolId) : AiPlatformApi.enableTool(row.toolId)
    const { error } = await toAsyncWait(action, false)
    if (!error) {
      this.loadData()
    } else if (error.message !== 'interrupt') {
      this.$message.error(error.message)
    }
  }

  private removeTool (row: AiToolDefinition) {
    const label = this.isMcpTool(row) ? 'MCP' : i18n.t('modules.views.aiPlatform.tools.s_8e7eca1f') as string
    this.$confirm(i18n.t('modules.views.aiPlatform.tools.s_9f7f4544', { value0: label, value1: row.toolId }) as string, i18n.t('common.hint') as string, { type: 'warning' })
      .then(async () => {
        const { error } = await toAsyncWait(AiPlatformApi.deleteTool(row.toolId), false)
        if (!error) {
          this.$message.success(i18n.t('modules.views.aiPlatform.experts.s_0007d170') as string)
          this.loadData()
        } else if (error.message !== 'interrupt') {
          this.$message.error(error.message)
        }
      })
      .catch(() => undefined)
  }
}
</script>

<style lang="scss" scoped>
.full-width {
  width: 100%;
}

.mr-6 {
  margin-right: 6px;
}

.ml-10 {
  margin-left: 10px;
}

.scope-filter {
  flex-shrink: 0;
}

:deep(.code-textarea .el-textarea__inner) {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 12px;
  line-height: 1.65;
  color: #27364f;
  background: #f7f9fc;
}
</style>
