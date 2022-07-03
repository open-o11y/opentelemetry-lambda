package io.opentelemetry.lambda.sampleapps.awssdk;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.ObservableLongGauge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;

public class AwsSdkRequestHandler
    implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  long apiBytesSent;
  long queueSizeChange;

  long totalBytesSent;
  long apiLastLatency;
  long actualQueueSize;

  // The below API name and status code dimensions are currently shared by all metrics observer in
  // this class.
  String apiNameValue = "";
  String statusCodeValue = "";

  private static final Logger logger = LogManager.getLogger(AwsSdkRequestHandler.class);
  private static final Meter sampleMeter =
      GlobalOpenTelemetry.getMeterProvider()
          .meterBuilder("aws-otel")
          .setInstrumentationVersion("1.0")
          .build();
  private static final LongUpDownCounter queueSizeCounter =
      sampleMeter
          .upDownCounterBuilder("queueSizeChange")
          .setDescription("Queue Size change")
          .setUnit("one")
          .build();

  ObservableLongGauge sumMetric =
      sampleMeter
          .gaugeBuilder("totalApiBytesSentMetricName")
          .ofLongs()
          .setDescription("Total API request load sent in bytes")
          .setUnit("one")
          .buildWithCallback(
              measurement -> {
                measurement.record(2, Attributes.of(API_NAME, "apiName", STATUS_CODE, "200"));
              });

  ObservableLongGauge upDownCounterMetric =
      sampleMeter
          .gaugeBuilder("lastLatencyMetricName")
          .ofLongs()
          .setDescription("The last API latency observed at collection interval")
          .setUnit("ms")
          .buildWithCallback(
              measurement -> {
                measurement.record(2, Attributes.of(API_NAME, "apiName", STATUS_CODE, "200"));
              });
  ObservableLongGauge upDownSumMetric =
      sampleMeter
          .gaugeBuilder("actualQueueSizeMetricName")
          .ofLongs()
          .setDescription("The actual queue size observed at collection interval")
          .setUnit("one")
          .buildWithCallback(
              measurement -> {
                measurement.record(2, Attributes.of(API_NAME, "apiName", STATUS_CODE, "200"));
              });

  private static final AttributeKey<String> API_NAME = AttributeKey.stringKey("apiName");
  private static final AttributeKey<String> STATUS_CODE = AttributeKey.stringKey("statuscode");
  private static final Attributes METRIC_ATTRIBUTES =
      Attributes.builder().put(API_NAME, "apiName").put(STATUS_CODE, "200").build();

  /**
   * emit http request load size with counter metrics type
   *
   * @param bytes
   * @param apiName
   * @param statusCode
   */
  public void emitBytesSentMetric(int bytes, String apiName, String statusCode) {
    apiBytesSent += bytes;
  }

  /**
   * emit queue size change metrics with UpDownCounter metric type
   *
   * @param queueSizeChange
   * @param apiName
   * @param statusCode
   */
  public void emitQueueSizeChangeMetric(int queueSizeChange, String apiName, String statusCode) {
    queueSizeChange += queueSizeChange;
  }

  /**
   * update total http request load size, it will be collected as summary metrics type
   *
   * @param bytes
   * @param apiName
   * @param statusCode
   */
  public void updateTotalBytesSentMetric(int bytes, String apiName, String statusCode) {
    totalBytesSent += bytes;
    apiNameValue = apiName;
    statusCodeValue = statusCode;
  }

  /**
   * update last api latency, it will be collected by value observer
   *
   * @param returnTime
   * @param apiName
   * @param statusCode
   */
  public void updateLastLatencyMetric(Long returnTime, String apiName, String statusCode) {
    apiLastLatency = returnTime;
    apiNameValue = apiName;
    statusCodeValue = statusCode;
  }
  /**
   * update actual queue size, it will be collected by UpDownSumObserver
   *
   * @param queueSizeChange
   * @param apiName
   * @param statusCode
   */
  public void updateActualQueueSizeMetric(int queueSizeChange, String apiName, String statusCode) {
    actualQueueSize += queueSizeChange;
    apiNameValue = apiName;
    statusCodeValue = statusCode;
  }

  @Override
  public APIGatewayProxyResponseEvent handleRequest(
      APIGatewayProxyRequestEvent input, Context context) {
    logger.info("Serving lambda request.");

    APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
    try (S3Client s3 = S3Client.create()) {
      ListBucketsResponse listBucketsResponse = s3.listBuckets();
      response.setBody(
          "Hello lambda - found " + listBucketsResponse.buckets().size() + " buckets.");
    }

    // Generate a sample counter metric using the OpenTelemetry Java Metrics API
    queueSizeCounter.add(2, METRIC_ATTRIBUTES);

    return response;
  }
}
