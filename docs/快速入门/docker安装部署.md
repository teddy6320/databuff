# Docker 安装部署

**环境依赖：** docker、docker-compose

## 1 安装 databuff-ai-apm 平台

默认安装最新版（从 `${APM_PKG_BASE}/VERSION` 读取）：

```bash
curl -fsSL http://192.168.50.140/databuff/ai-apm-install.sh | bash
```

指定版本：

```bash
curl -fsSL http://192.168.50.140/databuff/ai-apm-install.sh | bash -s -- --version 0.1.0
# 或
APM_VERSION=0.1.0 curl -fsSL http://192.168.50.140/databuff/ai-apm-install.sh | bash
```

![安装成功](../images/docker-install-success.png)

打开上述 Web UI 即可进入平台。

常用命令：

```bash
cd /opt/databuff-ai-apm
docker-compose up -d
docker-compose down
```

平台固定容器名：`ai-apm-web`、`ai-apm-ingest`、`ai-apm-doris-fe`、`ai-apm-doris-be`。

## 2 安装 demo 应用（可选）

和平台在同一台主机上：

```bash
curl -fsSL http://192.168.50.140/databuff/ai-apm-demo-install.sh | bash
```

## 3 平台使用

配置管理 → 模型配置，输入 API Key 即可：

![配置 API Key](../images/set-api-key.png)

开启 AI + APM 之旅。
