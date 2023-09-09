package com.distsystem.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** JSON utils */
public class JsonUtils {

    /** local logger for this class*/
    protected static final Logger log = LoggerFactory.getLogger(JsonUtils.class);
    /** serialize */
    public static String serialize(Object obj) {
        try {
            ObjectMapper mapper = JsonMapper.builder()
                    .findAndAddModules()
                    .build();
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException ex) {
            log.warn("CANNOT SERIALIZE OBJECT: " + obj.getClass().getName() + ", reason: " + ex.getMessage(), ex);
            return null;
        }
    }
    /** deserialize JSON to given Object of type */
    public static <T> T deserialize(String json, Class<T> type) {
        try {
            ObjectMapper mapper = JsonMapper.builder()
                    .findAndAddModules()
                    .build();
            return mapper.readValue(json, type);
        } catch (JsonProcessingException ex) {
            log.warn("CANNOT DESERIALIZE TYPE: " + type.getName() + ", reason: " + ex.getMessage(), ex);
            return null;
        }
    }
    public static <T> T deserialize(String json, TypeReference<T> type) {
        try {
            ObjectMapper mapper = JsonMapper.builder()
                    .findAndAddModules()
                    .build();
            return mapper.readValue(json, type);
        } catch (JsonProcessingException ex) {
            log.warn("CANNOT DESERIALIZE TYPE: " + type.getType().getTypeName() + ", reason: " + ex.getMessage(), ex);
            return null;
        }
    }
}