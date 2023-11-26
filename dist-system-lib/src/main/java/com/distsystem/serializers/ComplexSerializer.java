package com.distsystem.serializers;

import com.distsystem.interfaces.DistSerializer;
import com.distsystem.utils.DistSerializerBase;
import com.distsystem.utils.DistUtils;

import java.util.*;
import java.util.stream.Collectors;

/** complex serializer that is keeping other serializers mapped to class name */
public class ComplexSerializer extends DistSerializerBase implements DistSerializer {

    /** all serializers per class */
    private HashMap<String, DistSerializer> serializerHashMap = new HashMap<>();
    /** */
    private DistSerializer defaultSerializer;
    /** create new complex serializer */
    public ComplexSerializer(Map<String, DistSerializer> serializers) {
        serializerHashMap.putAll(serializers);
        defaultSerializer = serializerHashMap.getOrDefault("*", new ObjectStreamSerializer());
    }

    public ComplexSerializer() {
        defaultSerializer = new ObjectStreamSerializer();
    }

    /** count objects  */
    public long countObjects() {
        return serializerHashMap.size()*3L;
    }
    /** */
    public Set<String> getSerializerKeys() {
        return serializerHashMap.keySet().stream().collect(Collectors.toSet());
    }
    public Set<String> getSerializerClasses() {
        return serializerHashMap.values().stream().map(s -> s.getClass().getName()).collect(Collectors.toSet());
    }

    public void initializeSerializers(String serializerDef) {

        // TODO: initialize serializers for classes and default ones
        serializerHashMap.put("java.lang.String", new StringSerializer());
        serializerHashMap.put("", new StringSerializer());
    }
    /** */
    public DistSerializer selectSerializer(String objectClassName) {
        return defaultSerializer;
    }
    @Override
    public byte[] serialize(Object obj) {
        return selectSerializer(obj.getClass().getName()).serialize(obj);
    }

    @Override
    public Object deserialize(String objectClassName, byte[] b) {
        return selectSerializer(objectClassName).deserialize(objectClassName, b);
    }

    @Override
    public String serializeToString(Object obj) {
        return selectSerializer(obj.getClass().getName()).serializeToString(obj);
    }

    @Override
    public Object deserializeFromString(String objectClassName, String str) {
        return selectSerializer(objectClassName).deserializeFromString(objectClassName, str);
    }

    public static ComplexSerializer createSerializer(HashMap<String, DistSerializer> sers) {
        return new ComplexSerializer(sers);
    }
    /** create complex serializer from String definition - it is parsing definition from:
     * class=SerializerClass,class2=SerializerClass2 */
    public static ComplexSerializer createComplexSerializer(String serializersDef) {
        var serializers = parseSerializers(serializersDef);
        return new ComplexSerializer(serializers);
    }
    /** */
    public static HashMap<String, DistSerializer> parseSerializers(String serializersDef) {
        HashMap<String, DistSerializer> serializers = new HashMap<>();
        var definitions = DistUtils.splitBySeparationEqual(serializersDef, ",", '=', true);
        definitions.stream().forEach(d -> {
            Optional<DistSerializer> ser = createSerializer("com.distsystem.serializers." + d[1]);
            if (ser.isPresent()) {
                serializers.put(d[0], ser.get());
            } else {
                ser = createSerializer(d[1]);
                if (ser.isPresent()) {
                    serializers.put(d[0], ser.get());
                }
            }
        });
        return serializers;
    }
    /** create serializer for given class - it must has constructor without parameters!!! */
    public static Optional<DistSerializer> createSerializer(String serializerClass) {
        try {
            DistSerializer ser = (DistSerializer)Class.forName(serializerClass)
                    .getConstructor()
                    .newInstance();
            return Optional.of(ser);
        } catch (Exception ex) {
            // cannot create such serializer from class name
            return Optional.empty();
        }
    }
}
