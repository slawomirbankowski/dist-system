package com.distsystem.api.info;

import java.util.List;

public class AgentConfigReaderInfo {

    private long readCount;
    private long readerObjectCount;
    private long readersCount;
    private List<String> readerKeys;

    public AgentConfigReaderInfo(long readCount, long readerObjectCount, long readersCount, List<String> readerKeys) {
        this.readCount = readCount;
        this.readerObjectCount = readerObjectCount;
        this.readersCount = readersCount;
        this.readerKeys = readerKeys;
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
}
