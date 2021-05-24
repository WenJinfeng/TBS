package cn.edu.pku.adapter.lifecycle;

import cn.edu.pku.adapter.log.LogUtil;
import com.aliyuncs.fc.client.FunctionComputeClient;
import com.aliyuncs.fc.constants.Const;
import com.aliyuncs.fc.model.Code;
import com.aliyuncs.fc.request.*;
import com.aliyuncs.fc.response.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.util.Date;

public class AliFCActivity {
    private static final String[] CODE_DIR = {"xxxx"};
    private static final String REGION = "XXX";
    private static final String accountId = "xxx";
    private static final String accessKey = "xxx";
    private static final String accessSecretKey = "xxxx";
    private static final String role = "acs:ram::xxxx:role/XXXXX";
    private static final String projctlog="XXXX";
    private static final String logstore="XXXX";
    private static int i=1;
    // Initialize FC client
    static FunctionComputeClient fcClient = new FunctionComputeClient(REGION, accountId, accessKey, accessSecretKey);


    public static void createService(String service_name){
        CreateServiceRequest csReq=new CreateServiceRequest();
        csReq.setServiceName(service_name);
        csReq.setDescription("Sercice Description");
        csReq.setRole(role);
        //csReq.setLogConfig(new LogConfig(projctlog,logstore));
        CreateServiceResponse csResp= fcClient.createService(csReq);
        System.out.println("Created service, request ID " + csResp.getRequestId());
    }

