package cn.edu.pku.experiment;

import cn.edu.pku.Patch;
import cn.edu.pku.adapter.lifecycle.AliFCActivity;
import cn.edu.pku.adapter.lifecycle.GoogleActivity;
import cn.edu.pku.adapter.lifecycle.LambdaActivity;
import cn.edu.pku.adapter.log.LambdaLog;
import cn.edu.pku.adapter.log.LogUtil;
import cn.edu.pku.model.TestFunction;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class LambdaTest {


    public static String[] functionApps = new String[]{
            "liuyibase",
            "liuyifloat",
            "liuyiimageprocessing",
            "liuyiinpack",
            "liuyimatrix",
            "liuyirandomio",
            "liuyisequentiaio",
            "liuyicnn",
            "liuyirnn",
            "liuyimllrtraining",
            "liuyimllrprediction",
            "liuyimemory",
            "liuyihttp",
    };

    public static String[] deployZip = new String[]{
            "base.zip",
            "float.zip",
            "imageprocessing.zip",
            "linpack.zip",
            "matrixmt.zip",
            "randomio.zip",
            "sequentiaoio.zip",
            "liuyicnn",
            "liuyirnn",
            "liuyimllrtraining",
            "liuyimllrprediction",
            "memory.zip",
            "http.zip",
    };

    static int[] memories = new int[]{128,256,512,1024, 2048};


    public static void concurrencyTest(int iter){

    }


    public static String memoryTest(int memory, int iter, String packageZip){
        String functionName = functionApps[TestFunction.Base.getIndex()]  + memories[memory] + "_" + iter;
        try {
            LambdaActivity.createFunctionWithS3(functionName, "python3.7", null, memories[memory], "lambda_function.handler", packageZip);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(5*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String result = "";
        result = LambdaActivity.invokeFunctionSync(functionName, LogUtil.AWS_PLATFORM + "_" + LogUtil.MEMORY_TEST + "_2_" + memories[memory] + "_" + iter, null);
        new LambdaActivity().deleteFunction(functionName, null);
        return result;
    }

    public static String coldStartTest(int iter){
        String functionName = functionApps[TestFunction.Base.getIndex()] + iter;
        try {
            LambdaActivity.createFunctionWithS3(functionName, "python3.7", null, 512, "lambda_function.handler", "base.zip");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(5*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String result = "";
        String saveName = LogUtil.AWS_PLATFORM + "_" + LogUtil.COLD_START_TEST + "_" + iter;
        System.out.println(saveName);
        result = LambdaActivity.invokeFunctionSync(functionName,saveName , null);
        new LambdaActivity().deleteFunction(functionName, null);
        return result;
    }


    public static String psStartTest(int iter, String packageName, int memory, String functionName){
        functionName = functionName + "_" + iter;
        try {
            LambdaActivity.createFunctionWithS3(functionName, "python3.7", null, memory, "lambda_function.handler", packageName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(5*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String result = "";
        String saveName = LogUtil.AWS_PLATFORM + "_" + LogUtil.PACKAGE_SIZE_TEST + "_" + packageName+ "_" + memory +"_" + iter;
        System.out.println(saveName);
        result = LambdaActivity.invokeFunctionSync(functionName, saveName, null);
        new LambdaActivity().deleteFunction(functionName, null);
        return result;
    }



    public static void patchTest(int iter, String functionName, String data, String packageNam){

        ExecutorService executorService = Executors.newFixedThreadPool(iter);
        CyclicBarrier cyclicBarrier = new CyclicBarrier(iter, new Runnable() {
            @Override
            public void run() {
                System.out.println("====test done=====");
                executorService.shutdown();
            }
        });
        AtomicInteger iteration = new AtomicInteger(0);
        for (int i = 0; i < iter; i++) {
            executorService.execute(new SingleTask(cyclicBarrier, i, functionName, data,packageNam));
        }
    }


    public static void patchTestForPS(int iter, String functionName, String data, int memory, String packageName){

        ExecutorService executorService = Executors.newFixedThreadPool(iter);
        CyclicBarrier cyclicBarrier = new CyclicBarrier(iter, new Runnable() {
            @Override
            public void run() {
                System.out.println("====test done=====");
                executorService.shutdown();
            }
        });
        AtomicInteger iteration = new AtomicInteger(0);
        for (int i = 0; i < iter; i++) {
            executorService.execute(new SingleTask(cyclicBarrier, i, functionName, data, memory, packageName));
        }
    }


    static int memoryIndex;

    public static class SingleTask implements Runnable{
        CyclicBarrier cyclicBarrier;
        int index;
        String functionName;
        String data;

        int memory;
        String packageName;


        public SingleTask(CyclicBarrier cyclicBarrier, int index, String functionName, String data){
            this.cyclicBarrier = cyclicBarrier;
            this.index = index;
            this.functionName = functionName;
            this.data = data;
            this.packageName = "base.zip";
        }

        public SingleTask(CyclicBarrier cyclicBarrier, int index, String functionName, String data, String packageName){
            this.cyclicBarrier = cyclicBarrier;
            this.index = index;
            this.functionName = functionName;
            this.data = data;
            this.packageName = packageName;
        }

        public SingleTask(CyclicBarrier cyclicBarrier, int index, String functionName, String data, int memory, String packageName){
            this.cyclicBarrier = cyclicBarrier;
            this.index = index;
            this.functionName = functionName;
            this.data = data;

            this.memory = memory;
            this.packageName = packageName;
        }

        @Override
        public void run() {
            try {
                memoryTest(memoryIndex, index,packageName);


                cyclicBarrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }


    public static void generateMemoryTestData(){
//        for(int i = 0; i < memories.length; i++) {
//            memoryIndex = i;
//            patchTest(TestBase.iter, null, null, "base6.zip");
//        }

        List<List<Long>> mems = new ArrayList<>();
        for(int i = 0; i < memories.length; i++) {
            List<Long> results = new ArrayList<Long>();
            for(int j = 0 ; j < TestBase.iter; j++) {
                String logKey = LogUtil.AWS_PLATFORM + "_" + LogUtil.MEMORY_TEST + "_2_" + memories[i] + "_" + j;
                String value = LogUtil.getValue(logKey);
                System.out.println(value);
                if(value != null) {
                    Map<String, String> result = LambdaLog.parseLog(value);
                    long initime = (long) Float.parseFloat(result.get("Init Duration"));
                    results.add(initime);
                }
            }
            mems.add(results);
            System.out.println( new Gson().toJson(results));
        }
    }


    public static void packageSizeTest(){

        int memoryIndex = 0;
        //add size, do not load
        //String[] packageName = new String[]{"base1.zip", "base2.zip", "base3.zip", "base4.zip"};
        //add size, and load
        String[] packageName = new String[]{"base1.zip", "base2.zip", "base3.zip", "base4.zip","base5.zip","base6.zip", "base7.zip"};

        //test
//        for(int i = 0; i < packageName.length; i++) {
//            patchTestForPS(TestBase.iter, "base"+(i+1), null, memories[memoryIndex], packageName[i]);
//        }


        //get data
        List<List<Long>> pss = new ArrayList<>();
        for(int i = 0; i < packageName.length; i++) {
            List<Long> results = new ArrayList<Long>();
            for(int j = 0 ; j < TestBase.iter; j++) {
                String logKey = LogUtil.AWS_PLATFORM + "_" + LogUtil.PACKAGE_SIZE_TEST + "_" + packageName[i]+ "_" + memories[memoryIndex] +"_" + j;
                String value = LogUtil.getValue(logKey);
                if(value != null) {
                    Map<String, String> result = LambdaLog.parseLog(value);
                    long initime = (long) Float.parseFloat(result.get("Init Duration"));
                    results.add(initime);
                }
            }
            pss.add(results);
            System.out.println( new Gson().toJson(results));
        }
        LogUtil.put(LogUtil.AWS_PLATFORM + "_" + LogUtil.PACKAGE_SIZE_TEST, new Gson().toJson(pss), true);
    }


    public static void generateColdStartData(){
        //conduct experiments
        //patchTest(TestBase.iter, null,null);
        //extract the data
        List<Long> results = new ArrayList<Long>();
        for(int i = 0 ; i < TestBase.iter; i++){
            String logKey = LogUtil.AWS_PLATFORM + "_" + LogUtil.COLD_START_TEST + "_" + i;
            String value = LogUtil.getValue(logKey);
            Map<String, String> result = LambdaLog.parseLog(value);
            long initime = (long) Float.parseFloat(result.get("Init Duration"));
            results.add(initime);
        }
        System.out.println( new Gson().toJson(results));
        LogUtil.put(LogUtil.AWS_PLATFORM + "_" + LogUtil.COLD_START_TEST, new Gson().toJson(results), true);
    }


    public static String baseTest(){
        String functionName = functionApps[TestFunction.Base.getIndex()];
        try {
            LambdaActivity.createFunctionWithS3(functionName, "python3.7", null, 128, "lambda_function.handler", "base.zip");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(5*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String result = "";
        String saveName = LogUtil.AWS_PLATFORM + "_" + LogUtil.BASE_TEST;
        System.out.println(saveName);
        result = LambdaActivity.invokeFunctionSync(functionName,saveName , null);
        System.out.println(result);
        new LambdaActivity().deleteFunction(functionName, null);
        return result;
    }


    public static String floatTest(){
        String functionName = functionApps[TestFunction.FLOAT.getIndex()];
        try {
            LambdaActivity.createFunctionWithS3(functionName, "python3.7", null, 128, "lambda_function.handler", deployZip[1]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(5*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String result = "";
        String saveName = LogUtil.AWS_PLATFORM + "_" + LogUtil.FLOAT_TEST;
        System.out.println(saveName);
        result = LambdaActivity.invokeFunctionSync(functionName,saveName , "{\"n\":10000}");
        System.out.println(result);
        new LambdaActivity().deleteFunction(functionName, null);
        return result;
    }


    public static String languageTest(){
        Patch.patchMemoryCreate("nodejs12.x", "node-hello", "handler.hello");
        Patch.patchMemoryRun("node-hello");
        Patch.patchMemoryGetLog("node-hello");
        Patch.patchMemoryCreate("java8", "java-hello", "cn.edu.pku.Handler");
        Patch.patchMemoryRun("java-hello");
        Patch.patchMemoryGetLog("java-hello");

        Patch.patchMemoryCreate("python3.7", "python-hello", "handler.hello");
        Patch.patchMemoryRun("python-hello");
        Patch.patchMemoryGetLog("python-hello");

        return null;
    }




    public static String applicationTest(String functionName, String zip, String saveName, String payload, int memory){
        try {
            LambdaActivity.createFunctionWithS3(functionName, "python3.7", null, memory, "lambda_function.handler", zip);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(5*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String result = "";
        System.out.println(saveName);
        result = LambdaActivity.invokeFunctionSync(functionName,saveName , payload);
        System.out.println(result);
        new LambdaActivity().deleteFunction(functionName, null);
        return result;
    }



    public static String roundTest(String functionName, String zip, String saveName, String payload, String language, String handlerName, int memory){
        try {
            LambdaActivity.createFunctionWithS3(functionName, language, null, memory, handlerName, zip);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(5*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String result = "";
        System.out.println(saveName);
        result = LambdaActivity.invokeFunctionSync(functionName,saveName , payload);
        System.out.println(result);
        new LambdaActivity().deleteFunction(functionName, null);
        return result;
    }

    public static void main(String[] args){
        String functionName = functionApps[TestFunction.FLOAT.getIndex()];
        String zip = deployZip[1];
        String saveName = LogUtil.AWS_PLATFORM + "_" + LogUtil.FLOAT_TEST;
        String payload = "{\"n\":10000}";


        functionName = functionApps[TestFunction.MEMORY.getIndex()];
        zip = deployZip[TestFunction.MEMORY.getIndex()];
        saveName = LogUtil.AWS_PLATFORM + "_" + LogUtil.MEMORY_TEST;
        payload = "{\"index\":25}";

        for(int i = 1; i < memories.length; i++){
            roundTest(functionName + memories[i], zip, saveName + "_" + memories[i], payload, "python3.7", "lambda_function.handler", memories[i]);
        }



    }
}

