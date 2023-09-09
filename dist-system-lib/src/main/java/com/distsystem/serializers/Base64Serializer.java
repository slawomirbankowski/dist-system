package com.distsystem.serializers;

import com.distsystem.interfaces.DistSerializer;

import java.util.Base64;

/** serialize using JSON */
public class Base64Serializer implements DistSerializer {

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
    @Override
    public String serializeToString(Object obj) {
        try {
            return new String(Base64.getEncoder().encode(obj.toString().getBytes()));
        } catch (Exception ex) {
            return null;
        }
    }
    @Override
    public Object deserializeFromString(String objectClassName, String str) {
        try {
            return new String(Base64.getDecoder().decode(str));
        } catch (Exception ex) {
            return null;
        }
    }
}
