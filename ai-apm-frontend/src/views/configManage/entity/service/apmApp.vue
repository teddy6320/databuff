<template>
  <div class="config-wrapper"
    v-loading='isLoading'>
    <div class="config-content">
      <el-form :model="configForm" :rules="configRules" ref="configForm"
        label-position="top" size="small" class="form-box mb-30">
        <el-form-item :label="$t('modules.views.configManage.entity.s_93ff2a31')" prop="key">
          <scroll-select
            v-model="configForm.key"
            :options="serviceList"
            :clearable="false"
            :showTitle="true"
            class="config-select max" />
        </el-form-item>

        <el-collapse class="db-setting-collapse" v-model="collapseData.value">
          <el-collapse-item :title="$t('modules.views.configManage.entity.s_cba0d619')" name="1">
            <el-form-item :label="$t('modules.views.appMonitor.traceDetail.s_4b1fb5dc')" prop="service_sample_rate">
              <div class="flex-h">
                <el-input
                  v-model="configForm.service_sample_rate"
                  @focus="iptNumberFocusHandle('service_sample_rate')"
                  @change="iptNumberChangeHandle($event, 'service_sample_rate')"
                  placeholder="" maxlength="10"
                  class="config-input input-number mr-5"
                ></el-input> %
                <el-slider
                  v-model="configForm.service_sample_rate"
                  :min="0" :max="100" :step="1" :show-tooltip="false"
                  class="config-slider ml-24"></el-slider>
              </div>
            </el-form-item>

            <div class="flex-h mb-18 lh-18">
              {{ $t('modules.views.configManage.entity.s_9a3380fe') }}
              <el-switch
                v-if="!configForm.low_power_mode"
                v-model="configForm.adaptive_sampling_config.enable"
                class="ml-8" />
              <el-tooltip v-else :content="$t('modules.views.configManage.entity.s_ee2e741f')" placement="right" effect="light" class="ml-8">
                <el-switch v-model="lowPowerData.adaptive_sampling_config.enable" disabled class="ml-8" />
              </el-tooltip>
              <el-tooltip :content="$t('modules.views.configManage.entity.s_6ef3ab00')" placement="right" effect="light" class="ml-8">
                <i class="db-icon-info information"></i>
              </el-tooltip>
            </div>
            <template v-if="!configForm.low_power_mode && configForm.adaptive_sampling_config.enable">
              <div class="flex-h mb-18">{{ $t('modules.views.configManage.entity.s_44f02a2a') }}
                <el-form-item label="" prop="adaptive_sampling_config.successive_duration" class="form-inline-item">
                  <el-select
                    v-model="configForm.adaptive_sampling_config.successive_duration"
                    class="config-select ml-8 mr-8">
                    <el-option v-for="t in 24" :key="t" :value="t * 5" :label="t * 5" />
                  </el-select>
                </el-form-item>
                {{ $t('modules.views.configManage.entity.s_077bba07') }}<span class="red">{{ $t('modules.views.configManage.entity.s_d6c6e029') }}</span>{{ $t('modules.views.configManage.entity.s_9a3380fe') }}
              </div>
              <div class="flex-h mb-18 pl-20">{{ $t('modules.views.configManage.entity.s_44f02a2a') }}
                <el-form-item label="" prop="adaptive_sampling_config.cpuLowerBound" class="form-inline-item">
                  <el-input-number
                    v-model="configForm.adaptive_sampling_config.cpuLowerBound"
                    :max="(configForm.adaptive_sampling_config.cpuUpperBound || 100) - 1"
                    :controls="false" :min="0" :precision="0"
                    size="mini"
                    class="config-input min input-number ml-8 mr-8"
                  ></el-input-number>
                </el-form-item> {{ $t('modules.views.configManage.entity.s_a3030a13') }}
                <el-form-item label="" prop="adaptive_sampling_config.cpuUpperBound" class="form-inline-item">
                  <el-input-number
                    v-model="configForm.adaptive_sampling_config.cpuUpperBound"
                    :min="(configForm.adaptive_sampling_config.cpuLowerBound || 0) + 1"
                    :controls="false" :max="100" :precision="0"
                    size="mini"
                    class="config-input min input-number ml-8 mr-8"
                  ></el-input-number>
                </el-form-item> {{ $t('modules.views.configManage.entity.s_cff9914a') }}
                <el-form-item label="" prop="adaptive_sampling_config.cpuLowerBoundSample" class="form-inline-item">
                  <el-input-number
                    v-model="configForm.adaptive_sampling_config.cpuLowerBoundSample"
                    :min="(configForm.adaptive_sampling_config.cpuUpperBoundSample || 0) + 1"
                    :controls="false" :max="100" :precision="0"
                    size="mini"
                    class="config-input min input-number ml-8 mr-8"
                  ></el-input-number>
                </el-form-item> {{ $t('modules.views.configManage.entity.s_5c47ba7f') }}
                <el-form-item label="" prop="adaptive_sampling_config.cpuUpperBound" class="form-inline-item">
                  <el-input-number
                    v-model="configForm.adaptive_sampling_config.cpuUpperBound"
                    :min="(configForm.adaptive_sampling_config.cpuLowerBound || 0) + 1"
                    :controls="false" :max="100" :precision="0"
                    size="mini"
                    class="config-input min input-number ml-8 mr-8"
                  ></el-input-number>
                </el-form-item> {{ $t('modules.views.configManage.entity.s_cff9914a') }}
                <el-form-item label="" prop="adaptive_sampling_config.cpuUpperBoundSample" class="form-inline-item">
                  <el-input-number
                    v-model="configForm.adaptive_sampling_config.cpuUpperBoundSample"
                    :max="(configForm.adaptive_sampling_config.cpuLowerBoundSample || 100) - 1"
                    :controls="false" :min="0" :precision="0"
                    size="mini"
                    class="config-input min input-number ml-8 mr-8"
                  ></el-input-number>
                </el-form-item> %
              </div>
              <div class="flex-h mb-18 pl-20">{{ $t('modules.views.configManage.entity.s_74ac1708') }}
                <el-form-item label="" prop="adaptive_sampling_config.loadUpperBound" class="form-inline-item">
                  <el-input-number
                    v-model="configForm.adaptive_sampling_config.loadUpperBound"
                    :controls="false" :min="0" :max="3" :precision="0"
                    size="mini"
                    class="config-input min input-number ml-8 mr-8"
                  ></el-input-number>
                </el-form-item> {{ $t('modules.views.configManage.entity.s_3897382b') }}
                <el-form-item label="" prop="adaptive_sampling_config.loadUpperBoundSample" class="form-inline-item">
                  <el-input-number
                    v-model="configForm.adaptive_sampling_config.loadUpperBoundSample"
                    :controls="false" :min="0" :max="100" :precision="0"
                    size="mini"
                    class="config-input min input-number ml-8 mr-8"
                  ></el-input-number>
                </el-form-item> %
              </div>
              <div class="flex-h mb-18 pl-20">{{ $t('modules.views.configManage.entity.s_44f02a2a') }}
                <el-form-item label="" prop="adaptive_sampling_config.heapLowerBound" class="form-inline-item">
                  <el-input-number
                    v-model="configForm.adaptive_sampling_config.heapLowerBound"
                    :max="(configForm.adaptive_sampling_config.heapUpperBound || 100) - 1"
                    :controls="false" :min="0" :precision="0"
                    size="mini"
                    class="config-input min input-number ml-8 mr-8"
                  ></el-input-number>
                </el-form-item> {{ $t('modules.views.configManage.entity.s_bc44ac4b') }}
                <el-form-item label="" prop="adaptive_sampling_config.heapUpperBound" class="form-inline-item">
                  <el-input-number
                    v-model="configForm.adaptive_sampling_config.heapUpperBound"
                    :min="(configForm.adaptive_sampling_config.heapLowerBound || 0) + 1"
                    :controls="false" :max="100" :precision="0"
                    size="mini"
                    class="config-input min input-number ml-8 mr-8"
                  ></el-input-number>
                </el-form-item> {{ $t('modules.views.configManage.entity.s_cff9914a') }}
                <el-form-item label="" prop="adaptive_sampling_config.heapLowerBoundSample" class="form-inline-item">
                  <el-input-number
                    v-model="configForm.adaptive_sampling_config.heapLowerBoundSample"
                    :min="(configForm.adaptive_sampling_config.heapUpperBoundSample || 0) + 1"
                    :controls="false" :max="100" :precision="0"
                    size="mini"
                    class="config-input min input-number ml-8 mr-8"
                  ></el-input-number>
                </el-form-item> {{ $t('modules.views.configManage.entity.s_ff04faf0') }}
                <el-form-item label="" prop="adaptive_sampling_config.heapUpperBound" class="form-inline-item">
                  <el-input-number
                    v-model="configForm.adaptive_sampling_config.heapUpperBound"
                    :min="(configForm.adaptive_sampling_config.heapLowerBound || 0) + 1"
                    :controls="false" :max="100" :precision="0"
                    size="mini"
                    class="config-input min input-number ml-8 mr-8"
                  ></el-input-number>
                </el-form-item> {{ $t('modules.views.configManage.entity.s_cff9914a') }}
                <el-form-item label="" prop="adaptive_sampling_config.heapUpperBoundSample" class="form-inline-item">
                  <el-input-number
                    v-model="configForm.adaptive_sampling_config.heapUpperBoundSample"
                    :max="(configForm.adaptive_sampling_config.heapLowerBoundSample || 100) - 1"
                    :controls="false" :min="0" :precision="0"
                    size="mini"
                    class="config-input min input-number ml-8 mr-8"
                  ></el-input-number>
                </el-form-item> %
              </div>
              <div class="mb-18 pl-20">{{ $t('modules.views.configManage.entity.s_2f8f095e') }}<span class="green">{{ $t('modules.views.configManage.entity.s_c7db6d4f') }}</span>{{ $t('modules.views.configManage.entity.s_14216603') }}</div>
            </template>

            <el-form-item prop="url_path_normalized_type">
              <div slot="label">{{ $t('modules.views.configManage.entity.s_0792a870') }}
                <el-tooltip placement="right" effect="light">
                  <div slot='content'>
                    <div>{{ $t('modules.views.configManage.entity.s_df88b578') }}</div>
                    <div class="mt-5 ml-20">POST /org.databuff.dispatch/abc123abc/82737764223/OrderDispatchService</div>
                    <div class="mt-10">{{ $t('modules.views.configManage.entity.s_fc8830d4') }}</div>
                    <div class="mt-5 ml-20">POST /org.databuff.dispatch/abc123abc/82737764223/OrderDispatchService</div>
                    <div class="mt-10">{{ $t('modules.views.configManage.entity.s_b8418ca6') }}</div>
                    <div class="mt-5 ml-20">POST /org.databuff.dispatch/abc123abc/?/OrderDispatchService</div>
                    <div class="mt-10">{{ $t('modules.views.configManage.entity.s_7456000e') }}</div>
                    <div class="mt-5 ml-20">POST /org.databuff.dispatch/?/?/OrderDispatchService</div>
                  </div>
                  <i class="db-icon-info information"></i>
                </el-tooltip>
              </div>
              <el-select
                v-if="!configForm.low_power_mode"
                v-model="configForm.url_path_normalized_type" clearable class="config-select max">
                <el-option :value="-1" :label="$t('modules.views.configManage.entity.s_4f1287d6')" />
                <el-option :value="0" :label="$t('modules.views.configManage.entity.s_11d0f0dd')" />
                <el-option :value="1" :label="$t('modules.views.configManage.entity.s_046e1a9f')" />
              </el-select>
              <el-tooltip v-else :content="$t('modules.views.configManage.entity.s_ee2e741f')" placement="right" effect="light" class="ml-8">
                <el-select
                  v-model="lowPowerData.url_path_normalized_type" disabled class="config-select max">
                  <el-option :value="-1" :label="$t('modules.views.configManage.entity.s_4f1287d6')" />
                  <el-option :value="0" :label="$t('modules.views.configManage.entity.s_11d0f0dd')" />
                  <el-option :value="1" :label="$t('modules.views.configManage.entity.s_046e1a9f')" />
                </el-select>
              </el-tooltip>
            </el-form-item>

            <el-form-item prop="sql_normalized_type">
              <div slot="label">{{ $t('modules.views.configManage.entity.s_5fa14562') }}
                <el-tooltip placement="right" effect="light">
                  <div slot='content'>
                    <div>{{ $t('modules.views.configManage.entity.s_c7b53dca') }}</div>
                    <div class="mt-5 ml-20">select * from dc_db where apiKey = HW274HYFH2492H and startTriggerTime &lt;= 1710224793 and lastTriggerTime &gt;= 1710226306</div>
                    <div class="mt-10">{{ $t('modules.views.configManage.entity.s_fc8830d4') }}</div>
                    <div class="mt-5 ml-20">select * from dc_db where apiKey = HW274HYFH2492H and startTriggerTime &lt;= 1710224793 and lastTriggerTime &gt;= 1710226306</div>
                    <div class="mt-10">{{ $t('modules.views.configManage.entity.s_0ed58191') }}</div>
                    <div class="mt-5 ml-20">select * from dc_db where apiKey = HW274HYFH2492H and startTriggerTime &lt;= ? and lastTriggerTime &gt;= ?</div>
                    <div class="mt-10">{{ $t('modules.views.configManage.entity.s_7456000e') }}</div>
                    <div class="mt-5 ml-20">select * from dc_db where apiKey = ? and startTriggerTime &lt;= ? and lastTriggerTime &gt;= ?</div>
                  </div>
                  <i class="db-icon-info information"></i>
                </el-tooltip>
              </div>
              <el-select
                v-if="!configForm.low_power_mode"
                v-model="configForm.sql_normalized_type" clearable class="config-select max">
                <el-option :value="-1" :label="$t('modules.views.configManage.entity.s_4f1287d6')" />
                <el-option :value="0" :label="$t('modules.views.configManage.entity.s_f5c2bd8a')" />
                <el-option :value="1" :label="$t('modules.views.configManage.entity.s_046e1a9f')" />
              </el-select>
              <el-tooltip v-else :content="$t('modules.views.configManage.entity.s_ee2e741f')" placement="right" effect="light" class="ml-8">
                <el-select
                  v-model="lowPowerData.sql_normalized_type" disabled class="config-select max">
                  <el-option :value="-1" :label="$t('modules.views.configManage.entity.s_4f1287d6')" />
                  <el-option :value="0" :label="$t('modules.views.configManage.entity.s_f5c2bd8a')" />
                  <el-option :value="1" :label="$t('modules.views.configManage.entity.s_046e1a9f')" />
                </el-select>
              </el-tooltip>
            </el-form-item>
          </el-collapse-item>

          <el-collapse-item :title="$t('modules.api.config.ts.s_1f318234')" name="2">
            <el-form-item label="" prop="profiling.enabled" :show-message="false" class="m-0 pb-10">
              Profiling
              <el-switch
                v-if="!configForm.low_power_mode"
                v-model="configForm.profiling.enabled" class="ml-5"></el-switch>
              <el-tooltip v-else :content="$t('modules.views.configManage.entity.s_ee2e741f')" placement="right" effect="light" class="ml-8">
                <el-switch v-model="lowPowerData.profiling.enabled" disabled class="ml-8" />
              </el-tooltip>
              <el-tooltip :content="$t('modules.views.configManage.entity.s_1d433f85')" placement="right" effect="light">
                <i class="db-icon-info information ml-5 vm"></i>
              </el-tooltip>
            </el-form-item>
            <el-form-item v-if="!configForm.low_power_mode && configForm.profiling.enabled"
              label="" prop="profiling.events" :show-message="false">
              <div class="checkbox-item">
                <div class="w140 flex-h">
                  <el-checkbox v-model="configForm.profiling.events.cpu">On CPU</el-checkbox>
                  <el-tooltip :content="$t('modules.views.configManage.entity.s_7b0a9765')" placement="top-start" effect="light">
                    <i class="db-icon-info information ml-5"></i>
                  </el-tooltip>
                </div>
                <div v-show="configForm.profiling.events.cpu" class="w140">{{ $t('modules.views.configManage.entity.s_9d48bc23', { value0: configForm.profiling.events.cpuValue }) }}</div>
                <div v-show="configForm.profiling.events.cpu" class="config-slider">
                  <el-slider v-model="configForm.profiling.events.cpuValue" :min="2" :max="10" :show-tooltip="false"></el-slider>
                </div>
              </div>
              <div class="checkbox-item">
                <div class="w140 flex-h">
                  <el-checkbox v-model="configForm.profiling.events.wall">Off CPU</el-checkbox>
                  <el-tooltip :content="$t('modules.views.configManage.entity.s_321357df')" placement="top-start" effect="light">
                    <i class="db-icon-info information ml-5"></i>
                  </el-tooltip>
                </div>
                <div v-show="configForm.profiling.events.wall" class="w140">{{ $t('modules.views.configManage.entity.s_9d48bc23', { value0: configForm.profiling.events.wallValue }) }}</div>
                <div v-show="configForm.profiling.events.wall" class="config-slider">
                  <el-slider v-model="configForm.profiling.events.wallValue" :min="25" :max="100" :show-tooltip="false"></el-slider>
                </div>
              </div>
              <div class="checkbox-item">
                <el-checkbox v-model="configForm.profiling.events.alloc">{{ $t('modules.views.configManage.entity.s_5dd79078') }}</el-checkbox>
                <el-tooltip :content="$t('modules.views.configManage.entity.s_2cfa4aac')" placement="top-start" effect="light">
                  <i class="db-icon-info information ml-5"></i>
                </el-tooltip>
              </div>
              <div class="checkbox-item">
                <el-checkbox v-model="configForm.profiling.events.lock">{{ $t('modules.views.configManage.entity.s_002e01e4') }}</el-checkbox>
                <el-tooltip :content="$t('modules.views.configManage.entity.s_4d354387')" placement="top-start" effect="light">
                  <i class="db-icon-info information ml-5"></i>
                </el-tooltip>
              </div>

              <div class="mt-10">{{ $t('modules.views.alarmCenter.alarm.s_71673bab') }}</div>
              <el-select
                v-model="configForm.profiling.serviceInstances"
                multiple clearable
                class="config-select max">
                <el-option v-for="t in (serviceInstanceMap[configForm.key] || [])" :key="t" :value="t" :label="t" />
              </el-select>
            </el-form-item>

            <el-form-item label="" prop="span_resource_consumption" :show-message="false"
              class="m-0 pb-10">
              {{ $t('modules.views.appMonitor.traceDetail.s_9687d0eb') }}
              <el-switch
                v-if="!configForm.low_power_mode"
                v-model="configForm.span_resource_consumption" class="ml-5"></el-switch>
              <el-tooltip v-else :content="$t('modules.views.configManage.entity.s_ee2e741f')" placement="right" effect="light" class="ml-8">
                <el-switch v-model="lowPowerData.span_resource_consumption" disabled class="ml-8" />
              </el-tooltip>
              <el-tooltip :content="$t('modules.views.configManage.entity.s_4891a405')" placement="right" effect="light">
                <i class="db-icon-info information ml-5 vm"></i>
              </el-tooltip>
            </el-form-item>
            <el-form-item v-if="!configForm.low_power_mode && configForm.span_resource_consumption" label="" prop="root_span_resource_consumption" :show-message="false"
              class="m-0 pb-10">
              <el-radio-group v-model="configForm.root_span_resource_consumption">
                <el-radio :label="true">{{ $t('modules.views.configManage.entity.s_8270974e') }}</el-radio>
                <el-radio :label="false">{{ $t('modules.views.configManage.entity.s_23704982') }}</el-radio>
              </el-radio-group>
              <el-tooltip :content="$t('modules.views.configManage.entity.s_9e510dc1')" placement="right" effect="light">
                <i class="db-icon-info information ml-5 vm"></i>
              </el-tooltip>
            </el-form-item>

            <el-form-item label="" prop="instrumentation_profiling.enabled" :show-message="false"
              class="m-0 pb-10">
              {{ $t('modules.views.configManage.entity.s_ecc07455') }}
              <el-switch
                v-if="!configForm.low_power_mode"
                v-model="configForm.instrumentation_profiling.enabled" class="ml-5"></el-switch>
              <el-tooltip v-else :content="$t('modules.views.configManage.entity.s_ee2e741f')" placement="right" effect="light" class="ml-8">
                <el-switch v-model="lowPowerData.instrumentation_profiling.enabled" disabled class="ml-8" />
              </el-tooltip>
            </el-form-item>
            <el-form-item v-if="!configForm.low_power_mode && configForm.instrumentation_profiling.enabled"
              label="" prop="instrumentation_profiling.resources" :show-message="false"
              class="m-0">
              <scroll-select
                v-model="componentType"
                :disabled="compTypeLoading || configForm.instrumentation_profiling.resources.length >= 10"
                :options="compTypeList"
                :clearable="false"
                :placeholder="$t('modules.views.appMonitor.traceDetail.s_9d89e443')"
                class="config-select prev" />
              <scroll-select
                v-model="requestName"
                @change="addResourceHandle"
                :disabled="compTypeLoading || configForm.instrumentation_profiling.resources.length >= 10"
                :options="requestMap[componentType] || []"
                :clearable="false"
                :showTitle="true"
                :allowCreate="true"
                :placeholder="$t('modules.views.configManage.entity.s_6b9aa5d0')"
                class="config-select next" />

              <div class="resource-list">
                <div
                  v-for="(t, i) in configForm.instrumentation_profiling.resources"
                  :key="i"
                  class="resource-item">{{ t }}
                  <span @click="deleteResourceHandle(i)" class="el-icon-close cp"></span>
                </div>
              </div>
            </el-form-item>
            <el-form-item v-if="!configForm.low_power_mode && configForm.instrumentation_profiling.enabled"
              label="" prop="instrumentation_profiling.openTime" :show-message="false">
              <div class="flex-h mt-10">
                {{ $t('modules.views.configManage.entity.s_cc42dd31') }}
                <el-input-number
                  v-model="configForm.instrumentation_profiling.openTime"
                  :controls="false" :min="1" :max="30" :precision="0"
                  class="ml-5 mr-5 config-input input-number"
                ></el-input-number> {{ $t('modules.views.configManage.entity.s_8917151a') }}

                <el-tooltip placement="right" effect="light" class="ml-5">
                  <div slot='content'>
                    <div>{{ $t('modules.views.configManage.entity.s_43017cdb') }}</div>
                    <div class="mt-10">{{ $t('modules.views.configManage.entity.s_85dc3fc5') }}</div>
                  </div>
                  <i class="db-icon-info information"></i>
                </el-tooltip>
              </div>
            </el-form-item>

            <div class="flex-h mb-18 lh-18">
              {{ $t('modules.views.configManage.entity.s_c6b56eb0') }}
              <el-switch
                v-model="configForm.circuit_breaking_config.enable"
                class="ml-8" />
            </div>
            <template v-if="configForm.circuit_breaking_config.enable">
              <div class="flex-h mb-18">{{ $t('modules.views.configManage.entity.s_44f02a2a') }}
                <el-form-item label="" prop="circuit_breaking_config.successive_duration" class="form-inline-item">
                  <el-select
                    v-model="configForm.circuit_breaking_config.successive_duration"
                    class="config-select ml-8 mr-8">
                    <el-option v-for="t in 24" :key="t" :value="t * 5" :label="t * 5" />
                  </el-select>
                </el-form-item>
                {{ $t('modules.views.configManage.entity.s_b2849d18') }}<span class="red">{{ $t('modules.views.configManage.entity.s_d6c6e029') }}</span>{{ $t('modules.views.configManage.entity.s_c5e3dab9') }}
              </div>
              <div class="flex-h mb-18 pl-20">
                <div class="w172">{{ $t('modules.views.configManage.entity.s_ef48f858') }}</div>
                <el-form-item label="" prop="circuit_breaking_config.cpu_trigger_threshold" class="form-inline-item w93">
                  <el-input-number
                    v-model="configForm.circuit_breaking_config.cpu_trigger_threshold"
                    :controls="false" :min="0" :max="100" :precision="0"
                    size="mini"
                    class="config-input min input-number mr-8"
                  ></el-input-number> %
                </el-form-item>
                <el-slider
                  v-model="configForm.circuit_breaking_config.cpu_trigger_threshold"
                  :min="0" :max="100" :step="1" :show-tooltip="false"
                  class="config-slider"></el-slider>
              </div>
              <div class="flex-h mb-18 pl-20">
                <div class="w172">{{ $t('modules.views.configManage.entity.s_108253a7') }}</div>
                <el-form-item label="" prop="circuit_breaking_config.load_trigger_threshold" class="form-inline-item w93">
                  <el-input-number
                    v-model="configForm.circuit_breaking_config.load_trigger_threshold"
                    :controls="false" :min="0" :max="3" :precision="2"
                    size="mini"
                    class="config-input min input-number mr-8"
                  ></el-input-number>
                </el-form-item>
                <el-slider
                  v-model="configForm.circuit_breaking_config.load_trigger_threshold"
                  :min="0" :max="3" :step="0.01" :show-tooltip="false"
                  class="config-slider"></el-slider>
              </div>
              <div class="flex-h mb-18 pl-20">
                <div class="w172">{{ $t('modules.views.configManage.entity.s_4d63f881') }}</div>
                <el-form-item label="" prop="circuit_breaking_config.heap_trigger_threshold" class="form-inline-item w93">
                  <el-input-number
                    v-model="configForm.circuit_breaking_config.heap_trigger_threshold"
                    :controls="false" :min="0" :max="100" :precision="0"
                    size="mini"
                    class="config-input min input-number mr-8"
                  ></el-input-number> %
                </el-form-item>
                <el-slider
                  v-model="configForm.circuit_breaking_config.heap_trigger_threshold"
                  :min="0" :max="100" :step="1" :show-tooltip="false"
                  class="config-slider"></el-slider>
              </div>

              <div class="flex-h mb-18">{{ $t('modules.views.configManage.entity.s_44f02a2a') }}
                <el-form-item label="" prop="circuit_breaking_config.successive_duration" class="form-inline-item">
                  <el-select
                    v-model="configForm.circuit_breaking_config.successive_duration"
                    class="config-select ml-8 mr-8">
                    <el-option v-for="t in 24" :key="t" :value="t * 5" :label="t * 5" />
                  </el-select>
                </el-form-item>
                {{ $t('modules.views.configManage.entity.s_289b314a') }}<span class="green">{{ $t('modules.views.configManage.entity.s_2d92531b') }}</span>{{ $t('modules.views.configManage.entity.s_c5e3dab9') }}
              </div>
              <div class="flex-h mb-18 pl-20">
                <div class="w172">{{ $t('modules.views.configManage.entity.s_93453b75') }}</div>
                <el-form-item label="" prop="circuit_breaking_config.cpu_recovery_threshold" class="form-inline-item w93">
                  <el-input-number
                    v-model="configForm.circuit_breaking_config.cpu_recovery_threshold"
                    :controls="false" :min="0" :max="100" :precision="0"
                    size="mini"
                    class="config-input min input-number mr-8"
                  ></el-input-number> %
                </el-form-item>
                <el-slider
                  v-model="configForm.circuit_breaking_config.cpu_recovery_threshold"
                  :min="0" :max="100" :step="1" :show-tooltip="false"
                  class="config-slider"></el-slider>
              </div>
              <div class="flex-h mb-18 pl-20">
                <div class="w172">{{ $t('modules.views.configManage.entity.s_fa9d4c7b') }}</div>
                <el-form-item label="" prop="circuit_breaking_config.load_recovery_threshold" class="form-inline-item w93">
                  <el-input-number
                    v-model="configForm.circuit_breaking_config.load_recovery_threshold"
                    :controls="false" :min="0" :max="3" :precision="2"
                    size="mini"
                    class="config-input min input-number mr-8"
                  ></el-input-number>
                </el-form-item>
                <el-slider
                  v-model="configForm.circuit_breaking_config.load_recovery_threshold"
                  :min="0" :max="3" :step="0.01" :show-tooltip="false"
                  class="config-slider"></el-slider>
              </div>
              <div class="flex-h mb-18 pl-20">
                <div class="w172">{{ $t('modules.views.configManage.entity.s_ea8ac283') }}</div>
                <el-form-item label="" prop="circuit_breaking_config.heap_recovery_threshold" class="form-inline-item w93">
                  <el-input-number
                    v-model="configForm.circuit_breaking_config.heap_recovery_threshold"
                    :controls="false" :min="0" :max="100" :precision="0"
                    size="mini"
                    class="config-input min input-number mr-8"
                  ></el-input-number> %
                </el-form-item>
                <el-slider
                  v-model="configForm.circuit_breaking_config.heap_recovery_threshold"
                  :min="0" :max="100" :step="1" :show-tooltip="false"
                  class="config-slider"></el-slider>
              </div>
            </template>

            <div class="flex-h mb-18 lh-18">
              {{ $t('modules.views.configManage.entity.s_7d16b0e6') }}
              <el-switch
                v-model="configForm.low_power_mode"
                class="ml-8" />
            </div>
          </el-collapse-item>
        </el-collapse>
      </el-form>
    </div>

    <div class="pt-10">
      <el-button
        :loading="resetLoading"
        :disabled="(postLoading || !configForm.key)"
        @click="resetHandle"
        size="small">{{ $t('modules.views.configManage.entity.s_770fe9e7') }}</el-button>
      <el-button
        :loading="postLoading"
        :disabled="(resetLoading || !configForm.key)"
        @click="saveHandle"
        type="primary" size="small">{{ $t('modules.views.configInstall.plugin.s_be5fbbe3') }}</el-button>
    </div>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop, Watch } from 'vue-property-decorator';
