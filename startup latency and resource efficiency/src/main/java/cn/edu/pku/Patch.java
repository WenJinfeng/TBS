package cn.edu.pku;

import cn.edu.pku.adapter.lifecycle.GoogleActivity;
import cn.edu.pku.adapter.lifecycle.LambdaActivity;
import cn.edu.pku.adapter.log.LogUtil;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Patch {

    static int[] memories = new int[]{128,256,512,1024,2048};

    public static void patchMemoryCreate(String runtime, String function, String handler){
        for(int i = 0; i < memories.length; i++) {
            int memory = memories[i];
            String s3key = "lambda/" + function;
            String functionName = function + "-" + memory;
//            new LambdaActivity().deleteFunction(functionName, null);
            try {
                LambdaActivity.createFunctionWithS3(functionName, runtime, null, memory, handler, s3key);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void patchMemoryRun(String function){
        for(int i = 0; i < memories.length; i++) {
            int memory = memories[i];
            String functionName = function + "-" + memory;
            LambdaActivity.invokeFunctionSync(functionName);
        }
    }


    public static void patchMemoryGetLog(String function){
        for(int i = 0; i < memories.length; i++) {
            int memory = memories[i];
            String functionName = function + "-" + memory;
            System.out.println(LogUtil.getValue(functionName));
        }
    }

    public static void initFuncInOnePlatform(int platform, String functionName){
        switch (platform){
            case 0:
                //AWS
                try {
                    LambdaActivity.createFunctionWithS3(functionName, "nodejs12.x", null, 256, "handler.handler", "lambda/node-concurrency");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case 1:
                //Google
                GoogleActivity.deployFunctionWithCLI(functionName, "function_handler", "python37", "512MB", "trigger-http"
                        , "/Users/vector/Desktop/poppy/ServerlessBenchmark/ServerlessBenchmark/resources/benchmark/python/cpu/float/google", "540");
                break;
            case 2:
                //Azure
                break;
            case 3:
                //Aliyun
                break;
            default:
                break;
        }
    }


    public static void deleteFuncInOnePlatform(int platform, String functionName){
        switch (platform){
            case 0:
                //AWS
                new LambdaActivity().deleteFunction(functionName, null);
                break;
            case 1:
                //Google
                String prefix = "projects/serverlessfunly/locations/us-central1/functions/";
                GoogleActivity.deleteCloudFunction(prefix + functionName);
                break;
            case 2:
                //Azure
                break;
            case 3:
                //Aliyun
                break;
            default:
                break;
        }
    }

    public static void invokeFuncInOnePlatform(int platform, String functionName, String data,  String saveName){
        switch (platform){
            case 0:
                //AWS
                LambdaActivity.invokeFunctionSync(functionName, data, saveName);
                break;
            case 1:
                //Google
                String result = GoogleActivity.invokeWithHttp(functionName, "{\"N\":5}");
                //{"latency": 3.743171691894531e-05, "instance": true}, if the instance if true means new instance
                LogUtil.put(saveName, result, true);
                break;
            case 2:
                //Azure
                break;
            case 3:
                //Aliyun
                break;
            default:
                break;
        }
    }

    public static int MAXITE = 100;


    public static void concurrencyTest(int iter, int platform, String functionName, String data){

        String tmpFunctionName = functionName + iter;
        System.out.println("======ite: " + iter + "=========");
        initFuncInOnePlatform(platform, tmpFunctionName);
        try {
            Thread.sleep(20*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ExecutorService executorService = Executors.newFixedThreadPool(iter);
        CyclicBarrier cyclicBarrier = new CyclicBarrier(iter, new Runnable() {
            @Override
            public void run() {
                deleteFuncInOnePlatform(platform, tmpFunctionName);
                executorService.shutdown();

                if(iter < MAXITE){
                    concurrencyTest(iter + 1, platform, functionName, data);
                }else{
                    return;
                }
            }
        });
        AtomicInteger iteration = new AtomicInteger(0);
        for (int i = 0; i < iter; i++) {
            executorService.execute(new SingleTask(cyclicBarrier, iter, i, tmpFunctionName,platform, data));
        }
    }


    public static class SingleTask implements Runnable{
        CyclicBarrier cyclicBarrier;
        int index;
        int count;
        String functionName;
        int platform;
        String data;

        public SingleTask(CyclicBarrier cyclicBarrier, int index, int count, String functionName, int platform,
                          String data){
            this.cyclicBarrier = cyclicBarrier;
            this.index = index;
            this.count = count;
            this.functionName = functionName;
            this.platform = platform;
            this.data = data;
        }

        @Override
        public void run() {
            try {
                invokeFuncInOnePlatform(platform,functionName, data,
                        functionName + "_" + platform + "_" + index + "_" + count);
                cyclicBarrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }



    public static void main(String[] args) throws IOException {
        patchMemoryCreate("python3.7", "python-hello", "handler.hello");
        patchMemoryCreate("nodejs12.x", "node-hello", "handler.hello");
        patchMemoryCreate("java8", "java-hello", "cn.edu.pku.Handler");
        concurrencyTest(1, 1, "float", "{\"N\":5}");

    }
}
