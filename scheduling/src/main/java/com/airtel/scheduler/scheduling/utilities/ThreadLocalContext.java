package com.airtel.scheduler.scheduling.utilities;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ThreadLocalContext {

    private ThreadLocalContext () { }

    private static ThreadLocal<Map<String, Object>> threadContext = ThreadLocal.withInitial(HashMap::new);

    public static void put(String key, Object data) {
        threadContext.get().put(key, data);
    }

    public static Map<String, Object> getThreadLocalData() {
        return Collections.unmodifiableMap((Map) threadContext.get());
    }

    public static void clear() {
        threadContext.remove();
    }
}