import i18n from '@/i18n';
import { Form } from 'element-ui'
import { toAsyncWait } from '@/utils/common';
import ConfigApi from '@/api/config';
import ApmApi from '@/api/service';
import ServiceApi from '@/api/service';
import { orderBy } from 'lodash';
import deepClone from 'lodash/cloneDeep';
import dayjs from 'dayjs';

const RequestTypeMapping: any = {
  'service.http': i18n.t('modules.views.appMonitor.resourceDetail.s_669262cd') as string,
  'service.rpc': i18n.t('modules.views.appMonitor.resourceDetail.s_4111408b') as string,
  'service.mq': i18n.t('modules.views.appMonitor.resourceDetail.s_82375db4') as string,
  'service.db': i18n.t('modules.views.appMonitor.resourceDetail.s_b3bf0a2d') as string,
  'service.redis': i18n.t('modules.views.appMonitor.resourceDetail.s_218e2ad9') as string,
  'service.config': i18n.t('modules.views.appMonitor.resourceDetail.s_88bdaf32') as string,
  'service.remote': i18n.t('modules.views.appMonitor.resourceDetail.s_71f31c96') as string,
  'service.other': i18n.t('modules.views.appMonitor.resourceDetail.s_b5667e29') as string,
}

@Component
export default class ApmApp extends Vue {
  public $refs!: {
    configForm: Form
  }

