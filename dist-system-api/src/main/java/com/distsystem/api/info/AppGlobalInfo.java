package com.distsystem.api.info;

import java.time.LocalDateTime;

/** global information about dist-cache application */
public class AppGlobalInfo {
    private final LocalDateTime localDateTime = LocalDateTime.now();
    private final LocalDateTime createdDate;
    private final String currentHostName;
    private final String currentHostAddress;
    private final String currentLocationPath;
    private final int activeThreadsCount;
    private final long freeMemory;
    private final long maxMemory;
    private final long totalMemory;
    private final long freeMemoryMb;
    private final long maxMemoryMb;
    private final long totalMemoryMb;

    public AppGlobalInfo(LocalDateTime createdDate, String currentHostName, String currentHostAddress, String currentLocationPath, int activeThreadsCount, long freeMemory, long maxMemory, long totalMemory, long freeMemoryMb, long maxMemoryMb, long totalMemoryMb) {
        this.createdDate = createdDate;
        this.currentHostName = currentHostName;
        this.currentHostAddress = currentHostAddress;
        this.currentLocationPath = currentLocationPath;
        this.activeThreadsCount = activeThreadsCount;
        this.freeMemory = freeMemory;
        this.maxMemory = maxMemory;
        this.totalMemory = totalMemory;
        this.freeMemoryMb = freeMemoryMb;
        this.maxMemoryMb = maxMemoryMb;
        this.totalMemoryMb = totalMemoryMb;
    }

    public String getCreatedDate() {
        return createdDate.toString();
    }

    public String getLocalDateTime() {
        return localDateTime.toString();
    }

    public String getCurrentHostName() {
        return currentHostName;
    }

    public int getActiveThreadsCount() {
        return activeThreadsCount;
    }

    public long getFreeMemory() {
        return freeMemory;
    }

    public long getMaxMemory() {
        return maxMemory;
    }

    public long getTotalMemory() {
        return totalMemory;
    }

    public long getFreeMemoryMb() {
        return freeMemoryMb;
    }

    public long getMaxMemoryMb() {
        return maxMemoryMb;
    }

    public long getTotalMemoryMb() {
        return totalMemoryMb;
    }

    public String getCurrentHostAddress() {
        return currentHostAddress;
    }

    public String getCurrentLocationPath() {
        return currentLocationPath;
    }
}
