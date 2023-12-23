package com.distsystem.serializers;

import com.distsystem.interfaces.DistSerializer;
import com.distsystem.utils.DistSerializerBase;

import java.io.*;
import java.util.Base64;

/** serialize of classes that implements Serializable interface, this is using ObjectOutputStream */
public class ObjectStreamSerializer extends DistSerializerBase implements DistSerializer {

    @Override
    public byte[] serialize(Object obj) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(2000);
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.close();
            return baos.toByteArray();
        } catch (Exception ex) {
            return new byte[0];
        }
    }

    @Override
    public Object deserialize(String objectClassName, byte[] b) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(b);
            ObjectInputStream ois = new ObjectInputStream(bais);
            Object obj = ois.readObject();
            ois.close();
            return obj;
        } catch (Exception ex) {
            return new byte[0];
        }
    }

    @Override
    public String serializeToString(Object obj) {
        return new String(Base64.getEncoder().encode(serialize(obj)));
    }
    @Override
    public Object deserializeFromString(String objectClassName, String str) {
        return deserialize(objectClassName, Base64.getDecoder().decode(str));
    }
}