  private configForm: any = {
    key: '',
    service_sample_rate: '',
    url_path_normalized_type: '',
    sql_normalized_type: '',
    span_resource_consumption: false,
    root_span_resource_consumption: true,
    low_power_mode: false,
    profiling: {
      enabled: false,
      events: {
        cpu: false,
        cpuValue: 5,
        wall: false,
        wallValue: 50,
        alloc: false,
        allocValue: 500,
        lock: false,
        lockValue: 10,
      },
      serviceInstances: [],
    },
    instrumentation_profiling: {
      enabled: false,
      resources: [],
      openTime: 10,
    },
    adaptive_sampling_config: {
      enable: false,
      successive_duration: 5,
      cpuLowerBound: 70,
      cpuLowerBoundSample: 50,
      cpuUpperBound: 80,
      cpuUpperBoundSample: 10,
      loadUpperBound: 1,
      loadUpperBoundSample: 10,
      heapLowerBound: 70,
      heapLowerBoundSample: 50,
      heapUpperBound: 80,
      heapUpperBoundSample: 10,
    },
    circuit_breaking_config: {
      enable: false,
      broken_sample_rate: 0,
      successive_duration: 30,
      cpu_trigger_threshold: 70,
      cpu_recovery_threshold: 30,
      load_trigger_threshold: 0.8,
      load_recovery_threshold: 0.6,
      heap_trigger_threshold: 70,
      heap_recovery_threshold: 40,
    },
  }

