package cn.edu.pku.adapter.log;


import cn.edu.pku.adapter.cli.AzureCLI;
import cn.edu.pku.adapter.lifecycle.AzureActivity;
import cn.edu.pku.utils.AzureUtil;
import cn.edu.pku.utils.CommandUtil;
import com.google.gson.*;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.credentials.AzureCliCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.appservice.FunctionApp;
import com.microsoft.azure.management.monitor.EventData;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.commons.lang.time.StopWatch;
import org.joda.time.DateTime;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

//https://dev.applicationinsights.io/quickstart
public class AzureLog {


    private static OkHttpClient httpClient;


    static {
        httpClient = new OkHttpClient.Builder().readTimeout(1, TimeUnit.MINUTES).build();
    }

    public static void main(String[] args){
        Azure azure = AzureActivity.azure;
        FunctionApp app = azure.appServices().functionApps().getById("/subscriptions/XXXX/resourceGroups/serverlesstestdeploy/providers/Microsoft.Web/sites/liuyiimageprocessing");
        String request = getRequestByInvocationId(app.id(), "XXXX");
        parseRequestLog(request);


    }

    @Deprecated
    static void getLogByApp(Azure azure, FunctionApp app, String appName) throws IOException {

        DateTime recordDateTime = DateTime.now();
        PagedList<EventData> logs = azure.activityLogs().defineQuery()
                .startingFrom(recordDateTime.minusDays(7))
                .endsBefore(recordDateTime)
                .withAllPropertiesInResponse()
                .filterByResource(app.id())
                .execute();

        for (EventData event : logs) {
            if (event.eventName() != null) {
                System.out.println("\tEvent: " + event.eventName().localizedValue());
            }
            if (event.operationName() != null) {
                System.out.println("\tOperation: " + event.operationName().localizedValue());
            }
            System.out.println("\tCaller: " + event.caller());
            System.out.println("\tCorrelationId: " + event.correlationId());
            System.out.println("\tSubscriptionId: " + event.subscriptionId());
            System.out.println(new Gson().toJson(event.properties()));
            System.out.println();
            break;
        }

    }


    private static String readLine(InputStream in) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        int c;
        for (c = in.read(); c != '\n' && c >= 0; c = in.read()) {
            stream.write(c);
        }
        if (c == -1 && stream.size() == 0) {
            return null;
        }
        return stream.toString("UTF-8");
    }

    private static String curl(String url) {
        Request request = new Request.Builder().url(url).get().build();
        try {
            return httpClient.newCall(request).execute().body().string();
        } catch (IOException e) {
            return null;
        }
    }

    private static String post(String url, String body) {
        Request request = new Request.Builder().url(url).post(RequestBody.create(MediaType.parse("text/plain"), body)).build();
        try {
            return httpClient.newCall(request).execute().body().string();
        } catch (IOException e) {
            return null;
        }
    }

    //notice the appid
    public static String getRequestByInvocationId(String appId, String invocationId){
        String querywithInvocationId = String.format(AzureCLI.REQUEST_BY_INVOCATION_ID, invocationId);
        DateTime endTime = SdkContext.dateTimeNow();
        DateTime startTime = SdkContext.dateTimeNow().minusDays(30);

        String cmd = AzureCLI.QUERY_LOG.replace(" ", "$$");
        String query =String.format(cmd, appId, querywithInvocationId, startTime.toString(), endTime.toString());
        String[] args = query.split("\\$\\$");

        //System.out.println(String.join(" ", args));
        String result = CommandUtil.executeCMDWithResponse(1, AzureCLI.CMD, AzureCLI.CMDPATH, args);
        //System.out.println(result);
        return result;
    }

    public static String getLogDetailByInvocationId(String appId, String invocationId){
        String querywithInvocationId = String.format(AzureCLI.REQUEST_DETAIL_BY_INVOCATION_ID, invocationId);
        DateTime endTime = SdkContext.dateTimeNow();
        DateTime startTime = SdkContext.dateTimeNow().minusDays(30);

        String cmd = AzureCLI.QUERY_LOG.replace(" ", "$$");
        String query =String.format(cmd, appId, querywithInvocationId, startTime.toString(), endTime.toString());
        String[] args = query.split("\\$\\$");

        String result = CommandUtil.executeCMDWithResponse(1, AzureCLI.CMD, AzureCLI.CMDPATH, args);
 
        return result;
    }

    public static Map<String, String> parseRequestLog(String result){
        System.out.println(result);
        Map<String, String> parseResult = new HashMap<>();
        JsonObject jsonObject = new JsonParser().parse(result).getAsJsonObject();
        JsonArray tables = jsonObject.getAsJsonArray("tables");
        JsonObject request = tables.get(0).getAsJsonObject();
        JsonArray content = request.getAsJsonArray("rows").get(0).getAsJsonArray();
        System.out.println(content.get(7).getAsString());
        parseResult.put("exeTime", content.get(7).getAsString());
        String requestDetail = content.get(10).getAsString();
        JsonObject detailObject = new JsonParser().parse(requestDetail).getAsJsonObject();
        String functionTime = detailObject.get("FunctionExecutionTimeMs").getAsString();
        String hostInstanceId = detailObject.get("HostInstanceId").getAsString();
        parseResult.put("functionTime", functionTime);
        parseResult.put("hostInstanceId", hostInstanceId);
        return parseResult;
    }

    public static  Map<String, String> parseLogDetail(String result){

        JsonObject jsonObject = new JsonParser().parse(result).getAsJsonObject();
        JsonObject contents = jsonObject.getAsJsonArray("tables").get(0).getAsJsonObject();
        JsonArray rows = contents.getAsJsonArray("rows");
        for(int i = 0; i < rows.size(); i++){
            JsonArray row = rows.get(i).getAsJsonArray();
            String date = row.get(0).getAsString();
            String log =  row.get(1).getAsString();
            String logLevel =  row.get(2).getAsString();
            System.out.println(log);
            if(log.startsWith("execute duration:")){
              
            }
        }
        return null;
    }
}
