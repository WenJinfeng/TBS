package cn.edu.pku.model;

import cn.edu.pku.adapter.log.AliFCLog;
import cn.edu.pku.adapter.log.LambdaLog;
import cn.edu.pku.adapter.log.LogUtil;

import java.util.Map;

/**
 * @author : Cynthia
 * @date :
 */
public class AliFCLogResult {
    public String requestId;
    public double duration;
    public double billedDuration;
    public double memorySize;
    public double memoryUsed;
    public String startTime;
    public String endTime;


    public static AliFCLogResult fromLogNode(String log){
        Map<String, String> items = AliFCLog.parseAliLogNode(log);
        AliFCLogResult result = new AliFCLogResult();
        result.requestId = items.get("requestId");
        result.duration = getTime(items.get("duration"));
        result.billedDuration = getTime(items.get("billedDuration"));
        result.memorySize = getMemory(items.get("memorySize"));
        result.memoryUsed = getMemory(items.get("memoryUsed"));
        result.startTime=items.get("startTime");
        result.endTime=items.get("endTime");
        return result;
    }
    public static AliFCLogResult fromLogPython(String log){
        Map<String, String> items = AliFCLog.parseAliLogpython(log);
        AliFCLogResult result = new AliFCLogResult();
        result.requestId = items.get("requestId");
        result.duration = getTime(items.get("duration"));
        result.billedDuration = getTime(items.get("billedDuration"));
        result.memorySize = getMemory(items.get("memorySize"));
        result.memoryUsed = getMemory(items.get("memoryUsed"));
        result.startTime=items.get("startTime");
        result.endTime=items.get("endTime");
        return result;
    }
    public static AliFCLogResult fromLogJava(String log){
        Map<String, String> items = AliFCLog.parseAliLogjava(log);
        AliFCLogResult result = new AliFCLogResult();
        result.requestId = items.get("requestId");
        result.duration = getTime(items.get("duration"));
        result.billedDuration = getTime(items.get("billedDuration"));
        result.memorySize = getMemory(items.get("memorySize"));
        result.memoryUsed = getMemory(items.get("memoryUsed"));
        result.startTime=items.get("startTime");
        result.endTime=items.get("endTime");
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

//        String log = LogUtil.getValue("python3-hello"+ "-" + 128);
//        System.out.println(log);
//        AliFCLogResult aliLogResult=AliFCLogResult.fromLogPython(log);
//        System.out.println(aliLogResult.requestId);
//        System.out.println(aliLogResult.duration);
//        System.out.println(aliLogResult.billedDuration);
//        System.out.println(aliLogResult.memorySize);
//        System.out.println(aliLogResult.endTime);

    }
}