  private configRules = {
    'key': { required: true, trigger: 'change', message: i18n.t('modules.views.configManage.entity.s_cd3f0891') as string, messageKey: 'modules.views.configManage.entity.s_cd3f0891' },
    'service_sample_rate': { required: false, trigger: 'blur', message: i18n.t('modules.views.configManage.entity.s_e6a4612b') as string, messageKey: 'modules.views.configManage.entity.s_e6a4612b' },
    'url_path_normalized_type': { required: false, trigger: 'change', message: i18n.t('modules.views.configManage.entity.s_91a2b481') as string, messageKey: 'modules.views.configManage.entity.s_91a2b481' },
    'sql_normalized_type': { required: false, trigger: 'change', message: i18n.t('modules.views.configManage.entity.s_982168d7') as string, messageKey: 'modules.views.configManage.entity.s_982168d7' },
    'adaptive_sampling_config.successive_duration': { required: true, trigger: 'change', message: i18n.t('modules.views.metrics.list.s_708c9d6d') as string },
    'adaptive_sampling_config.cpuLowerBound': { required: true, trigger: 'blur', message: i18n.t('modules.views.configInstall.dataAccess.s_02cc4f8f') as string, messageKey: 'modules.components.matching-criteria.s_02cc4f8f' },
    'adaptive_sampling_config.cpuLowerBoundSample': { required: true, trigger: 'blur', message: i18n.t('modules.views.configInstall.dataAccess.s_02cc4f8f') as string, messageKey: 'modules.components.matching-criteria.s_02cc4f8f' },
    'adaptive_sampling_config.cpuUpperBound': { required: true, trigger: 'blur', message: i18n.t('modules.views.configInstall.dataAccess.s_02cc4f8f') as string, messageKey: 'modules.components.matching-criteria.s_02cc4f8f' },
    'adaptive_sampling_config.cpuUpperBoundSample': { required: true, trigger: 'blur', message: i18n.t('modules.views.configInstall.dataAccess.s_02cc4f8f') as string, messageKey: 'modules.components.matching-criteria.s_02cc4f8f' },
    'adaptive_sampling_config.loadUpperBound': { required: true, trigger: 'blur', message: i18n.t('modules.views.configInstall.dataAccess.s_02cc4f8f') as string, messageKey: 'modules.components.matching-criteria.s_02cc4f8f' },
    'adaptive_sampling_config.loadUpperBoundSample': { required: true, trigger: 'blur', message: i18n.t('modules.views.configInstall.dataAccess.s_02cc4f8f') as string, messageKey: 'modules.components.matching-criteria.s_02cc4f8f' },
    'adaptive_sampling_config.heapLowerBound': { required: true, trigger: 'blur', message: i18n.t('modules.views.configInstall.dataAccess.s_02cc4f8f') as string, messageKey: 'modules.components.matching-criteria.s_02cc4f8f' },
    'adaptive_sampling_config.heapLowerBoundSample': { required: true, trigger: 'blur', message: i18n.t('modules.views.configInstall.dataAccess.s_02cc4f8f') as string, messageKey: 'modules.components.matching-criteria.s_02cc4f8f' },
    'adaptive_sampling_config.heapUpperBound': { required: true, trigger: 'blur', message: i18n.t('modules.views.configInstall.dataAccess.s_02cc4f8f') as string, messageKey: 'modules.components.matching-criteria.s_02cc4f8f' },
    'adaptive_sampling_config.heapUpperBoundSample': { required: true, trigger: 'blur', message: i18n.t('modules.views.configInstall.dataAccess.s_02cc4f8f') as string, messageKey: 'modules.components.matching-criteria.s_02cc4f8f' },
    'circuit_breaking_config.successive_duration': { required: true, trigger: 'change', message: i18n.t('modules.views.metrics.list.s_708c9d6d') as string },
    'circuit_breaking_config.cpu_trigger_threshold': { required: true, trigger: 'blur', message: i18n.t('modules.views.configInstall.dataAccess.s_02cc4f8f') as string, messageKey: 'modules.components.matching-criteria.s_02cc4f8f' },
    'circuit_breaking_config.cpu_recovery_threshold': { required: true, trigger: 'blur', message: i18n.t('modules.views.configInstall.dataAccess.s_02cc4f8f') as string, messageKey: 'modules.components.matching-criteria.s_02cc4f8f' },
    'circuit_breaking_config.load_trigger_threshold': { required: true, trigger: 'blur', message: i18n.t('modules.views.configInstall.dataAccess.s_02cc4f8f') as string, messageKey: 'modules.components.matching-criteria.s_02cc4f8f' },
    'circuit_breaking_config.load_recovery_threshold': { required: true, trigger: 'blur', message: i18n.t('modules.views.configInstall.dataAccess.s_02cc4f8f') as string, messageKey: 'modules.components.matching-criteria.s_02cc4f8f' },
    'circuit_breaking_config.heap_trigger_threshold': { required: true, trigger: 'blur', message: i18n.t('modules.views.configInstall.dataAccess.s_02cc4f8f') as string, messageKey: 'modules.components.matching-criteria.s_02cc4f8f' },
    'circuit_breaking_config.heap_recovery_threshold': { required: true, trigger: 'blur', message: i18n.t('modules.views.configInstall.dataAccess.s_02cc4f8f') as string, messageKey: 'modules.components.matching-criteria.s_02cc4f8f' },
    'instrumentation_profiling.openTime': { required: true, trigger: 'blur', message: i18n.t('modules.views.configInstall.dataAccess.s_02cc4f8f') as string, messageKey: 'modules.components.matching-criteria.s_02cc4f8f' },
  }

