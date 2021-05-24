package cn.edu.pku.adapter.log;

import cn.edu.pku.adapter.cli.AzureCLI;
import cn.edu.pku.model.GoogleLogDetailItem;
import cn.edu.pku.model.GoogleLogItem;
import cn.edu.pku.utils.CommandUtil;
import cn.edu.pku.utils.CredentialsProvider;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.cloudtrace.v1.CloudTrace;
import com.google.api.services.cloudtrace.v1.model.Trace;
import com.google.api.services.cloudtrace.v1.model.TraceSpan;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GoogleLog {
    static String GCM = "gcloud";
    static String prpject = "serverlessfunly";


    public static Long convertTimeToLong(String time) {
        time = time.replace("T", " ");
        time = time.replace("z", "");
        time = time.replace("Z", "");
        String[] times = time.split("\\.");
        DateTimeFormatter ftf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime parse = LocalDateTime.parse(times[0], ftf);
        long t1 = LocalDateTime.from(parse).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long t2 = Long.parseLong(times[1]);
        return t1 + t2/1000;
    }


    public static CloudTrace createCloudTraceService() throws IOException, GeneralSecurityException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        GoogleCredential credential = GoogleCredential.getApplicationDefault();
        if (credential.createScopedRequired()) {
            credential =
                    credential.createScoped(Arrays.asList("https://www.googleapis.com/auth/cloud-platform"));
        }

        return new CloudTrace.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName("Google-CloudTraceSample/0.1")
                .build();
    }

    public static Trace getTraceByID(String traceId){
        CloudTrace cloudTraceService = null;
        Trace response = null;
        try {
            cloudTraceService = createCloudTraceService();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        try {
            CloudTrace.Projects.Traces.Get request =
                    cloudTraceService.projects().traces().get(prpject, traceId);
            response = request.execute();
            System.out.println(response);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            return response;
        }
    }


    public static long getTimeInTrace(Trace trace){
        TraceSpan traceSpan = trace.getSpans().get(0);
        String startTime = traceSpan.getStartTime();
        String endTime = traceSpan.getEndTime();
        return convertTimeToLong(endTime) - convertTimeToLong(startTime);
    }

    public static String getLogDetailByExecuteID(String functionName, String executeId){
        String query = String.format("resource.type=cloud_function AND resource.labels.function_name=%s AND labels.execution_id=%s", functionName, executeId);
        if(executeId == null)
            query = String.format("resource.type=cloud_function AND resource.labels.function_name=%s", functionName);
        if(functionName == null)
            query = String.format("resource.type=cloud_function AND labels.execution_id=%s", executeId);
        String logCLI = String.format("logging read \"%s\" --format=json", query);
        //System.out.println(logCLI);
        String[] args = new String[]{"logging", "read", String.format("%s", query), "--format=json"};
        String result = CommandUtil.executeCMDWithResponse(0, GCM, "/Users/vector/Desktop/poppy/google-cloud-sdk/bin/", args);
        return result;
    }


    @Deprecated
    public static String getLogByExecuteID(String id){
        String logCLI =String.format("functions logs read --execution-id=%s --format=json", id);
        System.out.println(logCLI);
        String[] args = logCLI.split(" ");
        String result = CommandUtil.executeCMDWithResponse(0, GCM, "/Users/vector/Desktop/poppy/google-cloud-sdk/bin/", args);
        return result;
    }

    public static Map<String, String> parseLog(String logStr, int type) {
//        System.out.println("---log str----");
//        System.out.println(logStr);
        Map<String, String > result = new HashMap<>();
        List items = null;
        String execution_id = null;
        String functionName = null;
        String traceID = null;
        if(type == 0)
            
            items = new Gson().fromJson(logStr, new TypeToken<List<GoogleLogItem>>(){}.getType());
        else
            items = new Gson().fromJson(logStr, new TypeToken<List<GoogleLogDetailItem>>(){}.getType());

        for(Object item : items){
            String log = null;
            if(type == 0) {
                log = ((GoogleLogItem) item).log;
                execution_id =  ((GoogleLogItem) item).execution_id;
                functionName =  ((GoogleLogItem) item).name;
            }else{
                log = ((GoogleLogDetailItem) item).textPayload;
                traceID = ((GoogleLogDetailItem) item).getTraceID();
            }
            if(log.startsWith("execute duration:")){
                //json format output
                String data = log.replace("execute duration:", "").trim();
                result.put("executeInfo", data);
            }else if(log.startsWith("Function execution took")){
                //"Function execution took 3319 ms, finished with status code: 200"
                Pattern pattern = Pattern.compile("Function execution took ([0-9]+).*");
                Matcher matcher = pattern.matcher(log);
                int exeTime = 0;
                if(matcher.find()) {
                    exeTime = Integer.parseInt(matcher.group(1));
                }
                result.put("exeTime", exeTime+"");
            }else if(log.contains("Function execution started")){
                result.put("execution_id", execution_id);
                result.put("functionName", functionName);
                result.put("traceID", traceID);
                if(traceID != null){
                    Trace trace = getTraceByID(traceID);
                    result.put("wholeExecute", getTimeInTrace(trace)+"");
                }
            }

        }
        return result;
    }

        public static void main(String[] args){
        String function = "cnn_image_classification";
        //String executeID = "tq4yvwds07da";
        String executeID = "tq4y5kytiyq9";
        System.out.println(convertTimeToLong("2020-05-15T09:50:37.359681Z"));
    }
}
