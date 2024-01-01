package com.distsystem.api;

import com.distsystem.utils.DistUtils;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

/** single object kept in memory
 * */
public class DistMemoryObject {

    private final String objectGuid = DistUtils.generateCustomTimeGuid("OBJ");
    /** create date of this object  */
    private final LocalDateTime createDate = LocalDateTime.now();
    /** last updated date and time */
    private LocalDateTime lastUpdated = LocalDateTime.now();
    /** */
    private String value;
    /** number of updates of this object */
    private final AtomicLong updatesCount = new AtomicLong();
    /** number of updates of this object */
    private final AtomicLong useCount = new AtomicLong();

    public DistMemoryObject(String value) {
        this.value = value;
    }
    public String getObjectGuid() {
        return objectGuid;
    }
    public void setValue(String newValue) {
        lastUpdated = LocalDateTime.now();
        updatesCount.incrementAndGet();
        this.value = newValue;
    }
    /** */
    public void appendValue(String newValue) {
        lastUpdated = LocalDateTime.now();
        updatesCount.incrementAndGet();
        this.value = newValue;
    }
    public void use() {
        useCount.incrementAndGet();
    }
    public String getCreateDate() {
        return createDate.toString();
    }
    public String getLastUpdated() {
        return lastUpdated.toString();
    }
    public String getValue() {
        return value;
    }
    public long getUpdatesCount() {
        return updatesCount.get();
    }
    public long getUseCount() {
        return useCount.get();
    }
}
