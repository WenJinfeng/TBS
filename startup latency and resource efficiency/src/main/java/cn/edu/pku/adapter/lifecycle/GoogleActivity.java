package cn.edu.pku.adapter.lifecycle;

import cn.edu.pku.adapter.cli.AzureCLI;
import cn.edu.pku.adapter.cli.GoogleCLI;
import cn.edu.pku.adapter.log.GoogleLog;
import cn.edu.pku.model.GCFHTTPResponse;
import cn.edu.pku.model.GoogleLogDetailItem;
import cn.edu.pku.utils.CommandUtil;
import cn.edu.pku.utils.CredentialsProvider;
import cn.edu.pku.utils.NetUtil;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.*;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Maps;
import com.google.api.services.cloudfunctions.v1.CloudFunctions;
import com.google.api.services.cloudfunctions.v1.model.*;
import com.google.auth.Credentials;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.*;


public class GoogleActivity {

    static CloudFunctions cloudFunctions;
    static JsonFactory jsonFactory;
    static HttpTransport httpTransport;
    public static GoogleCredentials credential;
    static HttpRequestInitializer httpRequestInitializer;
    static{
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            jsonFactory = JacksonFactory.getDefaultInstance();
            credential = CredentialsProvider.authorize();
            httpRequestInitializer = new HttpCredentialsAdapter(credential);
            cloudFunctions = new CloudFunctions.Builder(httpTransport, jsonFactory, httpRequestInitializer)
                    .setApplicationName("serverlessfunly")
                    .build();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    public static String invokeFunction(String name, String data){
        name = "projects/serverlessfunly/locations/us-east1/functions/" + name;
        CallFunctionRequest callFunctionRequest = new CallFunctionRequest();
        callFunctionRequest.setData(data);
        callFunctionRequest.setFactory(jsonFactory);
        String executionId = null;
        try {
            CloudFunctions.Projects.Locations.Functions.Call callFunction = cloudFunctions.projects().locations().functions().call(name,callFunctionRequest);
            CallFunctionResponse callFunctionResponse = callFunction.execute();
            executionId = callFunctionResponse.getExecutionId();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            return executionId;
        }
    }

    public static String invokeFunction(String name, String data, String saveName){
        String executeID = invokeFunction(name, data);
        //TODO retrieve and saved the log
        return executeID;
    }

    public static String generateDownloadUrl(String name){
        name = "projects/serverlessfunly/locations/us-east1";
        GenerateDownloadUrlRequest request = new GenerateDownloadUrlRequest();
        try {
            CloudFunctions.Projects.Locations.Functions.GenerateDownloadUrl generateDownloadUrl = cloudFunctions.projects().locations().functions().generateDownloadUrl(name, request);
            GenerateDownloadUrlResponse generateDownloadUrlResponse = generateDownloadUrl.execute();
            System.out.println(generateDownloadUrlResponse.getDownloadUrl());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String generatedUploadUrl(){
        String parent = "projects/serverlessfunly/locations/us-east1";
        GenerateUploadUrlRequest request = new GenerateUploadUrlRequest();
        String url = null;
        try {
            CloudFunctions.Projects.Locations.Functions.GenerateUploadUrl generateUploadUrl = cloudFunctions.projects().locations().functions().generateUploadUrl(parent, request);
            GenerateUploadUrlResponse generateUploadUrlResponse = generateUploadUrl.execute();
            url = generateUploadUrlResponse.getUploadUrl();
            System.out.println(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return url;
    }


    public static void uploadFunctionCode(String url, String path) throws GeneralSecurityException, IOException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        //HttpRequestFactory httpRequestFactory = httpTransport.createRequestFactory(httpRequestInitializer);
        HttpRequestFactory httpRequestFactory = httpTransport.createRequestFactory();
        Map<String, String> parameters = Maps.newHashMap();
        // Add parameters
        MultipartContent content = new MultipartContent().setMediaType(
                new HttpMediaType("multipart/form-data")
                        .setParameter("boundary", "__END_OF_PART__"));



        for (String name : parameters.keySet()) {
            MultipartContent.Part part = new MultipartContent.Part(
                    new ByteArrayContent(null, parameters.get(name).getBytes()));
            part.setHeaders(new HttpHeaders().set(
                    "Content-Disposition", String.format("form-data; name=\"%s\"", name)));
            content.addPart(part);
        }
        // Add file
        FileContent fileContent = new FileContent(
                "application/zip", new File(path));
        MultipartContent.Part part = new MultipartContent.Part(fileContent);
        part.setHeaders(new HttpHeaders()
                .set("Content-Disposition", String.format("form-data; name=\"content\"; filename=\"%s\"", new File(path).getName()))
                .set("content-type","application/zip")
                .set("x-goog-content-length-range","0,104857600"));
        content.addPart(part);

        try {
            HttpResponse response = httpRequestFactory.buildPutRequest(
                    new GenericUrl(url), content).execute();
            System.out.println(IOUtils.toString(response.getContent()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void uploadFunctionCode1(String url, String path){
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpPost httppost = new HttpPost(url);

            FileBody bin = new FileBody(new File(path));
            StringBody comment = new StringBody("A binary file of some kind", ContentType.TEXT_PLAIN);
            HttpEntity reqEntity = MultipartEntityBuilder.create()
                    .addPart("file", bin)
                    .addPart("comment", comment)
                    .build();
            httppost.setEntity(reqEntity);
            System.out.println("executing request " + httppost.getRequestLine());
            CloseableHttpResponse response = null;
            try {
                response = httpclient.execute(httppost);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    System.out.println("Response content length: " +    resEntity.getContentLength());
                }
                try {
                    EntityUtils.consume(resEntity);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } finally {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String invokeWithHttp(String name, String data){
        //TODO region + project + function
        String prefix = "https://us-east1-serverlessfunly.cloudfunctions.net/";
        String url = prefix + name;
        String result = NetUtil.postParams(url, data);
        return result;
    }

    public static String invokeWithHttpAndExeId(String name, String data){
//        String prefix = "https://us-east1-serverlessfunly.cloudfunctions.net/";
//        String url = prefix + name;
//        String result = NetUtil.postParams(url, data);
        String result = invokeWithHttp(name,data);
        GCFHTTPResponse response = new Gson().fromJson(result, GCFHTTPResponse.class);
        return response.executionId;
    }

    public static void getCloudFunction(String name){
        name = "projects/serverlessfunly/locations/us-east1/functions/hello_http";
        try {
            CloudFunctions.Projects.Locations.Functions.Get get = cloudFunctions.projects().locations().functions().get(name);
            CloudFunction cloudFunction = get.execute();
            System.out.println(cloudFunction.getName());
            System.out.println(cloudFunction.getEntryPoint());
            System.out.println(cloudFunction.getRuntime());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteCloudFunction(String name){
        // name = "projects/serverlessfunly/locations/us-east1/functions/hello_http";
        try {
            CloudFunctions.Projects.Locations.Functions.Delete delete = cloudFunctions.projects().locations().functions().delete(name);
            Operation operation = delete.execute();
            System.out.println("delate function: " + name + " ," + new Gson().toJson(operation.getResponse()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static void deployFunctionWithCLI(String name, String entry, String runtime, String memory, String trigger,
                                      String localPath,
                                      String timeout){
        String deployCLI = String.format(GoogleCLI.DEPLOY, name, entry, runtime, memory, trigger, localPath, timeout);
        String[] args = deployCLI.split(" ");
        System.out.println(deployCLI);
        String result = CommandUtil.executeCMDWithResponse(0, GoogleCLI.CLI, "/Users/vector/Desktop/poppy/google-cloud-sdk/bin/", args);
        System.out.println(result);
    }
public void createCloudFunction(String name, String entryPoint, String runtime){
        HttpsTrigger trigger = new HttpsTrigger();
        CloudFunction cloudFunction = new CloudFunction();
        cloudFunction.setRuntime("")
                .setAvailableMemoryMb(256)
                .setHttpsTrigger(null)
                .setName("")
                .setMaxInstances(100)
                .setSourceUploadUrl("")
                .setSourceArchiveUrl("")
                .setTimeout("10");
        String location = "projects/serverlessfunly/locations/us-east1";
        try {
            CloudFunctions.Projects.Locations.Functions.Create create = cloudFunctions.projects().locations().functions().create(location, cloudFunction);
            Operation operation = create.execute();
            System.out.println(operation.getName());
            System.out.println(new Gson().toJson(operation.getResponse()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


   public static String getCodeStart(String functionName, String data){
        long start = System.currentTimeMillis();
        long end = System.currentTimeMillis();
        String invoke1 = invokeWithHttp(functionName, data);
        end = System.currentTimeMillis();
        System.out.println("===duration: " + (end - start));
        try {
            Thread.sleep(10*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        start = System.currentTimeMillis();
        String invoke2 = invokeWithHttp(functionName, data);
        end = System.currentTimeMillis();
        System.out.println("===duration: " + (end - start));
        GCFHTTPResponse response1 = new Gson().fromJson(invoke1, GCFHTTPResponse.class);
        GCFHTTPResponse response2 = new Gson().fromJson(invoke2, GCFHTTPResponse.class);
        List<String> result = new ArrayList<String>();
        result.add(response1.executionId);
        result.add(response2.executionId);
        return new Gson().toJson(result);
    }

    public static long getCodeStartTime(String ids){
        List<String> exeIds = new Gson().fromJson(ids, new TypeToken<List<String>>(){}.getType());
        String log1 = GoogleLog.getLogDetailByExecuteID(null, exeIds.get(0));
        String log2 = GoogleLog.getLogDetailByExecuteID(null, exeIds.get(1));

        Map<String, String> info1 = GoogleLog.parseLog(log1, 1);
        Map<String, String> info2 = GoogleLog.parseLog(log2, 1);
        long time1 = Long.parseLong(info1.get("exeTime"));
        long time2 = Long.parseLong(info2.get("exeTime"));
        System.out.println("code start: " + (time1 - time2));
        return (time1-time2);
    }

    public static void main(String[] args){
        String prefix = "projects/serverlessfunly/locations/us-east1/functions/";
        //invokeFunction("float", "{\"N\":5}");
        //deployFunctionWithCLI("float", "function_handler", "python37", "512MB", "trigger-http", "/Users/vector/Desktop/poppy/ServerlessBenchmark/ServerlessBenchmark/resources/benchmark/python/cpu/float/google", "540");
        //deleteCloudFunction(prefix + "float");

//        String id = invokeFunction("float", "{\"N\":5}");
//        System.out.println(GoogleLog.getLogByExecuteID(id));

        invokeWithHttp("memory-QDkFJ", "{\"index\":10}");
        //getCodeStart("float12345", "{\"N\":5}");
    }
}
