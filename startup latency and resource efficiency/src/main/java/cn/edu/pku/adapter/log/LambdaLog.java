package cn.edu.pku.adapter.log;

import cn.edu.pku.backend.ProxyHandler;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsync;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsyncClientBuilder;
import com.amazonaws.services.cloudwatch.model.*;
import com.amazonaws.services.logs.AWSLogsAsync;
import com.amazonaws.services.logs.AWSLogsAsyncClientBuilder;
import com.amazonaws.services.logs.model.*;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LambdaLog {
    //Interface for accessing CloudWatch asynchronously
    static AWSLogsAsync client;
    static AWSLogsAsyncClientBuilder builder;
    static{
        builder = AWSLogsAsyncClientBuilder.standard();
        builder.setCredentials(new AWSCredentialsProvider() {
            @Override
            public AWSCredentials getCredentials() {
                AWSCredentials awsCredentials = new AWSCredentials() {
                    @Override
                    public String getAWSAccessKeyId() {
                        return "XXXX";
                    }

                    @Override
                    public String getAWSSecretKey() {
                        return "XXXX";
                    }
                };
                return awsCredentials;
            }

            @Override
            public void refresh() {

            }
        });
        builder.setRegion("us-east-2");
        client = builder.build();
    }

    public static AWSLogsAsync getClient() {
        return client;
    }

  
    public static void getLogsByFunction(String functionName){
        DescribeLogStreamsRequest describeLogStreamsRequest = new DescribeLogStreamsRequest().withLogGroupName( "/aws/lambda/" + functionName);
        DescribeLogStreamsResult describeLogStreamsResult = client.describeLogStreams( describeLogStreamsRequest );
        for ( LogStream logStream : describeLogStreamsResult.getLogStreams() )
        {

            System.out.println(logStream.getLogStreamName());
            GetLogEventsRequest getLogEventsRequest = new GetLogEventsRequest()
                    .withStartTime( new Date().getTime() - 1000 * 60 * 60 * 100 )
                    .withEndTime(new Date().getTime())
                    .withLogGroupName( "/aws/lambda/" + functionName)
                    .withLogStreamName( logStream.getLogStreamName());


            GetLogEventsResult result = client.getLogEvents( getLogEventsRequest );
            result.getEvents().forEach( outputLogEvent -> {
                System.out.println(outputLogEvent.getMessage());
            } );

        }
    }

   public static void getLogRecordAsync(String logRecordPointer){
        GetLogRecordRequest getLogRecordRequest = new GetLogRecordRequest();
        getLogRecordRequest.setLogRecordPointer(logRecordPointer);
        Future<GetLogRecordResult> getLogEventsResultFuture = client.getLogRecordAsync(getLogRecordRequest);
        final ListenableFuture<GetLogRecordResult>  getLogRecordResultListenableFuture = JdkFutureAdapters.listenInPoolThread(getLogEventsResultFuture);
        getLogRecordResultListenableFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    GetLogRecordResult getLogRecordResult = getLogRecordResultListenableFuture.get();
                    Map<String, String> records = getLogRecordResult.getLogRecord();
                    for(String key : records.keySet()){
                        System.out.println(key + " : " + records.get(key));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        },ProxyHandler.executor);
    }


    public static Map<String, String> parseLog(String logStr){
        String[] logs = logStr.split("\\n");
        Map<String, String> results = new HashMap<>();
        for(String log : logs){
            if(log == null || log.trim().length() == 0)
                continue;
            if(log.startsWith("REPORT")) {
                log = log.replace("REPORT ", "");
                String[] items = log.split("\\t");
                for (String item : items) {
                    String[] kv = item.split(":");
                    kv[0] = kv[0].trim();
                    kv[1] = kv[1].trim();
                    //System.out.println(kv[0] + " : " + kv[1]);
                    results.put(kv[0], kv[1]);

                    if(kv[0].startsWith("Init Duration")){
                        kv[1] = kv[1].replace("ms", "").trim();
                        results.put(kv[0], kv[1]);
                    }else if(kv[0].startsWith("Memory Used")){
                        kv[1] = kv[1].replace("MB", "").trim();
                        results.put(kv[0], kv[1]);
                    }
                }
            }else if(log.contains("INFO")){
                String logStreamName = log.substring(log.length() - 33, log.length());
                results.put("logStreamName", logStreamName);
            }else if(log.startsWith("execute time:")){
                //json format output
                String data = log.replace("execute duration:", "").trim();
                results.put("executeInfo", data);
            }
        }
        return results;
    }

    public static void parseLogItem(String logitem){
        if(logitem == null || logitem.trim().length() == 0)
            return;
        String pattern = null;
        Pattern r = null;
        Matcher m = null;
        if(logitem.startsWith("START RequestId:")){
            pattern = "START RequestId: (.*) Version.*";
            r = Pattern.compile(pattern);
            m = r.matcher(logitem);
            if(m.find()){
                String requestId = m.group(1);
                System.out.println(requestId);
            }
        }else if(logitem.startsWith("END RequestId:")){
            pattern = "END RequestId: (.*)";
            r = Pattern.compile(pattern);
            m = r.matcher(logitem);
            if(m.find()){
                String requestId = m.group(1);
                System.out.println(requestId);
            }
        }else if(logitem.startsWith("REPORT")){
            logitem = logitem.replace("REPORT ", "");
            String[] items = logitem.split("\\t");
            for(String item : items){
                String[] kv = item.split(":");
                kv[0] = kv[0].trim();
                kv[1] = kv[1].trim();
                System.out.println(kv[0] + " : " + kv[1]);
            }
        }else if(logitem.contains("INFO")){
            String logStreamName = logitem.substring(logitem.length() - 32, logitem.length());
            System.out.println(logStreamName);
        }else if(logitem.startsWith("execute time:")){
            //json format output
            String data = logitem.replace("execute duration:", "").trim();
        }
    }

    public static void main(String[] args){
        parseLogItem("XXXX");
    }
}
