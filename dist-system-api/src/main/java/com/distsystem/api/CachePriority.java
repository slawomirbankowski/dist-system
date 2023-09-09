package com.distsystem.api;

/** list of priorities for cache object from VERY_LOW=1 to VERY_HIGH=9
 * Priority can be used to remove cache objects in given order from the lowest priority to the highest priority
 * It helps manage number of cache objects in memory or other cache storage and clean the least important ones
 * */
public class CachePriority {

    /** the lowest priority - object would be removed ASAP */
    public static int PRIORITY_VERY_LOW = 1;
    /**  */
    public static int PRIORITY_LOW = 3;
    /** medium priority - object would be removed somewhere in the middle before high priority objects and after low priority objects */
    public static int PRIORITY_MEDIUM = 5;
    /**  */
    public static int PRIORITY_HIGH = 7;
    /** the highest priority - the object would be removed as the last */
    public static int PRIORITY_VERY_HIGH = 9;

}
