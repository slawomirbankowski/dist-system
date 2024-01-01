package com.distsystem.utils;

import com.distsystem.interfaces.Agentable;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/** advanced read-only map with special GET features
 * it is for easier get of long, double, int, date, boolean values from properties map */
public class AdvancedMap extends HashMap<String, Object> {

    /** status created time */
    private final long startTime = System.currentTimeMillis();

    public AdvancedMap() {
    }
    public AdvancedMap(Map<String, Object> map) {
        this(map, false);
    }
    public AdvancedMap(Map<String, Object> map, boolean toLowercase) {
        if (toLowercase) {

            map.entrySet().stream().forEach(e -> {
                put(e.getKey().toLowerCase(), e.getValue());
            });
        } else {
            putAll(map);
        }
    }
    public String getString(String key, String defaultValue) {
        return getOrDefault(key, defaultValue).toString();
    }
    public String getStringOrEmpty(String key) {
        return getString(key, "");
    }

    public String getStringOrNull(String key) {
        Object v = get(key);
        if (v!=null) {
            return v.toString();
        } else {
            return null;
        }
    }
    public long getLong(String key, long defaultValue) {
        return DistUtils.parseLong(getOrDefault(key, defaultValue).toString(), defaultValue);
    }
    public long getLongOrZero(String key) {
        return getLong(key, 0L);
    }
    public int getInt(String key, int defaultValue) {
        return DistUtils.parseInt(""+getOrDefault(key, defaultValue), defaultValue);
    }
    public int getIntOrZero(String key) {
        return getInt(key, 0);
    }
    public double getDouble(String key, double defaultValue) {
        return DistUtils.parseDouble(getOrDefault(key, defaultValue).toString(), defaultValue);
    }
    public double getDoubleOrZero(String key, double defaultValue) {
        return getDouble(key, 0.0);
    }
    public boolean getBoolean(String key, boolean defaultValue) {
        return defaultValue;
    }
    public java.util.Date getDate(String key, java.util.Date defaultValue) {
        return defaultValue;
    }

    public LocalDateTime getLocalDateOrNull(String key) {
        try {
            Object v = get(key);
            if (v==null) {
                return null;
            } else if (LocalDateTime.class.isInstance(v)) {
                return (LocalDateTime)v;
            } else if (Date.class.isInstance(v)) {
                return DistUtils.dateToLocalDateTime((Date)v);
            } else {
                return LocalDateTime.parse(v.toString());
            }
        } catch (DateTimeParseException ex) {
            return null;
        }
    }
    public Map<String, String> getMap() {
        // TODO: implement
        return Map.of();
    }
    public LocalDateTime getLocalDateOrNow(String key) {
        try {
            return LocalDateTime.parse(getString(key, LocalDateTime.now().toString()));
        } catch (DateTimeParseException ex) {
            return LocalDateTime.now();
        }
    }
    /** */
    public Set<String> getWithSplit(String key, String splitChar) {
        return Arrays.stream(getString(key, "").split(splitChar)).collect(Collectors.toSet());
    }

    /** add final status */
    public AdvancedMap withStatus(String status) {
        put("status", status);
        calculateTotalTime();
        return this;
    }
    /** add key with value */
    public AdvancedMap append(String key, String value) {
        put(key, value);
        calculateTotalTime();
        return this;
    }
    /** add many key-values */
    public AdvancedMap append(String... keysValues) {
        putAll(DistUtils.createMap(keysValues));
        calculateTotalTime();
        return this;
    }
    public AdvancedMap append(Map<String, String> initialMap) {
        putAll(initialMap);
        calculateTotalTime();
        return this;
    }
    /** */
    public AdvancedMap appendMap(Map<String, Object> initialMap) {
        putAll(initialMap);
        calculateTotalTime();
        return this;
    }
    /** join all parameters from another status map */
    public AdvancedMap join(AdvancedMap statusMap) {
        putAll(statusMap);
        calculateTotalTime();
        return this;
    }
    /** add exception parameters to this map */
    public AdvancedMap exception(Exception ex) {
        put("status", "EXCEPTION");
        put("reason", ex.getMessage());
        put("className", ex.getClass().getName());
        put("exception", DistUtils.serializeException(ex));
        calculateTotalTime();
        return this;
    }
    private void calculateTotalTime() {
        put("totalTimeMs", (System.currentTimeMillis()-startTime));
    }
    /** */
    public AdvancedMap notImplemented() {
        return new AdvancedMap().append("status", "NOT_IMPLEMENTED");
    }
    public static AdvancedMap fromMap(Map<String, Object> map) {
        return new AdvancedMap(map);
    }
    /** */
    public static AdvancedMap fromStringMap(Map<String, String> map) {
        AdvancedMap m = new AdvancedMap();
        m.append(map);
        return m;
    }
    /** create new status to be returned somewhere */
    public static AdvancedMap create(Agentable agentable) {
        return new AdvancedMap()
                .append("thread", Thread.currentThread().getName())
                .append("createdDate", LocalDateTime.now().toString())
                .append("name", agentable.getAgentableName());
    }
    /** */
    public static AdvancedMap createFromObject(Object obj) {
        return new AdvancedMap()
                .append("objectClass", obj.getClass().getName());
    }

}
