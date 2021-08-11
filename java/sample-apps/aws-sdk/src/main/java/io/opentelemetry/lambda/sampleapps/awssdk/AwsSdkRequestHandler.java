package io.opentelemetry.lambda.sampleapps.awssdk;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import io.opentelemetry.api.metrics.GlobalMeterProvider;
import io.opentelemetry.api.metrics.LongSumObserver;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.common.Labels;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AwsSdkRequestHandler
    implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  private static final Logger logger = LogManager.getLogger(AwsSdkRequestHandler.class);
  private static final Meter meter = GlobalMeterProvider.getMeter("aws-otel", "1.0");
  private static long totalBytesSent = 0;

  private static final LongSumObserver totalBytesSentObserver =
      meter
          .longSumObserverBuilder("totalApiBytesSentMetricName")
          .setDescription("Total API request load sent in bytes")
          .setUnit("one")
          .setUpdater(
              longResult -> {
                System.out.println(
                    "emit total http request size "
                        + totalBytesSent
                        + " byte, "
                        + "apiNameValue"
                        + ","
                        + "statusCodeValue");
                longResult.observe(
                    totalBytesSent,
                    Labels.of(
                        "DIMENSION_API_NAME",
                        "apiNameValue",
                        "DIMENSION_STATUS_CODE",
                        "statusCodeValue"));
              })
          .build();
  ;

  @Override
  public APIGatewayProxyResponseEvent handleRequest(
      APIGatewayProxyRequestEvent input, Context context) {
    logger.info("Serving lambda request.");

    APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

    // Generate a sample counter metric using the OpenTelemetry Java Metrics API

    totalBytesSent += 1;
    // Metric is 0 even with this sleep.
    // try {
    //   Thread.sleep(15000);
    // } catch (InterruptedException e) {
    //   e.printStackTrace();
    // }
    // response.setBody("finished emitting metric");

    return response.withStatusCode(200).withBody("Status Code 200");
  }
}