  private lowPowerData: any = {
    url_path_normalized_type: -1,
    sql_normalized_type: -1,
    span_resource_consumption: false,
    profiling: {
      enabled: false,
    },
    instrumentation_profiling: {
      enabled: false,
    },
    adaptive_sampling_config: {
      enable: false,
    },
  }

  private collapseData: any = { // 折叠面板展开控制
    value: ['1'],
    1: [
      'adaptive_sampling_config.successive_duration',
      'adaptive_sampling_config.cpuLowerBound',
      'adaptive_sampling_config.cpuLowerBoundSample',
      'adaptive_sampling_config.cpuUpperBound',
      'adaptive_sampling_config.cpuUpperBoundSample',
      'adaptive_sampling_config.loadUpperBound',
      'adaptive_sampling_config.loadUpperBoundSample',
      'adaptive_sampling_config.heapLowerBound',
      'adaptive_sampling_config.heapLowerBoundSample',
      'adaptive_sampling_config.heapUpperBound',
      'adaptive_sampling_config.heapUpperBoundSample',
    ],
    2: [
      'circuit_breaking_config.successive_duration',
      'circuit_breaking_config.cpu_trigger_threshold',
      'circuit_breaking_config.cpu_recovery_threshold',
      'circuit_breaking_config.load_trigger_threshold',
      'circuit_breaking_config.load_recovery_threshold',
      'circuit_breaking_config.heap_trigger_threshold',
      'circuit_breaking_config.heap_recovery_threshold',
      'instrumentation_profiling.openTime',
    ],
  }

