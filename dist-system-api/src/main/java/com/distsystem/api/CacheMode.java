package com.distsystem.api;

import com.distsystem.utils.AdvancedMap;
import com.distsystem.utils.DistUtils;

import java.util.HashMap;

/** cache modes for objects to be kept in cache
 * mode contains how object would be cleared: time based, priority based, always keep, refresh
 * timeToLive is defining milliseconds this object is valid for OR time to refresh this object
 * priority is a number from 0 to 9 to decide in which order objects should be removed if there are too many items in cache
 * */
public class CacheMode {
    public enum Mode {
        /** time-to-live - object is removed from cache after given milliseconds */
        TTL,
        /** object is kept in cache till cleared by clear method */
        KEEP,
        /** like TTL, but timer is reset on every use */
        TTL_RENEW,
        /** object is automatically refreshed on a fixed rate, using the function given on insertion time */
        REFRESH,
        /** when cache size limit is reached and a new object is inserted, another object
         * with the lowest priority will be removed */
        PRIORITY;

        /** if this mode is TTL based - so it is Time To Live */
        public boolean isTtl() {
            return this.equals(TTL);
        }
        public boolean isRefresh() {
            return this.equals(REFRESH);
        }
        public static Mode parseModeOfDefault(String value) {
            try {
                return Mode.valueOf(value);
            } catch (IllegalArgumentException ex) {
                // unknown mode - set default TTL
                return TTL;
            }


        }
    }

    /** mode of keeping item in cache */
    private final Mode mode;
    /** time to live milliseconds */
    private final long timeToLiveMs;
    /** priority in cache */
    private final int priority;
    /** */
    private final boolean addToInternal;
    /** */
    private final boolean addToExternal;

    public CacheMode(Mode m, long timeToLiveMs, boolean addToInternal, boolean addToExternal, int priority) {
        this.mode = m;
        this.timeToLiveMs = timeToLiveMs;
        this.addToInternal = addToInternal;
        this.addToExternal = addToExternal;
        this.priority = priority;
    }
    public CacheMode(Mode m, long timeToLiveMs, boolean addToInternal, boolean addToExternal) {
        this.mode = m;
        this.timeToLiveMs = timeToLiveMs;
        this.addToInternal = addToInternal;
        this.addToExternal = addToExternal;
        this.priority = CachePriority.PRIORITY_MEDIUM;
    }
    public CacheMode(Mode m, long timeToLiveMs) {
        this.mode = m;
        this.timeToLiveMs = timeToLiveMs;
        this.addToInternal = true;
        this.addToExternal = false;
        this.priority = CachePriority.PRIORITY_MEDIUM;
    }
    public CacheMode(Mode cacheMode) {
        this(cacheMode, TIME_FOREVER, true, false);
    }

    /** get time to live in milliseconds */
    public long getTimeToLiveMs() { return timeToLiveMs; }

    public Mode getMode() { return this.mode; }
    /** get priority for this mode
     * priorities are defined in CachePriority class
     * default priority is PRIORITY_MEDIUM
     * */
    /** get priority of object with this mode assigned, priority is integer number from 1 (LOWEST) to 9 (HIGHEST)*/
    public int getPriority() { return priority; }

    public boolean isTtl() { return mode == Mode.TTL; }
    public boolean isRefresh() { return mode == Mode.REFRESH; }
    public boolean isKeep() { return mode == Mode.KEEP; }
    public boolean isAddToInternal() {
        return addToInternal;
    }
    public boolean isAddToExternal() {
        return addToExternal;
    }

    /** based time-to-live model where object is removed from cache after given milliseconds */
    public static int MODE_TTL = 1;
    /** mode when object is kept in cache till cleared by clear method */
    public static int MODE_KEEP = 2;
    /** mode when object is kept till object is still used for last X milliseconds, when object is
     * not used for much time */
    public static int MODE_USED = 3;
    /** refresh mode when item could be refreshed every N seconds through refreshing handler */
    public static int MODE_REFRESH = 4;
    /** object in cache is kept until there is too many objects in cache,
     * after that objects with the lowest priority would be removed  */
    public static int MODE_PRIORITY = 5;

    public static long TIME_ONE_SECOND = 1 * 1000L;
    public static long TIME_TWO_SECONDS = 2 * 1000L;
    public static long TIME_THREE_SECONDS = 3 * 1000L;
    public static long TIME_FIVE_SECONDS = 5 * 1000L;
    public static long TIME_TEN_SECONDS = 10 * 1000L;
    public static long TIME_TWENTY_SECONDS = 20 * 1000L;
    public static long TIME_THIRTY_SECONDS = 30 * 1000L;
    public static long TIME_ONE_MINUTE = 60 * 1000L;
    public static long TIME_FIVE_MINUTES = 5 * 60 * 1000L;
    public static long TIME_TEN_MINUTES = 10 * 60 * 1000L;
    public static long TIME_TWENTY_MINUTES = 20 * 60 * 1000L;
    public static long TIME_THIRTY_MINUTES = 30 * 60 * 1000L;
    public static long TIME_ONE_HOUR = 3600 * 1000L;
    public static long TIME_TWO_HOURS = 2 * 3600 * 1000L;
    public static long TIME_THREE_HOURS = 3 * 3600 * 1000L;
    public static long TIME_SIX_HOURS = 6 * 3600 * 1000L;
    public static long TIME_ONE_DAY = 24 * 3600 * 1000L;
    public static long TIME_ONE_WEEK = 7 * 24 * 3600 * 1000L;
    public static long TIME_TWO_WEEKS = 14 * 24 * 3600 * 1000L;
    public static long TIME_FOUR_WEEKS = 28 * 24 * 3600 * 1000L;
    public static long TIME_ONE_YEAR = 365 * 24 * 3600 * 1000L;
    public static long TIME_FOREVER = Long.MAX_VALUE;

