package com.distsystem.api;

import com.distsystem.interfaces.Agentable;
import com.distsystem.utils.DistUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/** map with status and more info */
public class DistStatusMap extends HashMap<String, Object> {

    /** status created time */
    private final long startTime = System.currentTimeMillis();
    /** */
    public DistStatusMap() {
    }

    public DistStatusMap withStatus(String status) {
        put("status", status);
        put("totalTimeMs", (System.currentTimeMillis()-startTime));
        return this;
    }
    public DistStatusMap append(String key, String value) {
        put(key, value);
        put("totalTimeMs", (System.currentTimeMillis()-startTime));
        return this;
    }
    public DistStatusMap append(String... keysValues) {
        putAll(DistUtils.createMap(keysValues));
        put("totalTimeMs", (System.currentTimeMillis()-startTime));
        return this;
    }
    public DistStatusMap append(Map<String, String> initialMap) {
        putAll(initialMap);
        put("totalTimeMs", (System.currentTimeMillis()-startTime));
        return this;
    }
    public DistStatusMap appendMap(Map<String, Object> initialMap) {
        putAll(initialMap);
        put("totalTimeMs", (System.currentTimeMillis()-startTime));
        return this;
    }
    public DistStatusMap join(DistStatusMap statusMap) {
        putAll(statusMap);
        put("totalTimeMs", (System.currentTimeMillis()-startTime));
        return this;
    }
    public DistStatusMap exception(Exception ex) {
        put("status", "EXCEPTION");
        put("reason", ex.getMessage());
        put("className", ex.getClass().getName());
        put("exception", DistUtils.serializeException(ex));
        put("totalTimeMs", (System.currentTimeMillis()-startTime));
        return this;
    }
    /** */
    public DistStatusMap notImplemented() {
        return new DistStatusMap().append("status", "NOT_IMPLEMENTED");
    }

    /** create new status to be returned somewhere */
    public static DistStatusMap create(Agentable agentable) {
        return new DistStatusMap()
                .append("thread", Thread.currentThread().getName())
                .append("createdDate", LocalDateTime.now().toString())
                .append("name", agentable.getAgentableName());
    }

}
