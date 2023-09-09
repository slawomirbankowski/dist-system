package com.distsystem.serializers;

import com.distsystem.interfaces.DistSerializer;

/** serializer and deserializer using reflection - get all fields and get/set values as simple Strings */
public class ReflectionSimpleSerializer implements DistSerializer {

    @Override
    public byte[] serialize(Object obj) {
        try {

            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public Object deserialize(String objectClassName, byte[] b) {


        return null;
    }

    @Override
    public String serializeToString(Object obj) {
        return null;
    }

    @Override
    public Object deserializeFromString(String objectClassName, String str) {
        return null;
    }

}
