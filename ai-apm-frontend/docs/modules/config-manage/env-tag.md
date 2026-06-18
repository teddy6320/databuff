# 环境标签

> 页面: `/config/envTag`
> 文件: `src/views/configManage/envTag/index.vue`

## 页面职责

环境标签页用于启停环境标签能力、查看标签树，并进入标签编辑页维护标签值与关联探针。

## 页面结构

- 顶部开关: 启用/停用环境标签
- `search-group`: 标签过滤条件
- `db-table`: 树形标签列表
- 设置页: 基本信息 + 标签值列表 + 探针绑定

## 主要接口

- `EnvTagApi.getTagStatus`
- `EnvTagApi.getTagList`
- `EnvTagApi.getTagDetail`
- `EnvTagApi.saveTag`
- `EnvTagApi.saveTagValue`
- `AgentApi.getList`

详细接口见:

- [Env Tag API](../../api/env-tag.md)
- [Agent API](../../api/agent.md)

## 关键参数

- 编辑页路由: `/config/envTagSetting`
- 编辑页支持两种入口:
  - `id`: 编辑已有标签
  - `key`: 打开静态标签键
- 当前代码内置了两个静态标签键:
  - `envTag1`
  - `envTag2`

## 典型流程

- 列表页先读 `getTagStatus`，再读筛选条件和标签树
- 点击“编辑”进入设置页
- 设置页会先根据 `id` 或 `key` 构建基础信息，再拉探针列表
- 保存成功后会清空 `Common/CLEAR_ENV_TAGS` 缓存

## 关联页面

- 环境标签编辑: `/config/envTagSetting?id=...&key=...`

## 注意事项

- 列表页会把缺失的静态标签键补进表格，即使服务端当前没返回
- 编辑页如果既没有合法 `id` 也没有可识别 `key`，会提示“环境标签不存在”并返回列表
