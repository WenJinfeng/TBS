package cn.edu.pku.experiment;

import cn.edu.pku.adapter.lifecycle.GoogleActivity;
import cn.edu.pku.adapter.lifecycle.LambdaActivity;
import cn.edu.pku.adapter.log.LogUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.microsoft.azure.storage.core.Base64;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class GCFTest {


    static String[] memories = new String[]{"128MB", "256MB", "512MB", "1024MB", "2048MB"};

    static String function_prefix = "projects/serverlessfunly/locations/us-east1/functions/";

    public static String PREFIX = "GOOGLE";

    public static String CODESTART = PREFIX + "CODE_START";
    public static String CODESTART_EXECUTE_IDS = PREFIX + "CODE_START_IDS";

    public static String MEMORY = PREFIX + "MEMORY";



    public static String getRandomString(int length){
        String str="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random=new Random();
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<length;i++){
            int number=random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }


    static String defaultMemory = "512MB";
    static String defaultPath = "/Users/vector/Desktop/poppy/ServerlessBenchmark/ServerlessBenchmark/resources/benchmark/python/base/google";

    public static String coldStartTest(String name, String data, String memory, String localPath){
        String salt = getRandomString(7);
        String functionName = name + "-" + salt;
        GoogleActivity.deployFunctionWithCLI(functionName, "function_handler", "python37",memory , "trigger-http"
                ,localPath, "540");
        try {
            Thread.sleep(15*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String executeIds = GoogleActivity.getCodeStart(functionName,data);
        System.out.println(executeIds);
        GoogleActivity.deleteCloudFunction(function_prefix + functionName);
        return executeIds;
    }

    //执行一次内存测试，返回log,好像和codeStartTest一样，这些代码需要重构下，现在case by case写不好修改
    public static String memoryTest(String name, String data, String memory, String localPath){
        String salt = getRandomString(7);
        String functionName = name + "-" + salt;
        GoogleActivity.deployFunctionWithCLI(functionName, "function_handler", "python37", memory, "trigger-http"
                , localPath, "540");
        try {
            Thread.sleep(15*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String executeId = GoogleActivity.getCodeStart(functionName,data);
        System.out.println(executeId);
        GoogleActivity.deleteCloudFunction(function_prefix + functionName);
        return executeId;
    }

    //package size
    public static String packageSizeTest(String name, String data, String memory, String localPath){
        return coldStartTest(name,data, memory, localPath);
    }


    //一次跑多少轮，最后取平均值 TODO 这里并发好像有点问题，每个函数应该就启动一个实例，这样连续两次请求才是在一个实例上进行的，不然会串？并发的应该是不同类型的实例
    public static void patchTest(int iter, String functionName, String data, String memory, String localPath){

        //创建一个可重用固定线程数的线程池，以共享的无界队列方式来运行这些线程，超出的线程会在队列中等待。
        ExecutorService executorService = Executors.newFixedThreadPool(iter);
        CyclicBarrier cyclicBarrier = new CyclicBarrier(iter, new Runnable() {
            @Override
            public void run() {
                //实验结束
                System.out.println("====test done=====");
                executorService.shutdown();
            }
        });
        AtomicInteger iteration = new AtomicInteger(0);
        for (int i = 0; i < iter; i++) {
            executorService.execute(new SingleTask(cyclicBarrier, i, functionName, data,memory, localPath));
        }
    }





    public static class SingleTask implements Runnable{
        CyclicBarrier cyclicBarrier;
        int index;
        String functionName;
        String data;

        String memory;
        String localPath;


        public SingleTask(CyclicBarrier cyclicBarrier, int index, String functionName, String data){
            this.cyclicBarrier = cyclicBarrier;
            this.index = index;
            this.functionName = functionName;
            this.data = data;

            this.memory = defaultMemory;
            this.localPath = defaultPath;
        }


        public SingleTask(CyclicBarrier cyclicBarrier, int index, String functionName, String data, String memory, String localPath){
            this.cyclicBarrier = cyclicBarrier;
            this.index = index;
            this.functionName = functionName;
            this.data = data;
            this.memory = memory;
            this.localPath = localPath;
        }

        @Override
        public void run() {
            try {
                //TODO 各种任务任务
                //cold start

//                String result = codeStartTest(functionName,data,memory, localPath);
//                LogUtil.put(LogUtil.GOOGLE_PLATFORM + "_" + LogUtil.COLD_START_TEST + "_" + index, result, true);

                //memory test

//                String result = memoryTest(functionName,data,memory, localPath);
//                //LogUtil.put(LogUtil.GOOGLE_PLATFORM + "_" + LogUtil.MEMORY_TEST + "_" + memory + "_" + index, result, true);
//                LogUtil.put(LogUtil.GOOGLE_PLATFORM + "_" + LogUtil.MEMORY_TEST + "_2_" + memory + "_" + index, result, true);

                //package size test, 应该和路径相关，记日志需要根据路径区分
                String result = packageSizeTest(functionName,data,memory, localPath);
                LogUtil.put(LogUtil.GOOGLE_PLATFORM + "_" + LogUtil.PACKAGE_SIZE_TEST + "_" + Base64.encode(localPath.getBytes()) + "_" + index, result, true);


                cyclicBarrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }

    static void coldStartTestResult(){
        //patchTest(TestBase.iter, functionName, data);

        List<Long> results = new ArrayList<>();
        for(int i = 0; i < TestBase.iter; i++){
            String logkey = LogUtil.GOOGLE_PLATFORM + "_" + LogUtil.COLD_START_TEST + "_" + i;
            String value = LogUtil.getValue(logkey);
            System.out.println(value);
            long codeStartTime = GoogleActivity.getCodeStartTime(value);
            results.add(codeStartTime);
        }
        LogUtil.put(LogUtil.GOOGLE_PLATFORM + "_" + LogUtil.COLD_START_TEST, new Gson().toJson(results), true);
    }


    public static void generateMemoryTestData(String functionName, String localPath){
//        for(int i = 0; i < memories.length; i++) {
//            patchTest(TestBase.iter, functionName, null, memories[i], localPath);
//            try {
//                Thread.sleep(200 * 1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

        List<List<Long>> mems = new ArrayList<>();
        List<List<Long>> mems1 = new ArrayList<>();
        for(int i = 0; i < memories.length; i++) {
            List<Long> results = new ArrayList<Long>();
            List<Long> results1 = new ArrayList<Long>();
            for(int j = 0 ; j < TestBase.iter; j++) {
                String logKey = LogUtil.GOOGLE_PLATFORM + "_" + LogUtil.MEMORY_TEST + "_" + memories[i] + "_" + j;
                String value = LogUtil.getValue(logKey);
                if(value != null) {
                    results.add(GoogleActivity.getCodeStartTime(value));
                }

                logKey = LogUtil.GOOGLE_PLATFORM + "_" + LogUtil.MEMORY_TEST + "_2_" + memories[i] + "_" + j;
                value = LogUtil.getValue(logKey);
                if(value != null) {
                    results1.add(GoogleActivity.getCodeStartTime(value));
                }

            }
            mems.add(results);
            mems1.add(results1);
        }
        System.out.println( new Gson().toJson(mems));
        System.out.println( new Gson().toJson(mems1));

//        LogUtil.put(LogUtil.GOOGLE_PLATFORM + "_" + LogUtil.MEMORY_TEST, new Gson().toJson(mems), true);
//        LogUtil.put(LogUtil.GOOGLE_PLATFORM + "_" + LogUtil.MEMORY_TEST + "_1", new Gson().toJson(mems1), true);
    }


    public static void generatePSTestData(String functionName){
        String localPath = "/Users/vector/Desktop/poppy/ServerlessBenchmark/ServerlessBenchmark/resources/benchmark/python/base/google";
//        for(int i = 1; i <= 7; i++) {
//            patchTest(TestBase.iter, functionName, null, memories[0], localPath+i);
//            try {
//                Thread.sleep(100 * 1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

        List<List<Long>> pss = new ArrayList<>();
        for(int i = 1; i <= 7; i++) {
            List<Long> results = new ArrayList<Long>();
            for(int j = 0 ; j < TestBase.iter; j++) {
                String path = localPath+i;
                String logKey = LogUtil.GOOGLE_PLATFORM + "_" + LogUtil.PACKAGE_SIZE_TEST + "_" + Base64.encode(path.getBytes()) + "_" + j;
                String value = LogUtil.getValue(logKey);
                if(value != null) {
                    results.add(GoogleActivity.getCodeStartTime(value));
                }

            }
            pss.add(results);
        }
        System.out.println( new Gson().toJson(pss));

        LogUtil.put(LogUtil.GOOGLE_PLATFORM + "_" + LogUtil.PACKAGE_SIZE_TEST + "_1", new Gson().toJson(pss), true);
    }


    public static String baseTest(String name, String data, String memory, String localPath){
        String salt = getRandomString(7);
        String functionName = name + "-" + salt;
        GoogleActivity.deployFunctionWithCLI(functionName, "function_handler", "python37",memory , "trigger-http"
                ,localPath, "540");
        try {
            Thread.sleep(15*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String executeIds = GoogleActivity.getCodeStart(functionName,data);
        System.out.println(executeIds);
        GoogleActivity.deleteCloudFunction(function_prefix + functionName);
        return executeIds;
    }


    public static String applicationTest(String name, String data, String memory, String localPath){

        String salt = getRandomString(7);
        String functionName = name + "-" + salt;
        GoogleActivity.deployFunctionWithCLI(functionName, "function_handler", "python37",memory , "trigger-http"
                ,localPath, "540");
        try {
            Thread.sleep(15*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String executeIds = GoogleActivity.getCodeStart(functionName,data);
        GoogleActivity.deleteCloudFunction(function_prefix + functionName);
        return executeIds;
    }

    //"python37" "function_handler"

    //java8 functions.HelloWorld
    //nodejs10

    public static String roundTest(String name, String data, String memory, String localPath, String runtime,String entry){
        //每轮实现就新建函数，省的有影响
        String salt = getRandomString(7);
        String functionName = name + "-" + salt;
        GoogleActivity.deployFunctionWithCLI(functionName, entry , runtime ,memory ,"trigger-http"
                ,localPath, "540");
        try {
            Thread.sleep(5*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String executeIds = GoogleActivity.getCodeStart(functionName,data);
        System.out.println(executeIds);
        GoogleActivity.deleteCloudFunction(function_prefix + functionName);

        return executeIds;
    }


    public static void javaColdStart(){
        String functionName = "hellojava";
        String memory = memories[0];
        String localPath = "/Users/vector/Desktop/poppy/ServerlessBenchmark/ServerlessBenchmark/resources/benchmark/java/hello/google";

//        List<String> ids = new ArrayList<>();
//        for(int i = 0; i < memories.length; i++) {
//            memory = memories[i];
//            String id = roundTest(functionName, null, memory, localPath, "java11", "functions.HelloWorld");
//            ids.add(id);
//        }
//        LogUtil.put(LogUtil.GOOGLE_PLATFORM + "_java_memory_1", new Gson().toJson(ids), true);

        String ids = LogUtil.getValue(LogUtil.GOOGLE_PLATFORM + "_java_memory_1");
        System.out.println(ids);
        List<String> exeIds = new Gson().fromJson(ids, new TypeToken<List<String>>(){}.getType());
        for(String id : exeIds){
            GoogleActivity.getCodeStartTime(id);
        }

    }


    public static void pythonColdStart(){
        String functionName = "hellopython";
        String memory = memories[0];
        String localPath = "/Users/vector/Desktop/poppy/ServerlessBenchmark/ServerlessBenchmark/resources/benchmark/python/hello/google";

        List<String> ids = new ArrayList<>();
            for(int i = 0; i < memories.length; i++) {
            memory = memories[i];
            String id = roundTest(functionName, null, memory, localPath, "python37", "function_handler");
            ids.add(id);
        }
        LogUtil.put(LogUtil.GOOGLE_PLATFORM + "_python_memory_1", new Gson().toJson(ids), true);

//        String ids = LogUtil.getValue(LogUtil.GOOGLE_PLATFORM + "_python_memory_1");
//        System.out.println(ids);
//        List<String> exeIds = new Gson().fromJson(ids, new TypeToken<List<String>>(){}.getType());
//        for(String id : exeIds){
//            GoogleActivity.getCodeStartTime(id);
//        }

    }

    public static void nodeColdStart(){
        String functionName = "hellonode";
        String localPath = "/Users/vector/Desktop/poppy/ServerlessBenchmark/ServerlessBenchmark/resources/benchmark/nodejs/base/google";

        List<String> ids = new ArrayList<>();
        for(int i = 0; i < 1; i++) {
            String memory = memories[i];
            String id = roundTest(functionName, null, memory, localPath, "nodejs10", "helloHttp");
            ids.add(id);
        }
        LogUtil.put(LogUtil.GOOGLE_PLATFORM + "_java_memory_1", new Gson().toJson(ids), true);

//        String ids = LogUtil.getValue(LogUtil.GOOGLE_PLATFORM + "_node_memory_1");
//        System.out.println(ids);
//        List<String> exeIds = new Gson().fromJson(ids, new TypeToken<List<String>>(){}.getType());
//        for(String id : exeIds){
//            GoogleActivity.getCodeStartTime(id);
//        }

    }


    public static void sequentiaIOStart(){
        String functionName = "seqIO";
        String localPath = "/Users/vector/Desktop/poppy/ServerlessBenchmark/ServerlessBenchmark/resources/benchmark/python/io/sequentiaoio/google";
        String data = "{\"file_size\":20,\"byte_size\":512}";
        List<String> ids = new ArrayList<>();
        for(int i = 0; i < 1; i++) {
            String memory = memories[i];
            String id = roundTest(functionName, null, memory, localPath, "python37", "function_handler");
            ids.add(id);
        }

    }


    public static void memoryStart(){
        String functionName = "memory";
        String localPath = "/Users/vector/Desktop/poppy/ServerlessBenchmark/ServerlessBenchmark/resources/benchmark/python/memory/google";
        String data = "{\"index\":25}";
        List<String> ids = new ArrayList<>();
        for(int i = 3; i < memories.length; i++) {
            String memory = memories[i];
            String id = roundTest(functionName, null, memory, localPath, "python37", "function_handler");
            ids.add(id);
        }

    }

    public static void main(String[] args){
        String functionName = "float";
        String data = "{\"n\":10000}";
        String memory = memories[0];
        String localPath = "/Users/vector/Desktop/poppy/ServerlessBenchmark/ServerlessBenchmark/resources/benchmark/python/cpu/float/google";


        functionName = "linpack";
        data = "{\"N\":1000}";
        memory = memories[0];
        localPath = "/Users/vector/Desktop/poppy/ServerlessBenchmark/ServerlessBenchmark/resources/benchmark/python/cpu/inpack/google";


//        for(int i = 0; i < memories.length; i++) {
//            memory = memories[i];
//            roundTest(functionName, data, memory, localPath);
//        }

//        applicationTest(functionName, data, memory, localPath);

        //memoryTest("memory","{\"index\":10}" , "128MB");

//        String localPath = "/Users/vector/Desktop/poppy/ServerlessBenchmark/ServerlessBenchmark/resources/benchmark/python/base/google";
////        generateMemoryTestData("memory", localPath);
//        generatePSTestData("packagesize");


//        String result = "[[501,964,532,1062,566,678,665,711,502,847,657,864,909,405,771],[298,384,990,362,261,-685,-1034,293,314,307,438,344,304,206,1355],[247,60,318,99,120,106,174,189,176,-6,223,145,121,234,260],[88,3,-853,84,81,58,82,82,204,56,229,68,60,-7,100],[18,152,-6,-18,46,20,66,117,-38,-5,134,44,47,41,71]]";
//        LogUtil.put(LogUtil.GOOGLE_PLATFORM + "_" + LogUtil.MEMORY_TEST + "_1", result, true);

//        baseTest("base", null, memories[4], localPath);

        //javaColdStart();
 //       nodeColdStart();
//        pythonColdStart();
//        sequentiaIOStart();
        memoryStart();

    }
}
