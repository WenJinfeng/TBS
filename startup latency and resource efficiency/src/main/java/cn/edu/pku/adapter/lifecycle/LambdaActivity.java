package cn.edu.pku.adapter.lifecycle;

import cn.edu.pku.adapter.log.LambdaLog;
import cn.edu.pku.adapter.log.LogUtil;
import cn.edu.pku.utils.Logger;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.regions.Region;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.List;

public class LambdaActivity implements Activity{


    static String bucketName = "lambda-liuyi";

    static AWSLambda client;
    static AWSLambdaClientBuilder builder;

    static AmazonS3 amazonS3;
    static AmazonS3ClientBuilder amazonS3ClientBuilder;

    static String ROLE = "xxx";

    static AWSCredentialsProvider awsCredentialsProvider = new AWSCredentialsProvider() {
        @Override
        public AWSCredentials getCredentials() {
            AWSCredentials awsCredentials = new AWSCredentials() {
                @Override
                public String getAWSAccessKeyId() {
                    return "xxx";
                }

                @Override
                public String getAWSSecretKey() {
                    return "xxx";
                }
            };
            return awsCredentials;
        }

        @Override
        public void refresh() {

        }
    };

    static{
        builder = AWSLambdaClientBuilder.standard();
        builder.setCredentials(awsCredentialsProvider);
        builder.setRegion("us-east-2");
        client = builder.build();

        amazonS3ClientBuilder = AmazonS3ClientBuilder.standard()
                .withCredentials(awsCredentialsProvider)
                .withRegion("us-east-2");
        amazonS3 = amazonS3ClientBuilder.build();

    }

    @Override
    public void init() {

    }

    @Override
    public void createFunction(String functionName, String runtime, String role) {
        role = ROLE;
        CreateFunctionRequest request = new CreateFunctionRequest().withFunctionName(functionName)
                .withRuntime(runtime)
                .withRole(role);
        CreateFunctionResult response = client.createFunction(request);
    }



    @Override
    public void deleteFunction(String functionName, String qualifier) {

        DeleteFunctionRequest request = new DeleteFunctionRequest().withFunctionName(functionName);
        if(qualifier != null){
            request.setQualifier(qualifier);
        }
        DeleteFunctionResult response = client.deleteFunction(request);
        System.out.println("delete function: " + functionName  + " " + response.getSdkResponseMetadata().toString());
    }

    public void setFunctionConcurrency(String functionName, int reservedConcurrentExecutions){
        PutFunctionConcurrencyRequest request = new PutFunctionConcurrencyRequest();
        request.setFunctionName(functionName);
        request.setReservedConcurrentExecutions(reservedConcurrentExecutions);
        PutFunctionConcurrencyResult response = client.putFunctionConcurrency(request);
    }

    public static void listFunctions(){
        ListFunctionsResult listFunctionsResult = client.listFunctions();
        List<FunctionConfiguration> functions = listFunctionsResult.getFunctions();
        for(FunctionConfiguration functionConfiguration : functions){
            System.out.println(functionConfiguration.getFunctionName());
        }
    }

    public static String invokeFunctionSync(String functionName){
        return invokeFunctionSync(functionName, functionName);
    }

    public static String invokeFunctionSync(String functionName, String savedName){
        return invokeFunctionSync(functionName, savedName, null);
    }

    public static String invokeFunctionSync(String functionName, String savedName, String payload){
        String loginfo = null;
        InvokeRequest invokeRequest = new InvokeRequest();
        invokeRequest.setFunctionName(functionName);
        invokeRequest.setInvocationType(InvocationType.RequestResponse);
        invokeRequest.setLogType("Tail");
        invokeRequest.setPayload(payload);
        long start = new Date().getTime();
        InvokeResult response = client.invoke(invokeRequest);
        if(response.getStatusCode() == 200){
            long end = new Date().getTime();
            String requestID = response.getSdkResponseMetadata().getRequestId();
            String payloads = Logger.byteBufferToString(response.getPayload());
            loginfo = Logger.base64Decode(response.getLogResult());
            loginfo = loginfo + "\n" + "startTime: " + start;
            loginfo = loginfo + "\n" + "endTime: " + end;
            LogUtil.put(savedName, loginfo, true);

        }
        return loginfo;
    }


    // [java8, java11, nodejs10.x, nodejs12.x, python2.7, python3.6, python3.7, python3.8, dotnetcore2.1, go1.x, ruby2.5]
    public static void createFunctionWithS3(String functionName, String runtime, String role, int memorySize, String handlerName, String s3key) throws IOException {
        FunctionCode functionCode = new FunctionCode();
        functionCode.setS3Bucket("lambda-liuyi");
        functionCode.setS3Key(s3key);

        TracingConfig tracingConfig = new TracingConfig();
        tracingConfig.setMode(TracingMode.Active);

        role = ROLE;
        CreateFunctionRequest request = new CreateFunctionRequest()
                .withFunctionName(functionName)
                .withRuntime(runtime)
                .withMemorySize(memorySize)
                .withHandler(handlerName)
                .withDescription(functionName + " with " + memorySize)
                .withRole(role)
                .withTimeout(15*60)
                .withCode(functionCode)
                .withPublish(true);
        CreateFunctionResult response = client.createFunction(request);
        System.out.println(response.getLastUpdateStatus());
        System.out.println(response.toString());
    }


    @Deprecated
    public static void createFunctionWithLocalFile(String functionName, String runtime, String role, int memorySize, String handlerName, String filePath) throws IOException {

        FunctionCode functionCode = new FunctionCode();
        FileChannel fc = new FileInputStream(filePath).getChannel();
        ByteBuffer buffer = ByteBuffer.allocate((int) fc.size());
        fc.read(buffer);
        functionCode.setZipFile(buffer);
        role = ROLE;
        CreateFunctionRequest request = new CreateFunctionRequest()
                .withFunctionName(functionName)
                .withRuntime(runtime)
                .withMemorySize(memorySize)
                .withHandler(handlerName)
                .withDescription(functionName + " with " + memorySize)
                .withRole(role)
                .withTimeout(15*60)
                .withCode(functionCode)
                .withPublish(true);
        CreateFunctionResult response = client.createFunction(request);
        System.out.println(response.getLastUpdateStatus());
        System.out.println(response.toString());
    }

    public static String uploadToS3(File tempFile, String remoteFileName) throws IOException {
        try {
            String bucketPath = bucketName + "/lambda" ;
            amazonS3.putObject(new PutObjectRequest(bucketPath, remoteFileName, tempFile)
                    .withCannedAcl(CannedAccessControlList.BucketOwnerFullControl));
            GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(bucketName, remoteFileName);
            URL url = amazonS3.generatePresignedUrl(urlRequest);
            return url.toString();
        } catch (AmazonServiceException ase) {
            ase.printStackTrace();
        } catch (AmazonClientException ace) {
            ace.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        new LambdaActivity().createFunction("python1-concurrency", "python3.7", null);
        //LambdaActivity.invokeFunctionSync("testwjf");
    }
}
