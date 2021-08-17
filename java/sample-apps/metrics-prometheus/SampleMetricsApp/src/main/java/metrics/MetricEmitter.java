package metrics;

import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.GlobalMeterProvider;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.metrics.export.IntervalMetricReader;
import io.opentelemetry.context.Scope;
import java.io.File;
import javax.swing.filechooser.FileSystemView;
import java.util.*;


public class MetricEmitter {

  static final String DIMENSION_API_NAME = "apiName";
  static final String DIMENSION_STATUS_CODE = "statusCode";

  LongHistogram histogram;
  LongCounter queueSizeCounter;
//   LongSumObserver cpuObserver;

  String latencyMetricName;
  IntervalMetricReader reader;

  public MetricEmitter() {
    String otelExporterOtlpEndpoint = System.getenv("OTEL_EXPORTER_OTLP_ENDPOINT") != null ? System.getenv("OTEL_EXPORTER_OTLP_ENDPOINT") : "127.0.0.1:55680";
    SdkMeterProvider meterProvider = SdkMeterProvider.builder().buildAndRegisterGlobal();
    OtlpGrpcMetricExporter metricExporter = OtlpGrpcMetricExporter.builder().setChannel(
            ManagedChannelBuilder.forTarget(otelExporterOtlpEndpoint).usePlaintext().build())
            .build();
    reader =
        IntervalMetricReader.builder()
        .setMetricExporter(metricExporter)
        .setMetricProducers(Collections.singleton(SdkMeterProvider.builder().buildAndRegisterGlobal()))
        .setExportIntervalMillis(1000)
        .buildAndStart();
    Meter meter = GlobalMeterProvider.get().get("io.opentelemetry.example");

    queueSizeCounter =
            meter
                .counterBuilder("testmetric4")
                .setDescription("Queue Size change")
                .setUnit("one")
                .build();

   histogram =
           meter
                .histogramBuilder("testmetric5")
                .ofLongs()
                .setUnit("metric tonnes")
                .build();

//   cpuObserver =
//            meter
//                 .longSumObserverBuilder("cpu_time")
//                 .setDescription("System CPU usage")
//                 .setUnit("ms")
//                 .setUpdater(
//                    longResult -> {
//                         longResult.observe(
//                             10,
//                             Labels.of(
//                                 "apiName",
//                                 "apiName"));
//                       })
//                 .build();
  }

//   /**
//    * emit http request queue size metrics
//    *
//    * @param returnTime
//    * @param apiName
//    * @param statusCode
//    */
  public void emitQueueSizeChangeMetric(Long queueSizeChange, String apiName, String statusCode) {
        System.out.println(
            "emit metric with queue size change " + queueSizeChange + "," + apiName + "," + statusCode);
        queueSizeCounter.add(queueSizeChange);
      }

  public void emitHistogram(long size, String apiName, String statusCode) {
        System.out.println(
                "emit histogram " + size + "," + apiName + "," + statusCode);
        histogram.record(size);
  }
}