  private isLoading = false // 配置获取中
  private postLoading = false // 配置获取中
  private resetLoading = false // 配置重置中

  private serviceList: any[] = []
  private configMapping: any = {} // 修改前的配置
  get config () {
    return this.configMapping[this.configForm.key]
  }

  @Watch('configForm.key')
  private onKeyChange (key: string) {
    this.collapseData.value = ['1'];
    this.$refs.configForm.resetFields();
    this.configForm.key = key;
    this.initForm()

    this.componentType = ''
    this.compTypeList = []
    this.requestMap = {}
    const serviceId = (this.serviceList.find(t => t.value === key) || {}).id
    if (serviceId) {
      this.getServiceRequest(serviceId)
      if (!this.serviceInstanceMap[key]) {
        this.getServiceInstance(serviceId, key)
      }
    }

    const _query = { ...this.$route.query }
    if (!key) {
      delete _query.service
    } else {
      _query.service = encodeURIComponent(key)
    }
    this.$router.replace({ query: { ..._query } })
  }

  private async mounted () {
    this.isLoading = true
    await this.getServiceList()
    await this.getConfig()
    // 默认选中
    const { service = '' } = this.$route.query;
    const _service = decodeURIComponent(String(service || ''));
    if (_service && this.serviceList.find(t => t.value === _service)) {
      this.configForm.key = _service
    } else {
      this.configForm.key = (this.serviceList[0] || {}).value || ''
    }
  }

  private async getConfig () {
    this.isLoading = true
    const { result, error } = await toAsyncWait(ConfigApi.getConfigList('/javaagent'))
    this.isLoading = false
    if (!error) {
      const data: any[] = (result || {}).data || []
      const configMapping: any = {}
      data.filter(t => t.key !== 'global').forEach((item: any) => {
        item = item || {}
        configMapping[item.key] = { ...item, value: this.formatValue(item.value) }
      })
      this.configMapping = configMapping
    } else if (error.message !== 'interrupt') {
      this.$message.error(error.message);
    }
  }

  private initForm () {
    const configValue = deepClone((this.config || {}).value || {})
    const isNum = (num: any) => typeof num === 'number' && !isNaN(num)
    const getNumValue = (num: any, scale = 1) => isNum(num) ? num * scale : ''
    Object.keys(this.configForm).filter(key => key !== 'key').forEach(key => {
      if (key === 'adaptive_sampling_config') {
        const adaptiveValue = configValue[key]
        const configs: any[] = adaptiveValue?.configs || []
        const cpuLower = configs.find(t => t.resource === 'cpu' && isNum(t.lowerBound) && isNum(t.upperBound))
        const cpuUpper = configs.find(t => t.resource === 'cpu' && isNum(t.lowerBound) && !isNum(t.upperBound))
        const loadUpper = configs.find(t => t.resource === 'load' && isNum(t.lowerBound))
        const heapLower = configs.find(t => t.resource === 'heap' && isNum(t.lowerBound) && isNum(t.upperBound))
        const heapUpper = configs.find(t => t.resource === 'heap' && isNum(t.lowerBound) && !isNum(t.upperBound))
        this.configForm[key] = {
          enable: !!adaptiveValue?.enable,
          successive_duration: adaptiveValue ? adaptiveValue.successive_duration : 5,
          cpuLowerBound: adaptiveValue ? getNumValue(cpuLower?.lowerBound, 100) : 70,
          cpuLowerBoundSample: adaptiveValue ? getNumValue(cpuLower?.sample, 100) : 50,
          cpuUpperBound: adaptiveValue ? getNumValue(cpuLower?.upperBound, 100) || getNumValue(cpuUpper?.lowerBound, 100) : 80,
          cpuUpperBoundSample: adaptiveValue ? getNumValue(cpuUpper?.sample, 100) : 10,
          loadUpperBound: adaptiveValue ? getNumValue(loadUpper?.lowerBound) : 1,
          loadUpperBoundSample: adaptiveValue ? getNumValue(loadUpper?.sample, 100) : 10,
          heapLowerBound: adaptiveValue ? getNumValue(heapLower?.lowerBound, 100) : 70,
          heapLowerBoundSample: adaptiveValue ? getNumValue(heapLower?.sample, 100) : 50,
          heapUpperBound: adaptiveValue ? getNumValue(heapLower?.upperBound, 100) || getNumValue(heapUpper?.lowerBound, 100) : 80,
          heapUpperBoundSample: adaptiveValue ? getNumValue(heapUpper?.sample, 100) : 10,
        }
      } else if (key === 'circuit_breaking_config') {
        const circuitValue = configValue[key]
        this.configForm[key] = {
          enable: !!circuitValue?.enable,
          broken_sample_rate: circuitValue?.broken_sample_rate || 0,
          successive_duration: circuitValue ? circuitValue.successive_duration : 30,
          cpu_trigger_threshold: circuitValue ? getNumValue(circuitValue.cpu_trigger_threshold, 100) : 70,
          cpu_recovery_threshold: circuitValue ? getNumValue(circuitValue.cpu_recovery_threshold, 100) : 30,
          load_trigger_threshold: circuitValue ? getNumValue(circuitValue.load_trigger_threshold) : 0.8,
          load_recovery_threshold: circuitValue ? getNumValue(circuitValue.load_recovery_threshold) : 0.6,
          heap_trigger_threshold: circuitValue ? getNumValue(circuitValue.heap_trigger_threshold, 100) : 70,
          heap_recovery_threshold: circuitValue ? getNumValue(circuitValue.heap_recovery_threshold, 100) : 40,
        }
      } else if (key === 'instrumentation_profiling') {
        const profilingValue = configValue[key] || {}
        this.configForm[key] = {
          enabled: !!profilingValue.enabled,
          resources: profilingValue.resources || [],
          openTime: profilingValue.openTime || 10,
        }
      } else if (key === 'profiling') {
        const profilingValue = configValue[key] || {}
        const profilingEvents = profilingValue.events || {}
        const cpu = parseInt(profilingEvents.cpu, 10)
        const wall = parseInt(profilingEvents.wall, 10)
        const alloc = parseInt(profilingEvents.alloc, 10)
        const lock = parseInt(profilingEvents.lock, 10)
        this.configForm[key] = {
          enabled: !!profilingValue.enabled,
          events: {
            cpu: !isNaN(cpu),
            cpuValue: !isNaN(cpu) ? cpu : 5,
            wall: !isNaN(wall),
            wallValue: !isNaN(wall) ? wall : 50,
            alloc: !isNaN(alloc),
            allocValue: !isNaN(alloc) ? alloc : 500,
            lock: !isNaN(lock),
            lockValue: !isNaN(lock) ? lock : 10,
          },
          serviceInstances: profilingValue.serviceInstances || [],
        }
      } else if (key === 'service_sample_rate') {
        const sampleRate = getNumValue(configValue[key], 100)
        this.configForm[key] = sampleRate || 0
        if (sampleRate === '') {
          // 如果service_sample_rate不存在时，先设为0，再改为空；
          //   防止el-slider复用上一次的value状态；
          // 而使用:key的方式会将el-slider绑定值由空变为0，不可用；
          this.$nextTick(() => {
            this.configForm[key] = ''
          });
        }
      } else if (key === 'root_span_resource_consumption' || key === 'all_span_resource_consumption') {
        const rootSpan = !!configValue.root_span_resource_consumption
        const allSpan = !!configValue.all_span_resource_consumption
        this.configForm.span_resource_consumption = rootSpan || allSpan
        this.configForm.root_span_resource_consumption = rootSpan || allSpan ? rootSpan : true
      } else if (['low_power_mode'].includes(key)) {
        this.configForm[key] = !!configValue[key]
      } else {
        this.configForm[key] = isNum(configValue[key]) ? configValue[key] : ''
      }
    })
  }

