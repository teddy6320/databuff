# 镜像构建上下文

ingest / web / demo 各自独立目录，由 `deploy/images/build-images.sh` 使用。

```
images/
├── ingest/     # Dockerfile + start.sh + application.yml
├── web/        # Dockerfile + start.sh + application.yml（构建时复制 skills/）
├── demo/       # Dockerfile + start.sh
├── scripts/    # 共享 lib.sh
├── build-images.sh
└── upload-infra-images.sh
```

`build-images.sh` 构建 `databuffhub/ai-apm-*` 短名镜像，导出 `ai-apm-ingest-<ver>-<arch>.tar`、`ai-apm-web-<ver>-<arch>.tar`、`ai-apm-demo-<ver>-<arch>.tar` 并上传到 `APM_IMAGES_PKG_BASE`。

`upload-infra-images.sh` 上传 Doris FE/BE、ZooKeeper 离线包：`doris-fe-<ver>-<arch>.tar` 等。
