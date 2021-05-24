package cn.edu.pku.adapter.log;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : Cynthia
 * @date :
 */
public class AliFCLog {

    public static Map<String, String> parseAliLogNode(String logStr){
        String[] logs = logStr.split("\\n");
//        for(String log:logs){
//            System.out.println(log);
//        }
        Map<String, String> results = new HashMap<>();
        results.put("requestId",logs[0].split(":")[1]);
        String[] infos=logs[4].split(",");
//        System.out.println(infos.length);
//        for(String info:infos){
//            System.out.println(info);
//        }
        results.put("duration",infos[0].split(":")[1]);
        //System.out.println(infos[1]);
        results.put("billedDuration",infos[1].split(":")[1]);
        results.put("memorySize",infos[2].split(":")[1]);
        results.put("memoryUsed",infos[3].split(":")[1]);
        results.put("startTime",logs[5].split(":")[1]);
        results.put("endTime",logs[6].split(":")[1]);
        return results;
    }
    public static Map<String, String> parseAliLogpython(String logStr){
        String[] logs = logStr.split("\\n");
        Map<String, String> results = new HashMap<>();
        results.put("requestId",logs[0].split(":")[1]);
        String[] infos=logs[3].split(",");
        results.put("duration",infos[0].split(":")[1]);
        results.put("billedDuration",infos[1].split(":")[1]);
        results.put("memorySize",infos[2].split(":")[1]);
        results.put("memoryUsed",infos[3].split(":")[1]);
        results.put("startTime",logs[4].split(":")[1]);
        results.put("endTime",logs[5].split(":")[1]);
        return results;
    }
    public static Map<String, String> parseAliLogjava(String logStr){
        String[] logs = logStr.split("\\n");
        Map<String, String> results = new HashMap<>();
        results.put("requestId",logs[0].split(":")[1]);
        if(logs[2].isEmpty()){
            System.out.println("为null");
            String[] infos=logs[3].split(",");
            results.put("duration",infos[0].split(":")[1]);
            results.put("billedDuration",infos[1].split(":")[1]);
            results.put("memorySize",infos[2].split(":")[1]);
            results.put("memoryUsed",infos[3].split(":")[1]);
            results.put("startTime",logs[4].split(":")[1]);
            results.put("endTime",logs[5].split(":")[1]);
        }else{
            System.out.println("不为null");
            String[] infos=logs[2].split(",");
            results.put("duration",infos[0].split(":")[1]);
            results.put("billedDuration",infos[1].split(":")[1]);
            results.put("memorySize",infos[2].split(":")[1]);
            results.put("memoryUsed",infos[3].split(":")[1]);
            results.put("startTime",logs[3].split(":")[1]);
            results.put("endTime",logs[4].split(":")[1]);
        }

        return results;
    }
    public static void main(String[] args) {


        Map<String, String> results= parseAliLogjava("FC Invoke Start RequestId: 999483aa-3867-4369-ae37-9c069d3acc90\n" +
                "        FC Invoke End RequestId: 999483aa-3867-4369-ae37-9c069d3acc90\n" +
                "\n" +
                "        Duration: 82.57 ms, Billed Duration: 100 ms, Memory Size: 128 MB, Max Memory Used: 92.89 MB\n" +
                "        startTime: 1587097272489\n" +
                "        endTime: 1587097275207");
        System.out.println(results.get("requestId"));
        System.out.println(results.get("duration"));
        System.out.println((results.get("billedDuration")));
        System.out.println(results.get("memorySize"));
        System.out.println(results.get("memoryUsed"));

    }
}