  private saveHandle () {
    this.$refs.configForm.validate(async (valid: boolean, fields: any) => {
      if (valid) {
        const configForm = { ...this.configForm }
        delete configForm.key
        const config = deepClone(this.config || {})
        const configValue = config.value || {}
        const profiling = configForm.profiling
        const instrProfiling = configForm.instrumentation_profiling
        const circuitConfig = configForm.circuit_breaking_config
        const adaptiveConfig = configForm.adaptive_sampling_config
        const paramsProfilingEvents: any = {}
        if (profiling.events.cpu) {
          paramsProfilingEvents.cpu = `${profiling.events.cpuValue}ms`
        }
        if (profiling.events.wall) {
          paramsProfilingEvents.wall = `${profiling.events.wallValue}ms`
        }
        if (profiling.events.alloc) {
          paramsProfilingEvents.alloc = `${profiling.events.allocValue}k`
        }
        if (profiling.events.lock) {
          paramsProfilingEvents.lock = `${profiling.events.lockValue}ms`
        }
        if (profiling.enabled && !Object.keys(paramsProfilingEvents).length) {
          this.collapseData.value = [...new Set([...this.collapseData.value, '2'])];
          this.$message.warning(i18n.t('modules.views.configManage.entity.s_90d37c4e') as string)
          return
        }
        const params = {
          ...config,
          key: this.configForm.key,
          path: `/javaagent/${this.configForm.key}`,
          value: {
            ...configValue,
            ...configForm,
            service_sample_rate: configForm.service_sample_rate / 100,
            profiling: {
              ...configValue.profiling || {},
              ...profiling,
              enabled: profiling.enabled && Object.keys(paramsProfilingEvents).length > 0,
              events: paramsProfilingEvents,
            },
            instrumentation_profiling: {
              ...configValue.instrumentation_profiling || {},
              ...instrProfiling,
            },
            circuit_breaking_config: {
              ...configValue.circuit_breaking_config || {},
              ...circuitConfig,
              cpu_trigger_threshold: circuitConfig.cpu_trigger_threshold / 100,
              cpu_recovery_threshold: circuitConfig.cpu_recovery_threshold / 100,
              load_trigger_threshold: circuitConfig.load_trigger_threshold,
              load_recovery_threshold: circuitConfig.load_recovery_threshold,
              heap_trigger_threshold: circuitConfig.heap_trigger_threshold / 100,
              heap_recovery_threshold: circuitConfig.heap_recovery_threshold / 100,
            },
            adaptive_sampling_config: {
              ...configValue.adaptive_sampling_config || {},
              enable: adaptiveConfig.enable,
              successive_duration: adaptiveConfig.successive_duration,
              configs: [
                { resource: 'cpu', lowerBound: adaptiveConfig.cpuLowerBound / 100, upperBound: adaptiveConfig.cpuUpperBound / 100, sample: adaptiveConfig.cpuLowerBoundSample / 100 },
                { resource: 'cpu', lowerBound: adaptiveConfig.cpuUpperBound / 100, sample: adaptiveConfig.cpuUpperBoundSample / 100 },
                { resource: 'load', lowerBound: adaptiveConfig.loadUpperBound, sample: adaptiveConfig.loadUpperBoundSample / 100 },
                { resource: 'heap', lowerBound: adaptiveConfig.heapLowerBound / 100, upperBound: adaptiveConfig.heapUpperBound / 100, sample: adaptiveConfig.heapLowerBoundSample / 100 },
                { resource: 'heap', lowerBound: adaptiveConfig.heapUpperBound / 100, sample: adaptiveConfig.heapUpperBoundSample / 100 },
              ],
            },
          },
        }
        if (params.value.span_resource_consumption) {
          // params.value.root_span_resource_consumption = params.value.root_span_resource_consumption
          params.value.all_span_resource_consumption = !params.value.root_span_resource_consumption
          delete params.value.span_resource_consumption
        } else {
          delete params.value.span_resource_consumption
          delete params.value.root_span_resource_consumption
          delete params.value.all_span_resource_consumption
        }
        if (instrProfiling.enabled) {
          if (!instrProfiling.resources.length) {
            this.collapseData.value = [...new Set([...this.collapseData.value, '2'])];
            this.$message.warning(i18n.t('modules.views.configManage.entity.s_0f650579') as string)
            return
          }
          const openTimestamp = instrProfiling.openTime * 60 * 1000
          params.value.instrumentation_profiling.endTime = dayjs(+new Date() + openTimestamp).format('YYYY-MM-DD HH:mm:ss')
        }
        if (!this.config) {
          params.add = true
          params.builtIn = false
          params.desc = ''
        }
        Object.entries(configForm).forEach(([key, value]) => {
          if (!value && value !== 0 && value !== false) {
            delete params.value[key]
          }
        })
        this.postLoading = true;
        const { result, error } = await toAsyncWait(ConfigApi.saveServiceConfig(params))
        this.postLoading = false;
        if (!error) {
          this.$message.success(i18n.t('modules.views.appMonitor.service.s_55aa6366') as string);
          delete params.add
          this.configMapping[params.key] = {
            ...(this.configMapping[params.key] || params),
            value: deepClone(params.value),
          }
        } else if (error.message !== 'interrupt') {
          this.$message.error(error.message || i18n.t('modules.views.cockpit.tab.s_6de920b4') as string);
        }
      } else {
        const fieldList = Object.keys(fields);
        const collapseValue = [...this.collapseData.value];
        for (const key in this.collapseData) {
          const collapseItem = this.collapseData[key];
          if (key !== 'value' && fieldList.some((t) => collapseItem.includes(t))) {
            collapseValue.push(key);
          }
        }
        this.collapseData.value = [...new Set(collapseValue)];
      }
    })
  }

