package com.distsystem.serializers;

import com.distsystem.interfaces.DistSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Base64;

/** */
public class ObjectStreamCompressedSerializer implements DistSerializer {

    private static final Logger log = LoggerFactory.getLogger(ObjectStreamCompressedSerializer.class);

    @Override
    public byte[] serialize(Object obj) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.close();
            var b = baos.toByteArray();
            var defl = new java.util.zip.Deflater();
            defl.setInput(b);
            defl.finish();
            byte[] buff = new byte[b.length];
            int compressed = defl.deflate(buff);
            byte[] outBytes = Arrays.copyOf(buff, compressed);
            return outBytes;
        } catch (Exception ex) {
            log.info("ObjectStreamCompressedSerializer:serialize Exception, reason: " + ex.getMessage());
            return new byte[0];
        }
    }

    @Override
    public Object deserialize(String objectClassName, byte[] b) {
        try {
            var infl = new java.util.zip.Inflater();
            infl.setInput(b);
            byte[] buff = new byte[b.length*3];
            int uncompressed = infl.inflate(buff);
            byte[] outBytes = Arrays.copyOf(buff, uncompressed);
            ByteArrayInputStream bais = new ByteArrayInputStream(outBytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            Object obj = ois.readObject();
            ois.close();
            return obj;
        } catch (Exception ex) {
            log.info("ObjectStreamCompressedSerializer:deserialize Exception, reason: " + ex.getMessage());
            ex.printStackTrace();
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
