package com.distsystem.api;

import com.distsystem.utils.CacheStats;

/** base class for policy check */
public abstract class CachePolicyCheck {
    private String _value;
    public CachePolicyCheck(String value) {
        this._value = value;
    }
    public abstract boolean check(CacheObject co, CacheStats stats);
    public String toString() {
        return getClass().getSimpleName() + "=" + _value;
    }
}
class CachePolicyCheckEmpty extends CachePolicyCheck {

    public CachePolicyCheckEmpty(String value) {
        super(value);
    }
    public boolean check(CacheObject co, CacheStats stats) {
        return true;
    }
}
class CachePolicyCheckSizeMin extends CachePolicyCheck {
    private long minValue;
    public CachePolicyCheckSizeMin(String value) {
        super(value);
        this.minValue = Long.parseLong(value);
    }
    public boolean check(CacheObject co, CacheStats stats) {
        return co.getSize() >= minValue;
    }
}
class CachePolicyCheckSizeMax extends CachePolicyCheck {
    private long maxValue;
    public CachePolicyCheckSizeMax(String value) {
        super(value);
        this.maxValue = Long.parseLong(value);
    }
    public boolean check(CacheObject co, CacheStats stats) {
        return co.getSize() <= maxValue;
    }
}
class CachePolicyCheckTtlMin extends CachePolicyCheck {
    private long value;
    public CachePolicyCheckTtlMin(String value) {
        super(value);
        this.value = Long.parseLong(value);
    }
    public boolean check(CacheObject co, CacheStats stats) {
        return co.getTimeToLive() >= value;
    }
}
class CachePolicyCheckTtlMax extends CachePolicyCheck {
    private long value;
    public CachePolicyCheckTtlMax(String value) {
        super(value);
        this.value = Long.parseLong(value);
    }
    public boolean check(CacheObject co, CacheStats stats) {
        return co.getTimeToLive() <= value;
    }
}
class CachePolicyCheckPriorityMin extends CachePolicyCheck {
    private long value;
    public CachePolicyCheckPriorityMin(String value) {
        super(value);
        this.value = Long.parseLong(value);
    }
    public boolean check(CacheObject co, CacheStats stats) {
        return co.getPriority() <= value;
    }
}
class CachePolicyCheckPriorityMax extends CachePolicyCheck {
    private long value;
    public CachePolicyCheckPriorityMax(String value) {
        super(value);
        this.value = Long.parseLong(value);
    }
    public boolean check(CacheObject co, CacheStats stats) {
        return co.getPriority() <= value;
    }
}
class CachePolicyCheckAcquireTimeMin extends CachePolicyCheck {
    private long value;
    public CachePolicyCheckAcquireTimeMin(String value) {
        super(value);
        this.value = Long.parseLong(value);
    }
    public boolean check(CacheObject co, CacheStats stats) {
        return co.getAcquireTimeMs() >= value;
    }
}
class CachePolicyCheckAcquireTimeMax extends CachePolicyCheck {
    private long value;
    public CachePolicyCheckAcquireTimeMax(String value) {
        super(value);
        this.value = Long.parseLong(value);
    }
    public boolean check(CacheObject co, CacheStats stats) {
        return co.getAcquireTimeMs() <= value;
    }
}
class CachePolicyCheckMode extends CachePolicyCheck {
    private CacheMode.Mode value;
    public CachePolicyCheckMode(String value) {
        super(value);
        this.value = CacheMode.Mode.valueOf(value);
    }
    public boolean check(CacheObject co, CacheStats stats) {
        return co.getMode().equals(value);
    }
}
class CachePolicyCheckClass extends CachePolicyCheck {
    private Class value;
    public CachePolicyCheckClass(String value) {
        super(value);
        try {
            this.value = Class.forName(value);
        } catch (Exception ex) {
            this.value = Object.class;
        }
    }
    public boolean check(CacheObject co, CacheStats stats) {
        return co.getObjectClass().isAssignableFrom(value);
    }
}

class CachePolicyCheckClassSuper extends CachePolicyCheck {
    private Class value;
    public CachePolicyCheckClassSuper(String value) {
        super(value);
        try {
            this.value = Class.forName(value);
        } catch (Exception ex) {
            this.value = Object.class;
        }
    }
    public boolean check(CacheObject co, CacheStats stats) {
        return value.isAssignableFrom(co.getObjectClass());
    }
}
class CachePolicyCheckClassName extends CachePolicyCheck {
    private String value;
    public CachePolicyCheckClassName(String value) {
        super(value);
        this.value = value;
    }
    public boolean check(CacheObject co, CacheStats stats) {
        return co.getObjectClass().getName().contains(value);
    }
}
class CachePolicyCheckThreadName extends CachePolicyCheck {
    private String value;
    public CachePolicyCheckThreadName(String value) {
        super(value);
        this.value = value;
    }
    public boolean check(CacheObject co, CacheStats stats) {
        return Thread.currentThread().getName().contains(value);
    }
}
class CachePolicyCheckThreadGroup extends CachePolicyCheck {
    private String value;
    public CachePolicyCheckThreadGroup(String value) {
        super(value);
        this.value = value;
    }
    public boolean check(CacheObject co, CacheStats stats) {
        return Thread.currentThread().getThreadGroup().getName().contains(value);
    }
}
class CachePolicyCheckMemFree extends CachePolicyCheck {
    private long value;
    public CachePolicyCheckMemFree(String value) {
        super(value);
        this.value = Long.parseLong(value);
    }
    public boolean check(CacheObject co, CacheStats stats) {
        return stats.getFreeMemory() <= value;
    }
}

class CachePolicyCheckMemTotal extends CachePolicyCheck {
    private long value;
    public CachePolicyCheckMemTotal(String value) {
        super(value);
        this.value = Long.parseLong(value);
    }
    public boolean check(CacheObject co, CacheStats stats) {
        return stats.getTotalMemory() <= value;
    }
}
class CachePolicyCheckMemMax extends CachePolicyCheck {
    private long value;
    public CachePolicyCheckMemMax(String value) {
        super(value);
        this.value = Long.parseLong(value);
    }
    public boolean check(CacheObject co, CacheStats stats) {
        return stats.getMaxMemory() <= value;
    }
}

class CachePolicyCheckKeyStarts extends CachePolicyCheck {
    private String value;
    public CachePolicyCheckKeyStarts(String value) {
        super(value);
        this.value = value;
    }
    public boolean check(CacheObject co, CacheStats stats) {
        return co.getKey().endsWith(value);
    }
}
class CachePolicyCheckKeyEnds extends CachePolicyCheck {
    private String value;
    public CachePolicyCheckKeyEnds(String value) {
        super(value);
        this.value = value;
    }
    public boolean check(CacheObject co, CacheStats stats) {
        return co.getKey().startsWith(value);
    }
}
class CachePolicyCheckKeyContains extends CachePolicyCheck {
    private String value;
    public CachePolicyCheckKeyContains(String value) {
        super(value);
        this.value = value;
    }
    public boolean check(CacheObject co, CacheStats stats) {
        return co.getKey().contains(value);
    }
}

class CachePolicyCheckKeyNotContains extends CachePolicyCheck {
    private String value;
    public CachePolicyCheckKeyNotContains(String value) {
        super(value);
        this.value = value;
    }
    public boolean check(CacheObject co, CacheStats stats) {
        return !co.getKey().contains(value);
    }
}