  private async resetHandle () {
    this.resetLoading = true
    const key = this.configForm.key
    const { result, error } = await toAsyncWait(ConfigApi.resetServiceConfig(`/javaagent/${key}`))
    this.resetLoading = false
    if (!error) {
      delete this.configMapping[key]
      this.$refs.configForm.resetFields();
      this.configForm.key = key;
      this.initForm()
      this.$message.success(i18n.t('modules.views.configManage.entity.s_faa357fc') as string);
    } else if (error.message !== 'interrupt') {
      this.$message.error(error.message || i18n.t('modules.views.configManage.entity.s_4d713822') as string);
    }
  }

  private formatValue (value: any) {
    let _value = value
    if (typeof value === 'string') {
      try {
        _value = JSON.parse(value)
      } catch (error) {
        //
      }
    }
    if (Object.prototype.toString.call(value) !== '[object Object]') {
      _value = {}
    }
    return _value
  }

  private async getServiceList () {
    const { result, error } = await toAsyncWait(ApmApi.getServicesIds({ fromTime: '', toTime: '', ignoreTime: 1, datasources: ['Databuff'], virtualService: 0 }))
    if (!error) {
      let list: any[] = Array.from(new Set((result.data || []).map((t: any) => ({
        label: t.name || '',
        value: t.service || '',
        id: t.id,
      }))));
      // 过滤以.开头或者有/的服务
      // list = list.filter((t: any) => {
      //   return t.label && !/^(?:[.])|\//.test(t.label) && t.value && !/^(?:[.])|\//.test(t.value)
      // });
      const list01 = list.filter(t => /^[a-zA-Z0-9]/.test(t.label))
      const list02 = list.filter(t => !/^[a-zA-Z0-9]/.test(t.label))
      this.serviceList = [
        ...orderBy(list01, [item => item.label.toLocaleLowerCase()], ['asc']),
        ...orderBy(list02, [item => item.label.toLocaleLowerCase()], ['asc']),
      ];
    }
  }

  // 服务请求类型及请求列表
  private compTypeLoading = false;
  private componentType = ''
  private requestName = ''
  private compTypeList: any[] = []
  private requestMap: any = {}
  private async getServiceRequest (serviceId: string) {
    const { fromTime, toTime } = this.getGlobalTimeV2()
    const params = {
      serviceId,
      isIn: 1,
      field: 'resource',
      fromTime, toTime
    }
    this.compTypeLoading = true;
    const { result, error } = await toAsyncWait(ServiceApi.getServiceRequestByCompTypes(params));
    this.compTypeLoading = false;
    if (!error) {
      const data = (result || {}).data || {};
      const types = Object.keys(data);
      this.compTypeList = Object.entries(RequestTypeMapping)
          .filter(([value]) => types.includes(value))
          .map(([value, label]) => ({ label, value }));
      this.requestMap = data
    }
  }
  private deleteResourceHandle (index: number) {
    this.configForm.instrumentation_profiling.resources.splice(index, 1)
  }
  private addResourceHandle () {
    const list: string[] = this.configForm.instrumentation_profiling.resources
    const idx = list.findIndex(t => t === this.requestName)
    if (idx > -1) {
      list.splice(idx, 1)
    }
    list.push(this.requestName);
    this.$nextTick(() => {
      this.requestName = '';
    })
  }

  // 服务实例
  private serviceInstanceMap: any = {}
  private async getServiceInstance (sid: string, key: string) {
    // 最近7天
    const toTime = +new Date()
    const fromTime = toTime - 1000 * 60 * 60 * 24 * 7
    const params = {
      serviceId: sid,
      fromTime: dayjs(fromTime).format('YYYY-MM-DD HH:mm') + ':00',
      toTime: dayjs(toTime).format('YYYY-MM-DD HH:mm') + ':00',
    }
    const { result, error } = await toAsyncWait(ServiceApi.getBasicServiceInstance(params))
    if (!error) {
      const list = ((result.data || {}).serviceInstances || []).map((t: any) => t.serviceInstance)
      this.$set(this.serviceInstanceMap, key, list);
    }
  }

  private prevInputValues: any = {}
  private iptNumberFocusHandle (key: string) {
    this.prevInputValues[key] = this.configForm[key];
  }
  private iptNumberChangeHandle (val: string, key: string, min: number = 0, max: number = 100) {
    val = val.trim();
    if (val === '') {
      if (Number(this.prevInputValues[key]) === 0) {
        this.$set(this.configForm, key, '');
        this.prevInputValues[key] = '';
      } else {
        this.$set(this.configForm, key, 0);
        this.prevInputValues[key] = 0;
      }
      return;
    }
    if (isNaN(Number(val))) {
      this.$set(this.configForm, key, this.prevInputValues[key]);
      return;
    }
    let value = Math.round(Number(val));
    if (value < min) {
      value = min;
    } else if (value > max) {
      value = max;
    }
    this.$set(this.configForm, key, value);
    this.prevInputValues[key] = value;
  }
}
</script>

<style lang="scss" scoped>
.config-wrapper {
  display: flex;
  flex-direction: column;

  .config-content {
    flex: 1;
    overflow: auto;
  }

  .config-input {
    width: 64px;
    &.max {
      width: 480px;
    }
    &.min {
      width: 48px;
      line-height: 24px;
      :deep(.el-input__inner) {
        height: 24px;
        line-height: 24px;
        padding-left: 9px;
        padding-right: 9px;
      }
    }
  }
  .config-select {
    width: 80px;
    &.max {
      width: 480px;
    }
  }
  .config-slider {
    width: 160px;
  }

  .input-number :deep(.el-input__inner) {
    text-align: left;
  }

  :deep(.el-form-item.is-required>.el-form-item__label:before) {
    display: none;
  }
  :deep(.el-input.is-disabled .el-input__inner) {
    color: inherit;
  }

  .form-inline-item {
    margin-bottom: 0;
    line-height: 1;
    :deep(.el-form-item__content) {
      line-height: 1;
      display: flex;
      align-items: center;
    }
    :deep(.el-form-item__error) {
      display: none;
    }
  }

  .m-0 {
    margin: 0 !important;
  }

  .ml-24 {
    margin-left: 24px;
  }

  .w172 {
    width: 172px;
  }
  .w93 {
    width: 93px;
  }


  .prev {
    margin-right: 10px;
    width: 150px;
  }
  .next {
    width: 320px;
  }

  .w100 {
    width: 100px;
  }

  .w55 {
    width: 55px;
  }

  .w140 {
    width: 140px;
  }

  .checkbox-item {
    font-size: 13px;
    line-height: 22px;
    display: flex;
    align-items: center;
    & + .checkbox-item {
      margin-top: 10px;
    }
  }

  .resource-item {
    width: 480px;
    margin-top: 10px;
    padding: 0 28px 0 10px;
    height: 28px;
    line-height: 26px;
    background: var(--bg-color02);
    border: 1px solid var(--border-color-light);
    border-radius: 4px;
    font-size: 12px;
    color: var(--color-text-regular);
    text-overflow: ellipsis;
    white-space: nowrap;
    overflow: hidden;
    position: relative;

    .el-icon-close {
      padding: 6px;
      position: absolute;
      right: 0;
      top: 0;
      font-size: 14px;
      cursor: pointer;
      transition: all 0.1s;
      &:hover {
        color: var(--color-danger);
      }
    }
  }
}
</style>
