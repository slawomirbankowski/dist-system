package com.distsystem.api.info;

import java.util.List;
import java.util.Map;

public class AgentConfigReaderInfo {

    private final long readCount;
    private final long readerObjectCount;
    private final long readersCount;
    private final List<String> readerKeys;
    private final Map<String, String> configValues;

    public AgentConfigReaderInfo(long readCount, long readerObjectCount, long readersCount,
                                 List<String> readerKeys, Map<String, String> configValues) {
        this.readCount = readCount;
        this.readerObjectCount = readerObjectCount;
        this.readersCount = readersCount;
        this.readerKeys = readerKeys;
        this.configValues = configValues;
    }
    public long getReadCount() {
        return readCount;
    }
    public long getReaderObjectCount() {
        return readerObjectCount;
    }
    public long getReadersCount() {
        return readersCount;
    }
    public List<String> getReaderKeys() {
        return readerKeys;
    }
    public Map<String, String> getConfigValues() {
        return configValues;
    }
}
