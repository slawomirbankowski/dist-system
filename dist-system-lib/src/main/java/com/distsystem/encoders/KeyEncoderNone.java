package com.distsystem.encoders;

import com.distsystem.interfaces.CacheKeyEncoder;

/** no encoding */
public class KeyEncoderNone implements CacheKeyEncoder {
    @Override
    public String encodeKey(String key) {
        return key;
    }
}
