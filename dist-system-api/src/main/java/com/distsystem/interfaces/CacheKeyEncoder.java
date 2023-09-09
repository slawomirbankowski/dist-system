package com.distsystem.interfaces;

/** interface to encode key
 * encoding key is when key could be very long or there might be secrets, passwords, private keys as part of key */
public interface CacheKeyEncoder {
    /** encode key to not show passwords and secrets */
    String encodeKey(String key);
}
