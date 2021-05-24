package cn.edu.pku.backend.action;


import cn.edu.pku.adapter.log.LogUtil;
import cn.edu.pku.model.AliFCLogResult;
import cn.edu.pku.model.LambdaLogResult;
import cn.edu.pku.model.MemoryTestData;
import cn.edu.pku.model.OrchestratorTestData;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.util.*;


public class BenckmarkActions {

	@Action(async = false)
	public void hello(JsonObject arg, final ResultCallback cb) {
		cb.onResult("hello");
	}

	@Action(async = false)
	public void concurrencyTest(JsonObject arg, final ResultCallback cb){
		String functionName = "node5-concurrency";
		Map<String, Integer> results = new HashMap<>();
		int iter = 20;
		for(int index = 1; index <= iter; index++){
			Set<String> logStreamNameSet = new HashSet<>();
			System.out.println(index);
			for(int i = 0; i < index; i++){
				String logKey = functionName + "_" + index + "_" + i;
				LambdaLogResult lambdaLogResult = LambdaLogResult.fromLog(LogUtil.getValue(logKey));
				logStreamNameSet.add(lambdaLogResult.logStreamName);
			}
			results.put(index + "", logStreamNameSet.size());
		}
		cb.onResult(new Gson().toJson(results));
	}


	@Action(async = false)
	public void coldStartTest(JsonObject arg, final ResultCallback cb){
		List<List<Long>> results = new ArrayList<>();
		String google_result = LogUtil.getValue(LogUtil.GOOGLE_PLATFORM + "_" + LogUtil.COLD_START_TEST);
		System.out.println(google_result);
		if(google_result != null){
			results.add(new Gson().fromJson(google_result, new TypeToken<List<Long>>(){}.getType()));
		}

		String azure_result = LogUtil.getValue(LogUtil.AZURE_PLATFORM + "_" + LogUtil.COLD_START_TEST);
		if(azure_result != null){
			results.add(new Gson().fromJson(azure_result, new TypeToken<List<Long>>(){}.getType()));
		}
		System.out.println(azure_result);



		String aws_result = LogUtil.getValue(LogUtil.AWS_PLATFORM + "_" + LogUtil.COLD_START_TEST);
		if(aws_result != null){
			results.add(new Gson().fromJson(aws_result, new TypeToken<List<Long>>(){}.getType()));
		}
		System.out.println(aws_result);



		System.out.println(new Gson().toJson(results));

		cb.onResult(new Gson().toJson(results));
	}


	@Action(async = false)
	public void packageSizeTest(JsonObject arg, final ResultCallback cb){



		List<List<List<Long>>> results = new ArrayList<>();

		String logKey  = LogUtil.AWS_PLATFORM + "_" + LogUtil.PACKAGE_SIZE_TEST;
		String value = LogUtil.getValue(logKey);
		results.add(new Gson().fromJson(value, new TypeToken<List<List<Long>>>(){}.getType()));


		logKey = LogUtil.GOOGLE_PLATFORM + "_" + LogUtil.PACKAGE_SIZE_TEST + "_1";
		value = LogUtil.getValue(logKey);
		List<List<Long>> google = new Gson().fromJson(value, new TypeToken<List<List<Long>>>(){}.getType());
		for(List<Long> item : google){
			for(int i = item.size() - 1; i >= 0; i--){
				if(item.get(i) < 0)
					item.remove(i);
			}
		}
		results.add(google);

		System.out.println(new Gson().toJson(results));
		cb.onResult(new Gson().toJson(results.get(0)));
	}


	@Action(async = false)
	public void memorySizeTest(JsonObject arg, final ResultCallback cb){
		List<List<List<Long>>> results = new ArrayList<>();
		String logKey = "";
		String value = "";

		logKey  = 	LogUtil.AWS_PLATFORM + "_" + LogUtil.MEMORY_TEST;
		value = LogUtil.getValue(logKey);
		results.add(new Gson().fromJson(value, new TypeToken<List<List<Long>>>(){}.getType()));


		logKey  = 	LogUtil.AWS_PLATFORM + "_" + LogUtil.MEMORY_TEST+"_1";
		value = LogUtil.getValue(logKey);
		results.add(new Gson().fromJson(value, new TypeToken<List<List<Long>>>(){}.getType()));


		logKey  = 	LogUtil.GOOGLE_PLATFORM + "_" + LogUtil.MEMORY_TEST;
		value = LogUtil.getValue(logKey);
		results.add(new Gson().fromJson(value, new TypeToken<List<List<Long>>>(){}.getType()));


		logKey  = 	LogUtil.GOOGLE_PLATFORM + "_" + LogUtil.MEMORY_TEST + "_1";
		value = LogUtil.getValue(logKey);
		results.add(new Gson().fromJson(value, new TypeToken<List<List<Long>>>(){}.getType()));


		System.out.println(new Gson().toJson(results));
		cb.onResult(new Gson().toJson(results));
	}


