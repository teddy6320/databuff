import i18n from '@/i18n';
// 指标类型
export const MetricTypeList = [
  // 原始指标 (Raw Metrics)
  { label: i18n.t('modules.utils.static.s_648b7506') as string, labelKey: 'modules.utils.static.s_648b7506', value: 'RAW_SYSTEM' },             // CPU、内存、磁盘、网络等直接从系统采集的指标
  { label: i18n.t('modules.utils.static.s_8bd34896') as string, labelKey: 'modules.utils.static.s_8bd34896', value: 'RAW_APPLICATION' },        // JVM堆内存、线程数、GC时间等应用运行时指标
  { label: i18n.t('modules.utils.static.s_41edbca1') as string, labelKey: 'modules.utils.static.s_41edbca1', value: 'RAW_MIDDLEWARE' },       // 数据库连接数、缓存命中率、消息队列深度等
  { label: i18n.t('modules.utils.static.s_e45f944a') as string, labelKey: 'modules.utils.static.s_e45f944a', value: 'RAW_BUSINESS' },        // 登录次数、订单数、页面访问量等
  // 衍生指标 (Derived Metrics)
  { label: i18n.t('modules.utils.static.s_073c22bd') as string, labelKey: 'modules.utils.static.s_073c22bd', value: 'DERIVED_RATIO' },            // 错误率、成功率、利用率等
  { label: i18n.t('modules.utils.static.s_2a7b7a8e') as string, labelKey: 'modules.utils.static.s_2a7b7a8e', value: 'DERIVED_STATISTICAL' },      // 平均值、中位数、百分位数等
  { label: i18n.t('modules.utils.static.s_101fcfea') as string, labelKey: 'modules.utils.static.s_101fcfea', value: 'DERIVED_TIME_WINDOW' },   // 变化率、趋势、同比环比等
  { label: i18n.t('modules.utils.static.s_59415392') as string, labelKey: 'modules.utils.static.s_59415392', value: 'DERIVED_COMPOSITE' },        // 健康分数、SLI指标等
  { label: i18n.t('modules.utils.static.s_115e7833') as string, labelKey: 'modules.utils.static.s_115e7833', value: 'DERIVED_AGGREGATION' },      // 集群级、服务级、应用级聚合指标
  // 预测指标 (Predictive Metrics)
  { label: i18n.t('modules.utils.static.s_a1f38f82') as string, labelKey: 'modules.utils.static.s_a1f38f82', value: 'PREDICTED_TREND' },        // 基于历史数据预测未来趋势
  { label: i18n.t('modules.utils.static.s_5c79af74') as string, labelKey: 'modules.utils.static.s_5c79af74', value: 'PREDICTED_ANOMALY' },      // 预测可能出现的异常情况
  { label: i18n.t('modules.utils.static.s_412b9f42') as string, labelKey: 'modules.utils.static.s_412b9f42', value: 'PREDICTED_CAPACITY' },     // 预测资源使用和需求
  { label: i18n.t('modules.utils.static.s_d056f4d9') as string, labelKey: 'modules.utils.static.s_d056f4d9', value: 'PREDICTED_LIFETIME' },     // 预测组件或系统的剩余寿命
  // 关联指标 (Correlation Metrics)
  { label: i18n.t('modules.utils.static.s_ed46b40d') as string, labelKey: 'modules.utils.static.s_ed46b40d', value: 'CORRELATION_CAUSAL' },         // 表示因果关系的指标
  { label: i18n.t('modules.utils.static.s_3d983083') as string, labelKey: 'modules.utils.static.s_3d983083', value: 'CORRELATION_COEFFICIENT' },  // 表示不同指标间相关程度
  { label: i18n.t('modules.utils.static.s_9d1abc42') as string, labelKey: 'modules.utils.static.s_9d1abc42', value: 'CORRELATION_IMPACT' },       // 量化某指标对其他指标的影响
  { label: i18n.t('modules.utils.static.s_200ebbfc') as string, labelKey: 'modules.utils.static.s_200ebbfc', value: 'CORRELATION_DEPENDENCY' },   // 服务调用链路上的关联指标
  // 上下文指标 (Contextual Metrics)
  { label: i18n.t('modules.utils.static.s_1f4693a0') as string, labelKey: 'modules.utils.static.s_1f4693a0', value: 'CONTEXTUAL_ENVIRONMENT' },     // 描述系统运行环境的指标
  { label: i18n.t('modules.utils.static.s_8f5fdfe6') as string, labelKey: 'modules.utils.static.s_8f5fdfe6', value: 'CONTEXTUAL_CONFIGURATION' },   // 描述系统配置状态的指标
  { label: i18n.t('modules.utils.static.s_becb1915') as string, labelKey: 'modules.utils.static.s_becb1915', value: 'CONTEXTUAL_CHANGE' },          // 描述系统变更情况的指标
  { label: i18n.t('modules.utils.static.s_c20a5845') as string, labelKey: 'modules.utils.static.s_c20a5845', value: 'CONTEXTUAL_EVENT' },           // 与特定事件相关的指标
  // 业务价值指标 (Business Value Metrics)
  { label: i18n.t('modules.utils.static.s_e673550c') as string, labelKey: 'modules.utils.static.s_e673550c', value: 'BUSINESS_USER_EXPERIENCE' },   // Apdex分数、用户满意度等
  { label: i18n.t('modules.utils.static.s_ec660d36') as string, labelKey: 'modules.utils.static.s_ec660d36', value: 'BUSINESS_IMPACT' },            // 收入影响、用户流失等
  { label: i18n.t('modules.utils.static.s_b5e1675e') as string, labelKey: 'modules.utils.static.s_b5e1675e', value: 'BUSINESS_SLA' },              // 服务水平目标达成情况
  { label: i18n.t('modules.utils.static.s_de07e01b') as string, labelKey: 'modules.utils.static.s_de07e01b', value: 'BUSINESS_COST_EFFICIENCY' },   // 资源利用效率、成本分摊等
  // 合成指标 (Synthetic Metrics)
  { label: i18n.t('modules.utils.static.s_9290a392') as string, labelKey: 'modules.utils.static.s_9290a392', value: 'SYNTHETIC_USER' },       // 通过模拟用户行为获取的指标
  { label: i18n.t('modules.utils.static.s_11ed7147') as string, labelKey: 'modules.utils.static.s_11ed7147', value: 'SYNTHETIC_BLACKBOX' },   // 从外部测试获取的系统表现指标
  { label: i18n.t('modules.utils.static.s_cbe4be23') as string, labelKey: 'modules.utils.static.s_cbe4be23', value: 'SYNTHETIC_BENCHMARK' },  // 与标准基准比较的性能指标
  { label: i18n.t('modules.utils.static.s_580ad6b2') as string, labelKey: 'modules.utils.static.s_580ad6b2', value: 'SYNTHETIC_STRESS' },     // 在压力条件下的系统表现指标
  // 兼容旧版本的枚举值
  { label: i18n.t('modules.utils.static.s_25218d9b') as string, labelKey: 'modules.utils.static.s_25218d9b', value: 'ORIGINAL' },
  { label: i18n.t('modules.utils.static.s_a43780f6') as string, labelKey: 'modules.utils.static.s_a43780f6', value: 'DERIVED' },
  { label: i18n.t('modules.utils.static.s_fbee26a1') as string, labelKey: 'modules.utils.static.s_fbee26a1', value: 'PREDICTED' },
  { label: i18n.t('modules.utils.static.s_c29e4097') as string, labelKey: 'modules.utils.static.s_c29e4097', value: 'SYNTHETIC' },
]