    public static CacheMode modeTtlTenSeconds = new CacheMode(Mode.TTL, TIME_TEN_SECONDS);
    public static CacheMode modeTtlTwentySeconds = new CacheMode(Mode.TTL, TIME_TWENTY_SECONDS);
    public static CacheMode modeTtlThirtySeconds = new CacheMode(Mode.TTL, TIME_THIRTY_SECONDS);
    public static CacheMode modeTtlOneMinute = new CacheMode(Mode.TTL, TIME_ONE_MINUTE);
    public static CacheMode modeTtlFiveMinutes = new CacheMode(Mode.TTL, TIME_FIVE_MINUTES);
    public static CacheMode modeTtlTenMinutes = new CacheMode(Mode.TTL, TIME_TEN_MINUTES);
    public static CacheMode modeTtlTwentyMinutes = new CacheMode(Mode.TTL, TIME_TWENTY_MINUTES);
    public static CacheMode modeTtlThirtyMinutes = new CacheMode(Mode.TTL, TIME_THIRTY_MINUTES);
    public static CacheMode modeTtlOneHour = new CacheMode(Mode.TTL, TIME_ONE_HOUR);
    public static CacheMode modeTtlTwoHours = new CacheMode(Mode.TTL, TIME_TWO_HOURS);
    public static CacheMode modeTtlThreeHours = new CacheMode(Mode.TTL, TIME_THREE_HOURS);
    public static CacheMode modeTtlSixHours = new CacheMode(Mode.TTL, TIME_SIX_HOURS);
    public static CacheMode modeTtlOneDay = new CacheMode(Mode.TTL, TIME_ONE_DAY);
    public static CacheMode modeTtlOneWeek = new CacheMode(Mode.TTL, TIME_ONE_WEEK);
    public static CacheMode modeTtlTwoWeeks = new CacheMode(Mode.TTL, TIME_TWO_WEEKS);
    public static CacheMode modeTtlFourWeeks = new CacheMode(Mode.TTL, TIME_FOUR_WEEKS);

    public static CacheMode modeKeep = new CacheMode(Mode.KEEP, TIME_FOREVER);
    public static CacheMode modeInternalOnly = new CacheMode(Mode.KEEP, TIME_FOREVER);

    public static CacheMode modeRefreshOneSecond = new CacheMode(Mode.REFRESH, TIME_ONE_SECOND);
    public static CacheMode modeRefreshTwoSeconds = new CacheMode(Mode.REFRESH, TIME_TWO_SECONDS);
    public static CacheMode modeRefreshTenSeconds = new CacheMode(Mode.REFRESH, TIME_TEN_SECONDS);
    public static CacheMode modeRefreshOneMinute = new CacheMode(Mode.REFRESH, TIME_ONE_MINUTE);
    public static CacheMode modeRefreshOneHour = new CacheMode(Mode.REFRESH, TIME_ONE_HOUR);
    public static CacheMode modeRefreshSixHours = new CacheMode(Mode.REFRESH, TIME_SIX_HOURS);

    public static CacheMode modePriorityVeryLow = new CacheMode(Mode.PRIORITY, TIME_FOREVER, true, false, CachePriority.PRIORITY_VERY_LOW);
    public static CacheMode modePriorityLow = new CacheMode(Mode.PRIORITY, TIME_FOREVER, true, false, CachePriority.PRIORITY_LOW);
    public static CacheMode modePriorityMedium = new CacheMode(Mode.PRIORITY, TIME_FOREVER, true, false, CachePriority.PRIORITY_MEDIUM);
    public static CacheMode modePriorityHigh = new CacheMode(Mode.PRIORITY, TIME_FOREVER, true, false, CachePriority.PRIORITY_HIGH);
    public static CacheMode modePriorityVeryHigh = new CacheMode(Mode.PRIORITY, TIME_FOREVER, true, false, CachePriority.PRIORITY_VERY_HIGH);

    public static CacheMode modePriority(int priority) {
        return new CacheMode(Mode.PRIORITY, TIME_FOREVER, true, false, priority);
    }

    /** already parsed modes for simplicity and velocity of parsing */
    private static HashMap<String, CacheMode> parsedModes = new HashMap<>();
    public static CacheMode createModeFromString(String modeStr) {
        // parse mode from string, mode could be something like 'mode=1,priority=3,ttl=5000,internal=false,external=true
        AdvancedMap map = DistUtils.splitToAdvancedMapBySeparationEqual(modeStr, ",", '=', true);
        Mode m = Mode.parseModeOfDefault(map.getString("mode", "TTL"));
        long timeToLiveMs = map.getLong("ttl", 60000);
        boolean addToInternal = map.getBoolean("internal", true);
        boolean addToExternal = map.getBoolean("external", true);
        int priority = map.getInt("priority", 5);
        return new CacheMode(m, timeToLiveMs, addToInternal, addToExternal, priority);
    }
    /** parse cache mode from String */
    public static CacheMode fromString(String modeStr) {
        return parsedModes.computeIfAbsent(modeStr, key -> createModeFromString(key));
    }
}
