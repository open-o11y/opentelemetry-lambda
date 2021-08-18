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
import io.opentelemetry.api.common.Attributes;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
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
  LongHistogram apiLatency;
  LongCounter queueSizeCounter;

  public MetricEmitter() {
    Meter meter = GlobalMeterProvider.get().get("io.opentelemetry.example");

    queueSizeCounter =
            meter
                .counterBuilder("queueSizechange")
                .setDescription("Queue Size change")
                .setUnit("one")
                .build();

   apiLatency =
           meter
                .histogramBuilder("apiLatency")
                .ofLongs()
                .setDescription("api Latency")
                .setUnit("ms")
                .build();

    meter
      .gaugeBuilder("jvm.memory.total")
      .setDescription("Reports JVM memory usage.")
      .setUnit("byte")
      .buildWithCallback(
          result -> result.observe(Runtime.getRuntime().totalMemory(), Attributes.empty()));
  }

  public void emitQueueSizeChangeMetric(long queueSizeChange, String apiName, String statusCode) {
        System.out.println(
            "emit metric with queue size change " + queueSizeChange + "," + apiName + "," + statusCode);
        queueSizeCounter.add(queueSizeChange);
      }

  public void emitLatency(long latency, String apiName, String statusCode) {
        System.out.println(
                "emit metric with api latency " + latency + "," + apiName + "," + statusCode);
        apiLatency.record(latency, Attributes.of(stringKey("apiName"), apiName, stringKey("statusCode"), statusCode));
  }
}
