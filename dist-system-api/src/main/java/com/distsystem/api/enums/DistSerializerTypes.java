package com.distsystem.api.enums;

/** all known serializer types to serialize/ deserialize Object to byte[] or String */
public enum DistSerializerTypes {
    ArrayBaseSerializer,
    BAse64BaseSerializer,
    Base64Serializer,
    ComplexSerializer,
    JsonSerializer,
    ObjectStreamCompressedSerializer,
    ObjectStreamSerializer,
    ReflectionSimpleSerializer,
    StringSerializer;
}
