# K8s 安装部署

**环境依赖：** kubectl、Kubernetes 集群

## 1 安装 databuff-ai-apm 平台

默认安装最新版（从 `${APM_PKG_BASE}/VERSION` 读取）：

```bash
curl -fsSL http://192.168.50.140/databuff/ai-apm-k8s-install.sh | bash
```

指定版本：

```bash
curl -fsSL http://192.168.50.140/databuff/ai-apm-k8s-install.sh | bash -s -- --version 0.1.0
# 或
APM_VERSION=0.1.0 curl -fsSL http://192.168.50.140/databuff/ai-apm-k8s-install.sh | bash
```

打开上述 Web UI 即可进入平台。

## 2 安装 demo 应用（可选）

和平台在同一个 K8s 集群上：

```bash
curl -fsSL http://192.168.50.140/databuff/ai-apm-demo-k8s-install.sh | bash
```

## 3 平台使用

配置管理 → 模型配置，输入 API Key 即可：

![配置 API Key](../images/set-api-key.png)

开启 AI + APM 之旅。
