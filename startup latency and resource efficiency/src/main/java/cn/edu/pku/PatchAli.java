package cn.edu.pku;

import cn.edu.pku.adapter.lifecycle.AliFCActivity;
import cn.edu.pku.adapter.log.LogUtil;


import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;


public class PatchAli {
//    static int[] memories = new int[]{128, 256, 384, 512, 640, 1024};
        static int[] memories = new int[]{128, 256, 512, 1024, 2048};
    //runtime=["nodejs6","nodejs8","nodejs10","python2.7","python3","java8","php7.2"]
    static String service_name = "service-experiment";

    public static void patchAliMemoryCreate(String function_name, String function_des, String runtime) {
        for (int i = 0; i < memories.length; i++) {
            int memory = memories[i];
            String functionName = function_name + "-" + memory;
            AliFCActivity.createFunction(service_name, functionName, function_des, memory, runtime);

        }
    }

    public static void patchAliMemoryRun(String function) {
        for (int i = 0; i < memories.length; i++) {
            int memory = memories[i];
            String functionName = function + "-" + memory;
            AliFCActivity.invokeFunctionLog(service_name, functionName, functionName);
            AliFCActivity.invokeFunctionLog(service_name, functionName, functionName+"_1");

            AliFCActivity.deleteFunction(service_name,functionName);
        }
    }

    
    public static void patchAliMemoryGetLog(String function) {
        for (int i = 0; i < memories.length; i++) {
            int memory = memories[i];
            String functionName = function + "-" + memory;
            System.out.println(LogUtil.getValue(functionName));
            System.out.println(LogUtil.getValue(functionName+"_1"));
            System.out.println("=========================");
        }
    }

   public static void concurrencyAliTest(int iter) {

        String functionName = "nodejs6-concurrency";
        AliFCActivity.createFunction(service_name, functionName, "concurrencytest", 256, "nodejs6");
        ExecutorService executorService = Executors.newFixedThreadPool(iter);
        CyclicBarrier cyclicBarrier = new CyclicBarrier(iter, new Runnable() {
            @Override
            public void run() {
                AliFCActivity.deleteFunction(service_name, functionName);
                executorService.shutdown();
                if (iter < 60) {
                    concurrencyAliTest(iter + 1);
                } else {
                    return;
                }
            }
        });
        AtomicInteger iteration = new AtomicInteger(0);
        for (int i = 0; i < iter; i++) {
            executorService.execute(new PatchAli.SingleTask(cyclicBarrier, iter, i, functionName));
        }
    }


    public static class SingleTask implements Runnable {
        CyclicBarrier cyclicBarrier;
        int index;
        int count;
        String functionName;

        public SingleTask(CyclicBarrier cyclicBarrier, int index, int count, String functionName) {
            this.cyclicBarrier = cyclicBarrier;
            this.index = index;
            this.count = count;
            this.functionName = functionName;
        }

