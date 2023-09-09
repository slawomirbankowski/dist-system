package com.distsystem.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** advanced read-only map with special GET features
 * it is for easier get of long, double, int, date, boolean values from properties map */
public class AdvancedMap {

    /** map to advanced functions */
    private final Map<String, Object> map;
    public AdvancedMap(Map<String, Object> map) {
        this(map, false);
    }
    public AdvancedMap(Map<String, Object> map, boolean toLowercase) {
        if (toLowercase) {
            this.map = new HashMap<>();
            map.entrySet().stream().forEach(e -> {
                this.map.put(e.getKey().toLowerCase(), e.getValue());
            });
        } else {
            this.map = map;
        }
    }
    public String getString(String key, String defaultValue) {
        return map.getOrDefault(key, defaultValue).toString();
    }
    public String getStringOrEmpty(String key) {
        return getString(key, "");
    }
    public long getLong(String key, long defaultValue) {
        return DistUtils.parseLong(map.getOrDefault(key, defaultValue).toString(), defaultValue);
    }
    public long getLongOrZero(String key) {
        return getLong(key, 0L);
    }
    public int getInt(String key, int defaultValue) {
        return DistUtils.parseInt(""+map.getOrDefault(key, defaultValue), defaultValue);
    }
    public int getIntOrZero(String key) {
        return getInt(key, 0);
    }
    public double getDouble(String key, double defaultValue) {
        return DistUtils.parseDouble(map.getOrDefault(key, defaultValue).toString(), defaultValue);
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

    public static AdvancedMap fromMap(Map<String, Object> map) {
        return new AdvancedMap(map);
    }
}
