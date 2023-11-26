package com.distsystem.utils;

import com.distsystem.interfaces.DistSerializer;

import java.util.Base64;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/** custom serializer to serialize and deserialize objects to be sent through network or write/read from external storages */
public class CustomSerializer extends DistSerializerBase implements DistSerializer {

    private Function<Object, String> serializeFunction;
    private BiFunction<String, String, Object> deserializeFunction;
    /** */
    public CustomSerializer(Function<Object, String> serializeFunction, BiFunction<String, String, Object> deserializeFunction) {
        this.serializeFunction = serializeFunction;
        this.deserializeFunction = deserializeFunction;
    }

    @Override
    public byte[] serialize(Object obj) {
        return Base64.getEncoder().encode(serializeToString(obj).getBytes());
    }

    @Override
    public Object deserialize(String objectClassName, byte[] b) {
        return deserializeFromString(objectClassName, new String(Base64.getDecoder().decode(b)));
    }

    @Override
    public String serializeToString(Object obj) {
        return serializeFunction.apply(obj);
    }

    @Override
    public Object deserializeFromString(String objectClassName, String str) {
        return deserializeFunction.apply(objectClassName, str);
    }

}
