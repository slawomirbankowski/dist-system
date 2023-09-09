package com.distsystem.serializers;

import com.distsystem.interfaces.DistSerializer;

/** serialize using JSON */
public abstract class ArrayBaseSerializer implements DistSerializer {

    @Override
    public byte[] serialize(Object obj) {
        try {
            return serializeToString(obj).getBytes();
        } catch (Exception ex) {
            return null;
        }
    }
    @Override
    public Object deserialize(String objectClassName, byte[] b) {
        try {
            return deserializeFromString(objectClassName, new String(b));
        } catch (Exception ex) {
            return null;
        }
    }
}
