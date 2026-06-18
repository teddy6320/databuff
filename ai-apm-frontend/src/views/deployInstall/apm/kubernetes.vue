<template>
  <div class="config-wrapper">
    <p>{{ $t('modules.views.deployInstall.apm.s_acf04cf1') }}</p>
    <p>{{ $t('modules.views.deployInstall.apm.s_6e0f5302') }} <code-view code="inject-sdk" :showCopy="false" type="inline" /> {{ $t('modules.views.deployInstall.apm.s_4673f959') }}</p>

    <h5>{{ $t('modules.views.deployInstall.apm.s_0bc15688') }}</h5>
    <p>{{ $t('modules.views.deployInstall.apm.s_12fd54c6') }}</p>
    <code-view :code="operatorInstall" />

    <h5>{{ $t('modules.views.deployInstall.apm.s_14daea8f') }}</h5>
    <p>{{ $t('modules.views.deployInstall.apm.s_389d97bd') }}</p>
    <code-view :code="instrumentationYaml" />

    <h5>{{ $t('modules.views.deployInstall.apm.s_db745577') }}</h5>
    <p>{{ $t('modules.views.deployInstall.apm.s_b3027698') }}</p>
    <marked-view :data="annotationTable" />
    <p>{{ $t('modules.views.deployInstall.apm.s_5e3ffc9c') }} <code-view code="--enable-go-instrumentation=true" :showCopy="false" type="inline" /> {{ $t('modules.views.deployInstall.apm.s_7c45e627') }}</p>
    <code-view code="instrumentation.opentelemetry.io/inject-go: &quot;true&quot;
instrumentation.opentelemetry.io/otel-go-auto-target-exe: &quot;/path/to/app&quot;" />
    <p>{{ $t('modules.views.deployInstall.apm.s_36c4be17') }} <code-view code="spec.apacheHttpd" :showCopy="false" type="inline" /> {{ $t('modules.components.matching-criteria.s_a7185263') }} <code-view code="spec.nginx" :showCopy="false" type="inline" />{{ $t('modules.views.deployInstall.apm.s_f74ecfc5') }}</p>
    <p>{{ $t('modules.views.deployInstall.apm.s_591c2bf6') }}</p>
    <code-view :code="deploymentYaml" />

    <h5>{{ $t('modules.views.deployInstall.apm.s_e71db72f') }}</h5>
    <p>{{ $t('modules.views.deployInstall.apm.s_e920c177') }}</p>
    <code-view code="kubectl annotate namespace my-app instrumentation.opentelemetry.io/inject-java=true" />

    <h5>{{ $t('modules.views.deployInstall.apm.s_50dc4349') }}</h5>
    <ul>
      <li>{{ $t('modules.views.deployInstall.apm.s_d1e55537') }}<code-view code="kubectl describe pod <pod-name> -n <namespace>" :showCopy="false" type="inline" /></li>
      <li>{{ $t('modules.views.deployInstall.apm.s_189448a2') }}</li>
      <li>{{ $t('modules.views.deployInstall.apm.s_7a5bf15a') }}</li>
    </ul>

    <h5>{{ $t('modules.views.deployInstall.apm.s_44b60d01') }}</h5>
    <marked-view :data="faqTableText" />
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import CodeView from '@/components/code-view.vue';
import MarkedView from '@/components/marked-view.vue';

@Component({
  components: { CodeView, MarkedView },
})
export default class DeployKubernetes extends Vue {
  @Prop({ default: '' }) private ingestHttpEndpoint!: string;

  get resolvedIngestHttpEndpoint () {
    if (this.ingestHttpEndpoint) {
      return this.ingestHttpEndpoint;
    }
    const host = window.location.hostname || '127.0.0.1';
    return `http://${host}:4318`;
  }

