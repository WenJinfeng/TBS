package cn.edu.pku.backend.action;

import com.google.gson.Gson;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SyncResult {
	Map<String, ResultCallback> waitObj = new HashMap<String, ResultCallback>();
	static Gson gson = new Gson();
	final public static HashedWheelTimer timer = new HashedWheelTimer(Executors.defaultThreadFactory(), 5,
			TimeUnit.MILLISECONDS, 2);

	public void wakeUp(String requestID, String result) {
		ResultCallback ob = waitObj.get(requestID);
		waitObj.remove(requestID);
		if (ob != null)
			ob.onResult(result);
	}

	public void sleep(final String requestID, ResultCallback cb) {
		if (!waitObj.containsKey(requestID)) {
			waitObj.put(requestID, cb);
		}
		TimerTask tt = new TimerTask() {
			@Override
			public void run(Timeout timeout) {
 				wakeUp(requestID, "Timeout");
			}
		};
		timer.newTimeout(tt, 10, TimeUnit.SECONDS);
	}

	public void sleepWithTimeout(final String requestID, ResultCallback cb, int timeOut) {
		if (!waitObj.containsKey(requestID)) {
			waitObj.put(requestID, cb);
		}
		TimerTask tt = new TimerTask() {
			@Override
			public void run(Timeout timeout) {
 				wakeUp(requestID, "Timeout");
			}
		};
		timer.newTimeout(tt, timeOut, TimeUnit.SECONDS);
	}
	static class Result{
		String r = "Timeout";
	}
	public String syncSleep(String reuqestID) {
		final Result cr = new Result();
		ResultCallback cb = new ResultCallback() {
			@Override
			public void onResult(String str) {
				cr.r = str;
				synchronized (this) {
					this.notifyAll();
				}
			}
		};
		sleep(reuqestID, cb);
		synchronized (cb) {
			try {
				cb.wait(20000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return cr.r;
	}
}