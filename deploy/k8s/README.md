# Kubernetes 部署

- 单机演示 manifests（[`manifests/`](manifests/)）：`configmap.yaml`、`zookeeper.yaml`、`doris.yaml`、`ingest.yaml`、`web.yaml`
- Demo 造数：[`demo/demo-seeder.yaml`](demo/demo-seeder.yaml)
- 一键安装：[`ai-apm-k8s-install.sh`](ai-apm-k8s-install.sh)、[`ai-apm-demo-k8s-install.sh`](ai-apm-demo-k8s-install.sh)
- 部署包内脚本：[`start.sh`](start.sh)、[`stop.sh`](stop.sh)、[`scripts/lib.sh`](scripts/lib.sh)
- 部署包构建：[`build-k8s.sh`](build-k8s.sh)

安装包目录结构：

```
databuff-ai-apm-k8s-<version>/
├── install.sh      # 卸载后全新安装
├── start.sh        # 按顺序启动（不卸载）
├── stop.sh         # 缩容停服
├── manifests/
├── sql/
└── scripts/lib.sh
```

`install.sh` / `start.sh` 使用 `kubectl` 直装（不依赖 Helm），按顺序启动：

1. ZooKeeper + Doris（StatefulSet）
2. 等待就绪 → `kubectl exec` 执行 `databuff.sql`（库表已存在则跳过）
3. ingest + web（ingest 默认 Deployment，亦兼容 StatefulSet；多副本时自动开启集群二次聚合）

`stop.sh` 将 ingest / web / doris / zookeeper 缩容至 0，保留 Service 与 ConfigMap。

`web` / `ingest` Service 使用 `NodePort`（集群内端口仍为 27403 / 4318，节点访问端口为 32703 / 30418）。

### ingest 集群模式

`ai-apm-config` 中已默认开启集群协调（`INGEST_CLUSTER_ENABLED=true`），依赖 ZooKeeper 做成员发现与选主。`ClusterInstanceCoordinator.effectiveClusterEnabled()` 要求 **clusterEnabled 且 live members > 1**；单副本时仍按 standalone 运行，与现有集成测试行为一致。

**Deployment / StatefulSet 均支持**，node-id 与 endpoint 由 `start.sh` 自动推导：

| 工作负载 | node-id | ZK 注册的 gRPC endpoint |
|---------|---------|----------------------|
| StatefulSet `ai-apm-ingest-0` | `ingest-1` | `<POD_IP>:18112` |
| Deployment `ai-apm-ingest-xxx-yyy` | Pod 全名 | `<POD_IP>:18112` |

Pod 模板需注入 `POD_IP`（Downward API `status.podIP`）。Pod 重启后 IP 变化，启动时会重新注册 ephemeral ZK 节点。

多实例二次聚合验证（Deployment 或 StatefulSet 均可）：

```bash
kubectl scale deploy/ai-apm-ingest -n databuff --replicas=4
# 或: kubectl scale sts/ai-apm-ingest -n databuff --replicas=4
kubectl rollout status deploy/ai-apm-ingest -n databuff
```

跨节点 partial 转发由 `GrpcClusterPartialForwarder` 完成。Demo 需向多个 ingest Pod 分散发送 trace 才能覆盖二次聚合路径。

## 离线镜像

集群拉取镜像困难时，在各节点执行（自动识别 amd64/arm64）：

```bash
curl -fsSL https://databuff.ai/databuff/ai-apm-k8s-download-images.sh | bash

# 或部署包内
./download-images.sh
```

k3s/containerd 节点：

```bash
export IMAGE_LOAD_CMD=ctr
curl -fsSL https://databuff.ai/databuff/ai-apm-k8s-download-images.sh | bash
```

仅升级 ingest / web（强制重新下载，不更新 Doris / ZooKeeper）：

```bash
curl -fsSL https://databuff.ai/databuff/ai-apm-k8s-download-apm-images.sh | bash

# 或部署包内
./download-apm-images.sh
```

镜像包目录见 `env.sh` 中 `APM_IMAGES_PKG_BASE`（默认 `https://databuff.ai/databuff/images`）。

## 默认资源

| 组件 | CPU limit | 内存 limit | JVM |
|------|-----------|------------|-----|
| Doris FE | 1 | 2Gi | FE `-Xmx1200m`（启动时 patch） |
| Doris BE | 2 | 6Gi | 官方 `be-4.1.1` 镜像 |
| ingest | 2 | 5Gi | `-Xms1g -Xmx4g` |
| web | 1 | 2Gi | `-Xms512m -Xmx1536m` |

见 [`manifests/doris.yaml`](manifests/doris.yaml)、[`manifests/ingest.yaml`](manifests/ingest.yaml)、[`manifests/web.yaml`](manifests/web.yaml)。

Doris 使用 `emptyDir`，Pod 重建后须重新 init SQL（[`../common/sql/databuff.sql`](../common/sql/databuff.sql)）。4.x 采用同 Pod 内 FE/BE 双容器（`fe-4.1.1` / `be-4.1.1`），FE 默认 `-Xmx8192m` 超出 2Gi limit，manifest 启动脚本会 patch 为 `-Xmx1200m`；init SQL 在 BE 存储就绪后由 `scripts/lib.sh` 外部执行（轮询 `SHOW BACKENDS` 表格列 `Alive`/`AvailCapacity`，与 Docker `init-doris.sh` 共用 [`../common/scripts/doris-be-wait.sh`](../common/scripts/doris-be-wait.sh)）。
