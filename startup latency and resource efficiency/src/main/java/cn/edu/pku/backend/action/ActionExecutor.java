package cn.edu.pku.backend.action;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class ActionExecutor<T, U> {
	static class Profiler {
		RandomAccessFile log;

		Profiler() {
			try {
				log = new RandomAccessFile("./log/profile.log", "rw");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public ExecutorService executor;
//	private Profiler profiler = new Profiler();

	public ActionExecutor(ExecutorService executor, Object... clzs) {
		this.executor = executor;
		initHandlers(clzs);
	}

	static class Pair<T, U> {
		T first;
		U second;

		public Pair(T f, U s) {
			first = f;
			second = s;
		}

	}

	private Map<String, Pair<Method, Object>> handlers;

	private Map<String, Pair<Method, Object>> initHandlers(Object... objects) {
		handlers = new HashMap<>();
		if (objects.length > 0) {
			for (Object obj : objects) {
				if (obj == null)
					continue;
				Method[] methods = obj.getClass().getDeclaredMethods();
				for (Method method : methods) {
					if (method.getAnnotation(Action.class) != null) {
						handlers.put(method.getName(), new Pair<Method, Object>(method, obj));
						if (!method.getReturnType().equals(Void.TYPE) || method.getParameterCount() != 2) {
							System.out.println("[FatelError] action ret is not void:"
									+ obj.getClass().getCanonicalName() + "-->" + method.getName());

							System.exit(0);
						}
					}
				}
			}
		}
		return handlers;
	}

	@SuppressWarnings("unchecked")
	public void handle(String action, final U args, final T callback)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		if (!handlers.containsKey(action)) {
			throw new IllegalArgumentException("unsupported action " + action);
		}
		long start = System.currentTimeMillis();
		final Pair<Method, Object> pair = handlers.get(action);
		Action actionAnno = pair.first.getAnnotation(Action.class);
		if (actionAnno.async()) {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						pair.first.invoke(pair.second, args, callback);
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			});
		} else
			pair.first.invoke(pair.second, args, callback);
		long end = System.currentTimeMillis();
		if (end - start > 100) {
			// try {
			// profiler.log.write((action + " " + (end - start) + "\n").getBytes());
			// } catch (IOException e) {
			// e.printStackTrace();
			// }
		}
		//System.out.println("[ActionExecutor] handle:" + action + " takes:" + (end - start));

		return;
	}
}