// 指标基本判断
export const MetricBasicJudgmentList = [
  { label: i18n.t('modules.utils.static.s_0f675701') as string, labelKey: 'modules.utils.static.s_0f675701', value: 'HIGH_GOOD' },
  { label: i18n.t('modules.utils.static.s_8503e74f') as string, labelKey: 'modules.utils.static.s_8503e74f', value: 'LOW_GOOD' },
  { label: i18n.t('modules.utils.static.s_42ce4c96') as string, labelKey: 'modules.utils.static.s_42ce4c96', value: 'RANGE_GOOD' },
  { label: i18n.t('modules.utils.static.s_a80f7654') as string, labelKey: 'modules.utils.static.s_a80f7654', value: 'TARGET_GOOD' },
  { label: i18n.t('modules.utils.static.s_5efb62a4') as string, labelKey: 'modules.utils.static.s_5efb62a4', value: 'STABLE_GOOD' },
  { label: i18n.t('modules.utils.static.s_8bbf4eef') as string, labelKey: 'modules.utils.static.s_8bbf4eef', value: 'TREND_UP_GOOD' },
  { label: i18n.t('modules.utils.static.s_d71a2d70') as string, labelKey: 'modules.utils.static.s_d71a2d70', value: 'TREND_DOWN_GOOD' },
]

// 指标聚合方式
export const MetricAggMethodList = [
  { label: i18n.t('modules.utils.static.s_e68632a3') as string, labelKey: 'modules.utils.static.s_e68632a3', value: 'AVG' },
  { label: i18n.t('modules.utils.static.s_fa599ec3') as string, labelKey: 'modules.utils.static.s_fa599ec3', value: 'SUM' },
  { label: i18n.t('modules.utils.static.s_b333034d') as string, labelKey: 'modules.utils.static.s_b333034d', value: 'COUNT' },
  { label: i18n.t('modules.utils.static.s_c322edb8') as string, labelKey: 'modules.utils.static.s_c322edb8', value: 'MIN' },
  { label: i18n.t('modules.utils.static.s_5da89314') as string, labelKey: 'modules.utils.static.s_5da89314', value: 'MAX' },
  { label: i18n.t('modules.utils.static.s_f18784bf') as string, labelKey: 'modules.utils.static.s_f18784bf', value: 'LAST' },
]
