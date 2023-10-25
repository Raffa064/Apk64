package apk64;

import java.util.HashMap;

public class Chrono {
	public static HashMap<String, Long> timers = new HashMap<>();
	
	public static void start(String name) {
		timers.put(name, System.nanoTime());
	}
	
	public static long end(String name) {
		long time = System.nanoTime() - timers.get(name);
		
		timers.remove(name);
		
		return time;
	}
}
