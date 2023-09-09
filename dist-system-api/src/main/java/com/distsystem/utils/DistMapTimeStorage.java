package com.distsystem.utils;

import java.time.LocalDateTime;

/** storage to keep objects by UID and by validity time, after that time item should be removed to not take space in memory */
public class DistMapTimeStorage<T> {

    /** messages already sent */
    private final java.util.concurrent.ConcurrentHashMap<String, T> itemsByUid = new java.util.concurrent.ConcurrentHashMap<>();
    /** messages by time bucket and map of messages - this is to efficiently remove all old messages */
    private final HashMapMap<String, String, T> itemsByTimeUid = new HashMapMap<>();

    /** */
    public void addItem(String uid, T item, LocalDateTime itemTime) {
        itemsByUid.put(uid, item);
    }

    /** get total number of items in storage */
    public int getItemsCount() { return itemsByUid.size(); }

    public T getByUid(String uid) {
        return itemsByUid.get(uid);
    }
    /** remove item by UID */
    public void removeByUid(String uid, LocalDateTime itemTime) {
        itemsByUid.remove(uid);

    }
    public void removeByItem(String uid) {
        itemsByUid.remove(uid);

    }

}