  get instrumentationYaml () {
    const endpoint = this.resolvedIngestHttpEndpoint.replace(/\/$/, '');
    return `apiVersion: opentelemetry.io/v1alpha1
kind: Instrumentation
metadata:
  name: databuff-instrumentation
  namespace: test
spec:
  exporter:
    endpoint: ${endpoint}
  propagators:
    - tracecontext
    - baggage
  sampler:
    type: parentbased_traceidratio
    argument: "1"
  java:
    env:
      - name: OTEL_EXPORTER_OTLP_PROTOCOL
        value: http/protobuf
      - name: OTEL_TRACES_EXPORTER
        value: otlp
      - name: OTEL_METRICS_EXPORTER
        value: otlp
      - name: OTEL_LOGS_EXPORTER
        value: none
      - name: OTEL_EXPORTER_OTLP_TRACES_ENDPOINT
        value: ${endpoint}/v1/traces
      - name: OTEL_EXPORTER_OTLP_METRICS_ENDPOINT
        value: ${endpoint}/v1/metrics
  nodejs:
    env:
      - name: OTEL_EXPORTER_OTLP_PROTOCOL
        value: http/protobuf
  python:
    env:
      - name: OTEL_EXPORTER_OTLP_PROTOCOL
        value: http/protobuf
  dotnet:
    env:
      - name: OTEL_EXPORTER_OTLP_PROTOCOL
        value: http/protobuf
  go:
    env:
      - name: OTEL_EXPORTER_OTLP_PROTOCOL
        value: http/protobuf`;
  }

  get faqTableText () {
    return `
| 现象 | 处理建议 |
| --- | --- |
| Pod 未注入 Agent | 检查 Operator 是否 Running；Instrumentation 与 Pod 是否在同一 Namespace；注解拼写是否正确 |
| 无 Trace 数据 | 确认 Ingest HTTP 地址 \`${this.resolvedIngestHttpEndpoint}\` 从集群内可达；检查 Pod 内 \`OTEL_EXPORTER_OTLP_ENDPOINT\` |
| 无 JVM Metrics | 在 Instrumentation 的 \`spec.java.env\` 中设置 \`OTEL_METRICS_EXPORTER=otlp\` 与 \`OTEL_EXPORTER_OTLP_METRICS_ENDPOINT\`；重启 Pod 后确认环境变量已注入 |
| 服务名不符合预期 | 在 Instrumentation 中配置 \`spec.resource\` 或通过 Pod 环境变量设置 \`OTEL_SERVICE_NAME\` |
`;
  }

  private operatorInstall = `# 安装 cert-manager
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.14.4/cert-manager.yaml

# 安装 OpenTelemetry Operator
kubectl apply -f https://github.com/open-telemetry/opentelemetry-operator/releases/latest/download/opentelemetry-operator.yaml`

  private annotationTable = `
| 语言 / 运行时 | Pod 模板注解 | 说明 |
| --- | --- | --- |
| Java | \`instrumentation.opentelemetry.io/inject-java: "true"\` | Init Container 自动注入 |
| Node.js | \`instrumentation.opentelemetry.io/inject-nodejs: "true"\` | Init Container 自动注入 |
| Python | \`instrumentation.opentelemetry.io/inject-python: "true"\` | Init Container 自动注入 |
| .NET | \`instrumentation.opentelemetry.io/inject-dotnet: "true"\` | Init Container 自动注入 |
| Go | \`instrumentation.opentelemetry.io/inject-go: "true"\` | eBPF Sidecar，需开启特性门控 |
| Deno | \`instrumentation.opentelemetry.io/inject-sdk: "true"\` | 仅注入 OTEL 环境变量 |
| Apache HTTPD | \`instrumentation.opentelemetry.io/apache-httpd-container-names: "<container>"\` | Sidecar 注入，需配置 \`spec.apacheHttpd\` |
| NGINX | \`instrumentation.opentelemetry.io/nginx-container-names: "<container>"\` | Sidecar 注入，需配置 \`spec.nginx\` |
| 其他 SDK 语言 | \`instrumentation.opentelemetry.io/inject-sdk: "true"\` | Ruby / PHP / C++ / Rust 等，应用内自行集成 SDK |
`

  private deploymentYaml = `apiVersion: apps/v1
kind: Deployment
metadata:
  name: demo-service
  namespace: my-app
spec:
  replicas: 1
  selector:
    matchLabels:
      app: demo-service
  template:
    metadata:
      labels:
        app: demo-service
      annotations:
        instrumentation.opentelemetry.io/inject-java: "true"
    spec:
      containers:
        - name: app
          image: your-registry/demo-service:latest
          ports:
            - containerPort: 8080`
}
</script>

<style lang="scss" scoped>
.config-wrapper {
  font-size: 13px;
  line-height: 24px;

  h5 {
    margin: 0 0 10px;
    font-size: 14px;
    font-weight: 500;
    color: var(--color-text-primary);
  }

  * + h5 {
    margin-top: 20px;
  }

  p, ul {
    margin: 5px 0;
  }

  ul {
    padding-left: 20px;
  }

}
</style>
