package com.distsystem.api;

import com.distsystem.api.enums.CacheStorageType;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/** apply of single item rule to CacheObject */
public abstract class CachePolicyApply {
    private String _value;
    public CachePolicyApply(String value) {
        this._value = value;
    }
    /** apply this rule to CacheObject */
    public abstract void apply(CacheObject co);
    public String toString() {
        return getClass().getSimpleName() + "=" + _value;
    }
}
class CachePolicyApplyEmpty extends CachePolicyApply {
    public CachePolicyApplyEmpty(String value) {
        super(value);
    }
    /** apply this rule to CacheObject */
    public void apply(CacheObject co) {
    }
}
class CachePolicyApplyPrioritySet extends CachePolicyApply {
    private int value;
    public CachePolicyApplyPrioritySet(String value) {
        super(value);
        this.value = Integer.parseInt(value);
    }
    /** apply this rule to CacheObject */
    public void apply(CacheObject co) {
        co.setPriority(value);
    }
}
class CachePolicyApplyPriorityIncrease extends CachePolicyApply {
    private int value;
    public CachePolicyApplyPriorityIncrease(String value) {
        super(value);
        this.value = Integer.parseInt(value);
    }
    /** apply this rule to CacheObject */
    public void apply(CacheObject co) {
        co.setPriority(co.getPriority()+value);
    }
}
class CachePolicyApplyPriorityDecrease extends CachePolicyApply {
    private int value;
    public CachePolicyApplyPriorityDecrease(String value) {
        super(value);
        this.value = Integer.parseInt(value);
    }
    /** apply this rule to CacheObject */
    public void apply(CacheObject co) {
        co.setPriority(co.getPriority()-value);
    }
}

class CachePolicyApplySizeAdd extends CachePolicyApply {
    private int value;
    public CachePolicyApplySizeAdd(String value) {
        super(value);
        this.value = Integer.parseInt(value);
    }
    /** apply this rule to CacheObject */
    public void apply(CacheObject co) {
        co.setSize(co.getSize()+value);
    }
}
class CachePolicyApplySizeMultiply extends CachePolicyApply {
    private int value;
    public CachePolicyApplySizeMultiply(String value) {
        super(value);
        this.value = Integer.parseInt(value);
    }
    /** apply this rule to CacheObject */
    public void apply(CacheObject co) {
        co.setSize(co.getSize()*value);
    }
}

class CachePolicyApplyTtlMultiply extends CachePolicyApply {
    private int value;
    public CachePolicyApplyTtlMultiply(String value) {
        super(value);
        this.value = Integer.parseInt(value);
    }
    /** apply this rule to CacheObject */
    public void apply(CacheObject co) {
        co.setTtl((long)(co.getTimeToLive()*value));
    }
}
class CachePolicyApplyTtlDivide extends CachePolicyApply {
    private double value;
    public CachePolicyApplyTtlDivide(String value) {
        super(value);
        this.value = Double.parseDouble(value);
        if (this.value == 0) {
            throw new IllegalArgumentException("TTL divide cannot be zero");
        }
    }
    /** apply this rule to CacheObject */
    public void apply(CacheObject co) {
        co.setTtl((long)(co.getTimeToLive()/value));
    }
}
class CachePolicyApplyTtlAdd extends CachePolicyApply {
    private long value;
    public CachePolicyApplyTtlAdd(String value) {
        super(value);
        this.value = Long.parseLong(value);
    }
    /** apply this rule to CacheObject */
    public void apply(CacheObject co) {
        co.setTtl((long)(co.getTimeToLive()+value));
    }
}

class CachePolicyApplyMode extends CachePolicyApply {
    private CacheMode.Mode mode;
    public CachePolicyApplyMode(String value) {
        super(value);
        mode = CacheMode.Mode.valueOf(value);
    }
    /** apply this rule to CacheObject */
    public void apply(CacheObject co) {
        co.setMode(mode);
    }
}
class CachePolicyApplyStorageInternal extends CachePolicyApply {
    private boolean value;
    public CachePolicyApplyStorageInternal(String value) {
        super(value);
        this.value = Boolean.parseBoolean(value);
    }
    /** apply this rule to CacheObject */
    public void apply(CacheObject co) {
        co.setSupportedStorages(CacheStorageType.internalStorages);
    }
}
class CachePolicyApplyStorageExternal extends CachePolicyApply {
    public CachePolicyApplyStorageExternal(String value) {
        super(value);
    }
    /** apply this rule to CacheObject */
    public void apply(CacheObject co) {
        co.setSupportedStorages(CacheStorageType.externalStorages);
    }
}
class CachePolicyApplyStorageSet extends CachePolicyApply {

    private Set<CacheStorageType> storageTypes;
    public CachePolicyApplyStorageSet(String value) {
        super(value);
        storageTypes = CacheStorageType.parseStorages(value);
    }
    /** apply this rule to CacheObject */
    public void apply(CacheObject co) {
        co.setSupportedStorages(storageTypes);
    }
}
class CachePolicyApplyGroupSet extends CachePolicyApply {
    private Set<String> groups;
    public CachePolicyApplyGroupSet(String value) {
        super(value);
        groups = Arrays.stream(value.split("\\|")).collect(Collectors.toSet());
    }
    /** apply this rule to CacheObject */
    public void apply(CacheObject co) {
        co.setGroups(groups);
    }
}