   public static void createFunction(String service_name,String function_name,String function_des,int memorySize,String runtime ){
        CreateFunctionRequest cfReq = new CreateFunctionRequest(service_name);
        cfReq.setFunctionName(function_name);
        cfReq.setDescription(function_des);
        cfReq.setMemorySize(memorySize);
        cfReq.setRuntime(runtime);


        // Used in initializer situations.
        //cfReq.setInitializer("index.initializer");
        Code code = null;
        try {
            if(runtime.equals("nodejs6")){
                cfReq.setHandler("index.handler");
                code = new Code().setDir(CODE_DIR[0]);
                cfReq.setCode(code);
            }
            if(runtime.equals("python3")){
                cfReq.setHandler("index.handler");
                code = new Code().setDir(CODE_DIR[1]);
                cfReq.setCode(code);
            }
            if(runtime.equals("java8")){
                cfReq.setHandler("example.JavaTest::handleRequest");
                cfReq.setCode(new Code().setZipFile(getBufferinfo(CODE_DIR[2])));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        cfReq.setTimeout(10);
        CreateFunctionResponse cfResp = fcClient.createFunction(cfReq);
        System.out.println("Created function, request ID " + cfResp.getRequestId());
    }

    public static void createfunctionByZip(String service_name,String function_name,String function_des,int memorySize,String runtime, String zipfile ){
        CreateFunctionRequest cfReq = new CreateFunctionRequest(service_name);
        cfReq.setFunctionName(function_name);
        cfReq.setDescription(function_des);
        cfReq.setMemorySize(memorySize);
        cfReq.setRuntime(runtime);
        cfReq.setHandler("index.handler");
        cfReq.setCode(new Code().setZipFile(getBufferinfo(zipfile)));
        cfReq.setTimeout(60);
        CreateFunctionResponse cfResp = fcClient.createFunction(cfReq);
        System.out.println("Created function, request ID " + cfResp.getRequestId());
    }

    public static byte[] getBufferinfo(String jarFilePath){
        File jarFile = new File(jarFilePath);
        byte[] buffer = new byte[(int) jarFile.length()];
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(jarFilePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            fis.read(buffer);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

       return buffer;
    }



    public static void invokeFunction(String service_name,String function_name){
        InvokeFunctionRequest invkReq = new InvokeFunctionRequest(service_name, function_name);
        String payload = "Hello FunctionCompute!";
        invkReq.setPayload(payload.getBytes());
        invkReq.setInvocationType(Const.INVOCATION_TYPE_ASYNC);
        //invkReq.setLogType("Tail");
        //invkReq.setLogType("None");
        InvokeFunctionResponse invkResp = fcClient.invokeFunction(invkReq);

        System.out.println(new String(invkResp.getContent()));


    }

    public static void deleteFunction(String service_name,String function_name){
        // Delete the function
        DeleteFunctionRequest dfReq = new DeleteFunctionRequest(service_name, function_name);
        DeleteFunctionResponse dfResp = fcClient.deleteFunction(dfReq);
        System.out.println("Deleted function, request ID " + dfResp.getRequestId());
    }

    public static void deleteService(String service_name){
        // Delete the service
        DeleteServiceRequest dsReq = new DeleteServiceRequest(service_name);
        DeleteServiceResponse dsResp = fcClient.deleteService(dsReq);
        System.out.println("Deleted service, request ID " + dsResp.getRequestId());
    }


    public static String invokeFunctionLog(String service_name,String function_name,String savedName) {
        InvokeFunctionRequest invkReq = new InvokeFunctionRequest(service_name, function_name);

        invkReq.setLogType("Tail");
   
        String loginfo=null;
        long start = new Date().getTime();
        InvokeFunctionResponse invkResp = fcClient.invokeFunction(invkReq);
        if(invkResp.getStatus() == 200){
            long end = new Date().getTime();
          
            loginfo = invkResp.getLogResult();
            System.out.println(invkResp.getPayload());
        
            loginfo = loginfo + "\n" + "startTime: " + start;
            loginfo = loginfo + "\n" + "endTime: " + end;
            
            LogUtil.put(savedName, loginfo, true);
            System.out.println("====duration: " + ( end - start));
            System.out.println(loginfo);
        }
        return loginfo;
    }

  
    public static void createFunctionByBenchmark(String service_name,String function_name,String function_des,int memorySize,String runtime,String dirLoc,String handlerStr ){
        CreateFunctionRequest cfReq = new CreateFunctionRequest(service_name);
        cfReq.setFunctionName(function_name);
        cfReq.setDescription(function_des);
        cfReq.setMemorySize(memorySize);
        cfReq.setRuntime(runtime);
        Code code = null;
        cfReq.setHandler(handlerStr);
        try {
            code = new Code().setDir(dirLoc);
        } catch (IOException e) {
            e.printStackTrace();
        }
        cfReq.setCode(code);
        cfReq.setTimeout(60);
        CreateFunctionResponse cfResp = fcClient.createFunction(cfReq);
        System.out.println("Created function, request ID " + cfResp.getRequestId());
    }

    public static void main(final String[] args) throws IOException, InvalidKeyException, IllegalStateException {
//        createService("test-service1");
 //         createFunction("new","function333","test a function",128,"java8" );
//        //invokeFunction("test-service","test_function444");
//        //invokeFunctionLog("test-service","test_function444");
//        //deleteFunction("test-service","test_function444");
//        //deleteService("test-service");
//        String loginfo=invokeFunctionLog("test-service1","consolefunction1","consolefunction1-testlog");
//        System.out.println(loginfo);

        //-----------------------------------------------
        //createFunction("service-experiment","test","test-hello",128,"python3");
        //invokeFunctionLog("service-experiment","test","service-experiment-test-savelog");
        //deleteFunction("service-experiment","test");
        //invokeFunction("service-experiment","test");

        //createFunction("new","function333","test a function",128,"java8" );
        //invokeFunctionLog("new","function333","new-function333-savelog");
        //createFunctionByBenchmark("service-experiment","fibonacciTest","test fibonacci function",512,"python3","tmp/fc_code_python/fibonacci","index.handler" );
        //invokeFunctionLog("service-experiment","fibonacciTest","fibonacciTest-savelog");
        //-----------------------------------------
        //test zip create
        //createfunctionByZip("test-service","ziptest","test ziptest",512,"python3","tmp/fc_code_python/imageprocessing/ziptest.zip");
        //invokeFunction("test-service","ziptest");
    }
}