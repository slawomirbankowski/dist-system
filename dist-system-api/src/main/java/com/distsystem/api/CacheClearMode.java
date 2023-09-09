package com.distsystem.api;

/** Mode of clearning elements in Cache */
public enum CacheClearMode {
    NO_CLEAR,
    ALL_ELEMENTS,
    BY_PRIORITY;

    public static CacheClearMode parseClearMode(String name) {
        try {
            return CacheClearMode.valueOf(name);
        } catch (IllegalArgumentException ex) {
            return CacheClearMode.NO_CLEAR;
        }
    }

}
