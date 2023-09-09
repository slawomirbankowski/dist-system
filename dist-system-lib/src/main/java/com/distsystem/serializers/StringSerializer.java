package com.distsystem.serializers;

import com.distsystem.interfaces.DistSerializer;

/** serializer and deserializer using String - assuming everything is String */
public class StringSerializer implements DistSerializer {

    @Override
    public byte[] serialize(Object obj) {
        try {
            return obj.toString().getBytes();
        } catch (Exception ex) {
            return null;
        }
    }
    @Override
    public Object deserialize(String objectClassName, byte[] b) {
        return new String(b);
    }
    @Override
    public String serializeToString(Object obj) {
        return obj.toString();
    }

    @Override
    public Object deserializeFromString(String objectClassName, String str) {
        return str;
    }

}
