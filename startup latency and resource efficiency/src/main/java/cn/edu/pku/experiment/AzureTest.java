package cn.edu.pku.experiment;

import cn.edu.pku.adapter.lifecycle.AzureActivity;
import cn.edu.pku.adapter.lifecycle.GoogleActivity;
import cn.edu.pku.adapter.log.AzureLog;
import cn.edu.pku.adapter.log.LogUtil;
import cn.edu.pku.model.TestFunction;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class AzureTest {


    public static String[] functionApps = new String[]{
            "liuyibase",
            "liuyifloat",
            "liuyiimageprocessing",
            "liuyiinpack",
            "liuyimatrix",
            "liuyisequentiaio",
            "liuyicnn",
            "liuyirnn",
            "liuyimllrtraining",
            "liuyimllrprediction",
            "liuyimemory"
    };

    public static String floatParam = "N=%s";
    public static String linpackParam = "N=%s";
    public static String randomIOParam = "file_size=%s&byte_size=%s";
    public static String rnnParam = "account_name=slstorageinaz&account_key=M/QtOLcwk/E2c6Y0z/VnDfYX0uUKGyMDErfk7p6cJ40iufjRDB6WcAmwfjJtj7ahlBRydqayI6Cy0PyAiUWcig==&container_name=test&model_parameter_blob_name=rnn_params.pkl&model_blob_name=rnn_model.pth&language=English&start_letters=%s";
    public static String cnnParam = "account_name=slstorageinaz&account_key=M/QtOLcwk/E2c6Y0z/VnDfYX0uUKGyMDErfk7p6cJ40iufjRDB6WcAmwfjJtj7ahlBRydqayI6Cy0PyAiUWcig==&container_name=test&blob_name=animal-dog.jpg&model_blob_name=squeezenet_weights_tf_dim_ordering_tf_kernels.h5";

    public static String codeStartTest(){
        String functionApp = functionApps[TestFunction.Base.getIndex()];
        String testDate = null;
        //TODO new function app
//        try {
//            Thread.sleep(15*1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        String executeIds = AzureActivity.getCodeStart(functionApp,testDate);
        System.out.println(executeIds);
        //TODO delete function App
        return executeIds;
    }


    public static String codeStartTestTemporal(int index){
        String functionApp = functionApps[TestFunction.Base.getIndex()] + index;
        String testDate = null;
        String saveName = LogUtil.AZURE_PLATFORM + "_" + LogUtil.COLD_START_TEST + "_"+LogUtil.RESPONSE+"_" + index;
        String executeIds = AzureActivity.getCodeStartTemporal(functionApp,testDate,saveName);
        System.out.println(executeIds);
        return executeIds;
    }

    //multiple rounds, get avaerage value
    public static void patchTest(int iter, String functionName, String data){

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
            executorService.execute(new SingleTask(cyclicBarrier, i, functionName, data));
        }
    }





    public static class SingleTask implements Runnable{
        CyclicBarrier cyclicBarrier;
        int index;
        String functionName;
        String data;

        public SingleTask(CyclicBarrier cyclicBarrier, int index, String functionName, String data){
            this.cyclicBarrier = cyclicBarrier;
            this.index = index;
            this.functionName = functionName;
            this.data = data;
        }

        @Override
        public void run() {
            try {
                String result = codeStartTestTemporal(index+1);
                LogUtil.put(LogUtil.AZURE_PLATFORM + "_" + LogUtil.COLD_START_TEST + "_2_" + index, result, true);
                cyclicBarrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }


    public static void coldStartTest(){
        //        patchTest(10, null, null);

        List<Long> results = new ArrayList<>();
        for(int i = 0; i < 10; i++){
            String appId = AzureActivity.getApplicationID(functionApps[TestFunction.Base.getIndex()]+(i+1));
            String logkey = LogUtil.AZURE_PLATFORM + "_" + LogUtil.COLD_START_TEST + "_" + i;
            String value = LogUtil.getValue(logkey);
            long codeStartTime = AzureActivity.getCodeStartTime(appId,value);
            System.out.println(logDetail);
            results.add(codeStartTime);
        }
        System.out.println( new Gson().toJson(results));
        LogUtil.put(LogUtil.AZURE_PLATFORM + "_" + LogUtil.COLD_START_TEST, new Gson().toJson(results), true);
    }


    public static String roundTest(String name, String saveName){

        AzureActivity.getCodeStartTemporal(name, "N=1000", "Azure_linpack");

        String appId = AzureActivity.getApplicationID(name);
        String logkey = LogUtil.AZURE_PLATFORM + "_" + LogUtil.LINPACK_TEST;
        String value = LogUtil.getValue(logkey);
        System.out.println(value);
        long codeStartTime = AzureActivity.getCodeStartTime(appId,value);
        return value;
    }

    public static void main(String[] args){
        roundTest(functionApps[TestFunction.RANDOMIO.getIndex()], "");
    }
}
