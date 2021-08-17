package metrics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static MetricEmitter buildMetricEmitter() {
        return new MetricEmitter();
    }

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        
        MetricEmitter metricEmitter = buildMetricEmitter();

        metricEmitter.emitQueueSizeChangeMetric((long) 500, "/lambda-sample-app", "200");
        metricEmitter.emitHistogram((long) 10.5, "test", "test");

        try {
            Thread.sleep(10000);
        } catch (Exception e) {
            //TODO: handle exception
        }
        return response.withStatusCode(200).withBody("Status Code 200");
    }
}
