package com.distsystem.encoders;

import com.distsystem.interfaces.CacheKeyEncoder;
import com.distsystem.utils.DistUtils;

/** encoding everything after 'secret:' or 'password:' key */
public class KeyEncoderStarting implements CacheKeyEncoder {
    private static final String FINGERPRINT_CONST = "0142x8k20397r8zx23047rzxmh203487gzmh08347ygzxhfio43hfxe35gxc";
    @Override
    public String encodeKey(String key) {
        int secretPos = key.indexOf("secret:");
        if (secretPos >= 0) {
            return encodeFrom(key, secretPos);
        } else {
            secretPos = key.indexOf("password:");
            if (secretPos > 0) {
                return encodeFrom(key, secretPos);
            } else {
                return key;
            }
        }
    }
    private String encodeFrom(String key, int secretPos) {
        String secretPart = key.substring(secretPos);
        return key.substring(0, secretPos) + DistUtils.fingerprint(FINGERPRINT_CONST + secretPart + FINGERPRINT_CONST);
    }

}
