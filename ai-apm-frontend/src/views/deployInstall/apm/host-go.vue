<template>
  <div class="config-wrapper">
    <h5>{{ $t('modules.views.deployInstall.apm.s_10114287') }}</h5>
    <code-view code="go get go.opentelemetry.io/otel
go get go.opentelemetry.io/otel/sdk
go get go.opentelemetry.io/otel/exporters/otlp/otlptrace/otlptracegrpc" />

    <h5>{{ $t('modules.views.deployInstall.apm.s_52ebdbb2') }}</h5>
    <p>{{ $t('modules.views.deployInstall.apm.s_a08eb78b') }}</p>
    <code-view :code="initCode" />

    <h5>{{ $t('modules.views.deployInstall.apm.s_2b900ba4') }}</h5>
    <code-view :code="spanCode" />

    <h5>{{ $t('modules.views.deployInstall.apm.s_fbf960f3') }}</h5>
    <ul>
      <li>{{ $t('modules.views.deployInstall.apm.s_f1e13ce2') }}</li>
      <li>{{ $t('modules.views.deployInstall.apm.s_bb6b0125') }}</li>
    </ul>
  </div>
</template>

<script lang="ts">
import { Vue, Component, Prop } from 'vue-property-decorator';
import CodeView from '@/components/code-view.vue';

@Component({
  components: { CodeView },
})
export default class HostGo extends Vue {
  @Prop({ default: '' }) private ingestHost!: string;

  get resolvedIngestHost () {
    return this.ingestHost || window.location.hostname || '127.0.0.1';
  }

  get initCode () {
    return `ctx := context.Background()

exporter, err := otlptracegrpc.New(ctx,
    otlptracegrpc.WithEndpoint("${this.resolvedIngestHost}:4317"),
    otlptracegrpc.WithInsecure(),
)
if err != nil {
    log.Fatal(err)
}

tp := sdktrace.NewTracerProvider(
    sdktrace.WithBatcher(exporter),
    sdktrace.WithResource(resource.NewWithAttributes(
        semconv.SchemaURL,
        semconv.ServiceName("my-go-service"),
    )),
)
defer tp.Shutdown(ctx)

otel.SetTracerProvider(tp)
otel.SetTextMapPropagator(propagation.TraceContext{})`;
  }

  private spanCode = `tracer := otel.Tracer("my-go-service")
ctx, span := tracer.Start(ctx, "HandleRequest")
defer span.End()

// 业务逻辑...
`
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
