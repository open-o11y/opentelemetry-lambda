package io.opentelemetry.lambda.sampleapps.awssdk;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import io.opentelemetry.api.metrics.GlobalMeterProvider;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.common.Labels;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;

public class AwsSdkRequestHandler
    implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  private static final Logger logger = LogManager.getLogger(AwsSdkRequestHandler.class);
  private static final Meter sampleMeter = GlobalMeterProvider.getMeter("aws-otel", "1.0");
  private static final LongUpDownCounter queueSizeCounter =
      sampleMeter
          .longUpDownCounterBuilder("queueSizeChange")
          .setDescription("Queue Size change")
          .setUnit("one")
          .build();

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
    queueSizeCounter.add(2, Labels.of("apiName", "apiName", "statuscode", "200"));

    return response;
  }
}
