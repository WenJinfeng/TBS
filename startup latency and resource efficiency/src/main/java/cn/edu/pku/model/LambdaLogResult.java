package cn.edu.pku.model;

import cn.edu.pku.adapter.log.LambdaLog;
import cn.edu.pku.adapter.log.LogUtil;

import java.util.Map;

public class LambdaLogResult {
    public String requestId;
    public String logStreamName;
    public double duration;
    public double billedDuration;
    public double memorySize;
    public double memoryUsed;
    public double initDuration;

    public static LambdaLogResult fromLog(String log){
        Map<String, String> items = LambdaLog.parseLog(log);
        LambdaLogResult result = new LambdaLogResult();
        result.requestId = items.get("RequestId");
        result.logStreamName = items.get("logStreamName");
        result.duration = getTime(items.get("Duration"));
        result.billedDuration = getTime(items.get("Billed Duration"));
        result.initDuration = getTime(items.get("Init Duration"));
        result.memorySize = getMemory(items.get("Memory Size"));
        result.memoryUsed = getMemory(items.get("Max Memory Used"));
        return result;
    }


    static double getTime(String time){
        if(time == null || time.length() == 0)
            return -1;
        time = time.replace("ms", "").trim();
        return Double.parseDouble(time);
    }

    static double getMemory(String memory){
        if(memory == null || memory.length() == 0)
            return -1;
        memory = memory.replace("MB", "").trim();
        return Double.parseDouble(memory);
    }

    public static void main(String[] args) {

//        String log = LogUtil.getValue("nodejs6-hello"+ "-" + 128);
//        System.out.println(log);
        //LambdaLogResult lambdaLogResult=LambdaLogResult.fromLog(log);
        //System.out.println(lambdaLogResult.billedDuration);
    }
}