        @Override
        public void run() {
            try {
                AliFCActivity.invokeFunctionLog(service_name, functionName, functionName + "_" + index + "_" + count);
                cyclicBarrier.await();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            } catch (BrokenBarrierException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void patchAliBenchmarkTest(String service_name,String function_name,String function_des,int memorySize,String runtime,String dirLoc,String handlerStr ) {
        AliFCActivity.createFunctionByBenchmark(service_name,function_name,function_des,memorySize,runtime,dirLoc,handlerStr);
        System.out.println("create function finished!");
        AliFCActivity.invokeFunctionLog(service_name, function_name, function_name+"-savelog");
        System.out.println("get log information finished!");
        AliFCActivity.deleteFunction(service_name, function_name);

    }
    public static void patchAliMapReducer(){
        AliFCActivity.createFunctionByBenchmark("service-experiment","map","test mapreducer-map",512,"python3","tmp/fc_code_python/mapreducer/map","index.handler");
        System.out.println("create map function finished!");
        AliFCActivity.createFunctionByBenchmark("service-experiment","reducer","test mapreducer-reducer",512,"python3","tmp/fc_code_python/mapreducer/reducer","index.handler");
        System.out.println("create reducer function finished!");
        AliFCActivity.createFunctionByBenchmark("service-experiment","driver","test mapreducer-driver",512,"python3","tmp/fc_code_python/mapreducer/driver","index.handler");
        System.out.println("create driver function finished!");
        AliFCActivity.invokeFunctionLog("service-experiment","driver", "driver-savelog");
        System.out.println("get log information finished!");
    }
    public static void patchAliFunctionByZip(String service_name,String function_name,String function_des,int memorySize,String runtime,String dirLoc ) {
        AliFCActivity.createfunctionByZip(service_name,function_name,function_des,memorySize,runtime,dirLoc);
        System.out.println("create function finished!");
        AliFCActivity.invokeFunctionLog(service_name, function_name, function_name+"-savelog");
        System.out.println("get log information finished!");
    }
    public static void main(String[] args) {
//        AliFCActivity.createService("service-experiment");
//        patchAliMemoryCreate("nodejs6-hello-ly","node-hell0 world", "nodejs6");
//        patchAliMemoryCreate("python3-hello-ly","python-hello world", "python3");
////        patchAliMemoryCreate("java8-hello-ly","java-hello world", "java8");
////        patchAliMemoryRun("nodejs6-hello-ly");
//        patchAliMemoryRun("python3-hello-ly");
//        patchAliMemoryRun("java8-hello-ly");


//        patchAliMemoryGetLog("nodejs6-hello-ly");
//        patchAliMemoryGetLog("python3-hello-ly");
//        patchAliMemoryGetLog("java8-hello-ly");
        //concurrencyAliTest(1);

        //AliFCActivity.createFunction(service_name, "nodejs6-hello-new", "llll", 128, "nodejs6");
        //AliFCActivity.invokeFunctionLog(service_name, "nodejs6-hello-new", "nodejs6-hello-new");

//------------------------------------------------------------------------------
        //进行benchmark测试
        //fibonacci
        //patchAliBenchmarkTest("service-experiment", "fibonacciTest", "test fibonacci function", 512, "python3", "tmp/fc_code_python/fibonacci", "index.handler")
        //System.out.println(LogUtil.getValue("fibonacciTest"+ "-savelog"));

//        for(int i = 0; i < memories.length; i++){
//            patchAliBenchmarkTest("service-experiment", "fibonacciTest_3_" + memories[i], "test fibonacci function",  memories[i], "python3", "tmp/fc_code_python/fibonacci", "index.handler");
//            System.out.println(LogUtil.getValue("fibonacciTest"+ "-savelog"));
//        }
        //cpu-float
        //patchAliBenchmarkTest("service-experiment","cpu_float","test cpu_float",512,"python3","tmp/fc_code_python/cpu/float","index.handler");
        //System.out.println(LogUtil.getValue("cpu_float"+ "-savelog"));
        //cpu-inpack

//        for(int i = 0; i < memories.length; i++) {
//            patchAliBenchmarkTest("service-experiment", "cpu_inpack_1_" + memories[i], "test cpu_inpack", memories[i], "python3", "tmp/fc_code_python/cpu/inpack", "index.handler");
//            System.out.println(LogUtil.getValue("cpu_inpack" + "-savelog"));
//        }
        //cpu-matrix
        //patchAliBenchmarkTest("service-experiment","cpu_matrix","test cpu_matrix",512,"python3","tmp/fc_code_python/cpu/matrix","index.handler");
       //System.out.println(LogUtil.getValue("cpu_matrix"+ "-savelog"));
        //io-gzip
        //patchAliBenchmarkTest("service-experiment","io_gzip","test io_gzip",512,"python3","tmp/fc_code_python/io/gzip","index.handler");
        //System.out.println(LogUtil.getValue("io_gzip"+ "-savelog"));
        //io-sequentiaoio
//        for(int i = 0; i < memories.length; i++){
//            patchAliBenchmarkTest("service-experiment","io_sequentiaoio_2" + memories[i],"test io_sequentiaoio",memories[i],"python3","tmp/fc_code_python/io/sequentiaoio","index.handler");
//            System.out.println(LogUtil.getValue("io_sequentiaoio"+ "-savelog"));
//        }

        //net-http
        //patchAliBenchmarkTest("service-experiment","net_http","test net_http",512,"python3","tmp/fc_code_python/net/http","index.handler");
        //System.out.println(LogUtil.getValue("net_http"+ "-savelog"));
        //net-cloudstrage-oss
        //patchAliBenchmarkTest("service-experiment","net_cloudstorage","test net_cloudstorage",512,"python3","tmp/fc_code_python/net/cloudstorage","index.handler");
        //System.out.println(LogUtil.getValue("net_cloudstorage"+ "-savelog"));
        //---------------------------------------------------------------
        //mapreducer test
        //patchAliMapReducer();
        //System.out.println(LogUtil.getValue("driver"+ "-savelog"));
        //-------------------------------------------
        //zip upload to create and invoke function
        //imageprocess test
        //patchAliFunctionByZip("service-experiment","imageprocess","test imageprocess",512,"python3","tmp/fc_code_python/imageprocessing/imageprocess.zip");
        //System.out.println(LogUtil.getValue("imageprocess"+ "-savelog"));

        for(int i = 0; i < memories.length; i++){
            patchAliFunctionByZip("service-experiment","imageprocess" + memories[i],"test imageprocess",memories[i],"python3","tmp/fc_code_python/imageprocessing/imageprocess.zip");
            System.out.println(LogUtil.getValue("imageprocess"+ "-savelog"));
        }
        //videoprocess test
        //patchAliFunctionByZip("service-experiment","videoprocess","test videoprocess",512,"python2.7","tmp/fc_code_python/videoprocessing/videoprocess.zip");
        //System.out.println(LogUtil.getValue("videoprocess"+ "-savelog"));
    }
}
