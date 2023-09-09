package com.distsystem.serializers;

import com.distsystem.interfaces.DistSerializer;

import java.util.Base64;

/** serialize using JSON */
public abstract class BAse64BaseSerializer implements DistSerializer {

    @Override
    public byte[] serialize(Object obj) {
        return Base64.getEncoder().encode(serializeToString(obj).getBytes());
    }

    @Override
    public Object deserialize(String objectClassName, byte[] b) {
        return deserializeFromString(objectClassName, new String(Base64.getDecoder().decode(b)));
    }

}
