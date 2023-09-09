package com.distsystem.api.info;

import java.time.LocalDateTime;

/** global information about dist-cache application */
public class AppGlobalInfo {
    private LocalDateTime localDateTime = LocalDateTime.now();
    private LocalDateTime createdDate;
    private String currentHostName;
    private String currentHostAddress;
    private String currentLocationPath;
    private int activeThreadsCount;
    public long freeMemory;
    public long maxMemory;
    public long totalMemory;
    public long freeMemoryMb;
    public long maxMemoryMb;
    public long totalMemoryMb;

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