	@Action(async = false)
	public void memoryTest(JsonObject arg, final ResultCallback cb){
		String function = "python-hello";
		String functionName;
		LambdaLogResult lambdaLogResult;
		String log;
		int[] memories = new int[]{128,256,384,512,640,1024};
		List<MemoryTestData> results = new ArrayList<>();
		results.add(new MemoryTestData());
		results.add(new MemoryTestData());
		results.add(new MemoryTestData());
		results.add(new MemoryTestData());

		results.get(0).title = "start time";
		results.get(1).title = "execution time";
		results.get(2).title = "start time";
		results.get(3).title = "execution time";
		for(int i = 0; i < memories.length; i++) {
			int memory = memories[i];
			function = "python-hello";
			functionName = function + "-" + memory;
			log = LogUtil.getValue(functionName);
			lambdaLogResult = LambdaLogResult.fromLog(log);
			results.get(0).x.add(memory+"");
			results.get(1).x.add(memory+"");
			results.get(0).y.add(lambdaLogResult.duration);
			results.get(1).y.add(lambdaLogResult.initDuration);

			function = "java-hello";
			functionName = function + "-" + memory;
			log = LogUtil.getValue(functionName);
			lambdaLogResult = LambdaLogResult.fromLog(log);
			results.get(2).x.add(memory+"");
			results.get(3).x.add(memory+"");
			results.get(2).y.add(lambdaLogResult.duration);
			results.get(3).y.add(lambdaLogResult.initDuration);
		}

		cb.onResult(new Gson().toJson(results));
	}

	@Action(async = false)
	public void memoryAliTest(JsonObject arg, final ResultCallback cb){
		String function = "python3-hello";
		String functionName;
		AliFCLogResult aliLogResult;
		String log;
		int[] memories = new int[]{128,256,384,512,640,1024};
		List<MemoryTestData> results = new ArrayList<>();
		results.add(new MemoryTestData());
		results.add(new MemoryTestData());
		results.add(new MemoryTestData());
		results.add(new MemoryTestData());
		results.add(new MemoryTestData());
		results.add(new MemoryTestData());

		results.get(0).title = "execution time";
		results.get(1).title = "execution time";
		results.get(2).title = "execution time";
		results.get(3).title = "execution time";
		results.get(4).title = "execution time";
		results.get(5).title = "execution time";

		for(int i = 0; i < memories.length; i++) {
			int memory = memories[i];
			function = "nodejs6-hello";
			functionName = function + "-" + memory;
			log = LogUtil.getValue(functionName);
			aliLogResult = AliFCLogResult.fromLogNode(log);
			results.get(0).x.add(memory+"");
			results.get(1).x.add(memory+"");
			results.get(0).y.add(aliLogResult.duration);
			results.get(1).y.add(aliLogResult.duration);

			function = "python3-hello";
			functionName = function + "-" + memory;
			log = LogUtil.getValue(functionName);
			aliLogResult = AliFCLogResult.fromLogPython(log);
			results.get(2).x.add(memory+"");
			results.get(3).x.add(memory+"");
			results.get(2).y.add(aliLogResult.duration);
			results.get(3).y.add(aliLogResult.duration);

			function = "java8-hello";
			functionName = function + "-" + memory;
			log = LogUtil.getValue(functionName);
			aliLogResult = AliFCLogResult.fromLogJava(log);
			results.get(4).x.add(memory+"");
			results.get(5).x.add(memory+"");
			results.get(4).y.add(aliLogResult.duration);
			results.get(5).y.add(aliLogResult.duration);
		}

		cb.onResult(new Gson().toJson(results));
	}

}

