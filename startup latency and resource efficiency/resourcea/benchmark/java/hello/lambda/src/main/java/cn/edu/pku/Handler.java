package cn.edu.pku;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.util.Map;

public class Handler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {
    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> stringObjectMap, Context context) {
        return ApiGatewayResponse.builder()
                .setStatusCode(200)
                .setObjectBody("Hello Serverless Guys !")
                .build();
    }
}
