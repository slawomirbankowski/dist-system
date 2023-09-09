package com.distsystem.api;

import com.distsystem.utils.DistUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/** builder for policy set priority, ttl, mode for cache */
public class CachePolicyBuilder {

    /** building policy */
    private CachePolicy currentPolicy = new CachePolicy();
    /** current building item */
    private CachePolicyItem currentItem = new CachePolicyItem();

    /** creates new policy builder */
    private CachePolicyBuilder() {
    }

    /** parse line with policy.
     * Policy items are separated by ;
     * Checks and appliers are separated by ,
     * Sample policy items:
     *  sizeMin=10,sizeMax=200,classLike=MyApplication,applySizeAdd=10,applySizeMultiply=2,applyMode=KEEP
     *
     * */
    public CachePolicyBuilder parse(String policyStr) {
        currentPolicy.addItems(parsePolicyItems(policyStr));
        return this;
    }

    /** create next item to this policy*/
    public CachePolicyBuilder next() {
        if (currentItem.isNonEmpty()) {
            currentPolicy.addItem(currentItem);
        }
        currentItem = new CachePolicyItem();
        return this;
    }

    public CachePolicyBuilder checkSize(long minSize, long maxSize) {
        currentItem.addCheck(new CachePolicyCheckSizeMin(""+minSize));
        currentItem.addCheck(new CachePolicyCheckSizeMax(""+maxSize));
        return this;
    }
    public CachePolicyBuilder checkSizeMin(long minSize) {
        currentItem.addCheck(new CachePolicyCheckSizeMin(""+minSize));
        return this;
    }
    public CachePolicyBuilder checkSizeMax(long maxSize) {
        currentItem.addCheck(new CachePolicyCheckSizeMax(""+maxSize));
        return this;
    }
    public CachePolicyBuilder checkTtl(long minSize, long maxSize) {
        currentItem.addCheck(new CachePolicyCheckTtlMin(""+minSize));
        currentItem.addCheck(new CachePolicyCheckTtlMax(""+maxSize));
        return this;
    }
    public CachePolicyBuilder checkTtlMin(long minSize) {
        currentItem.addCheck(new CachePolicyCheckTtlMin(""+minSize));
        return this;
    }
    public CachePolicyBuilder checkTtlMax(long maxSize) {
        currentItem.addCheck(new CachePolicyCheckTtlMax(""+maxSize));
        return this;
    }
    public CachePolicyBuilder checkPriority(long minValue, long maxValue) {
        currentItem.addCheck(new CachePolicyCheckPriorityMin(""+minValue));
        currentItem.addCheck(new CachePolicyCheckPriorityMax(""+maxValue));
        return this;
    }
    public CachePolicyBuilder checkPriorityMin(long minValue) {
        currentItem.addCheck(new CachePolicyCheckPriorityMin(""+minValue));
        return this;
    }
    public CachePolicyBuilder checkPriorityMax(long maxValue) {
        currentItem.addCheck(new CachePolicyCheckPriorityMax(""+maxValue));
        return this;
    }
    public CachePolicyBuilder checkAcquireTime(long minValue, long maxValue) {
        currentItem.addCheck(new CachePolicyCheckAcquireTimeMin(""+minValue));
        currentItem.addCheck(new CachePolicyCheckAcquireTimeMax(""+maxValue));
        return this;
    }
    public CachePolicyBuilder checkAcquireTimeMin(long minValue) {
        currentItem.addCheck(new CachePolicyCheckAcquireTimeMin(""+minValue));
        return this;
    }
    public CachePolicyBuilder checkAcquireTimeMax(long maxValue) {
        currentItem.addCheck(new CachePolicyCheckAcquireTimeMax(""+maxValue));
        return this;
    }
    public CachePolicyBuilder checkThread(String threadContains) {
        currentItem.addCheck(new CachePolicyCheckThreadName(threadContains));
        return this;
    }
    public CachePolicyBuilder checkMode(CacheMode.Mode mode) {
        currentItem.addCheck(new CachePolicyCheckMode(mode.name()));
        return this;
    }
    public CachePolicyBuilder checkThreadGroup(String threadGroupContains) {
        currentItem.addCheck(new CachePolicyCheckThreadGroup(threadGroupContains));
        return this;
    }
    public CachePolicyBuilder checkMemoryFree(long freeMemory) {
        currentItem.addCheck(new CachePolicyCheckMemFree(""+freeMemory));
        return this;
    }
    public CachePolicyBuilder checkKeyStarts(String key) {
        currentItem.addCheck(new CachePolicyCheckKeyStarts(key));
        return this;
    }
    public CachePolicyBuilder checkKeyEnds(String key) {
        currentItem.addCheck(new CachePolicyCheckKeyEnds(key));
        return this;
    }
    public CachePolicyBuilder checkKeyContains(String key) {
        currentItem.addCheck(new CachePolicyCheckKeyContains(key));
        return this;
    }
    public CachePolicyBuilder checkKeyNotContains(String key) {
        currentItem.addCheck(new CachePolicyCheckKeyNotContains(key));
        return this;
    }

    public CachePolicyBuilder applyPriority(int priority) {
        currentItem.addApply(new CachePolicyApplyPrioritySet(""+priority));
        return this;
    }
    public CachePolicyBuilder applyPriorityIncrease(int priority) {
        currentItem.addApply(new CachePolicyApplyPriorityIncrease(""+priority));
        return this;
    }
    public CachePolicyBuilder applyPriorityDecrease(int priority) {
        currentItem.addApply(new CachePolicyApplyPriorityDecrease(""+priority));
        return this;
    }
    public CachePolicyBuilder applySizeAdd(int size) {
        currentItem.addApply(new CachePolicyApplyPrioritySet(""+size));
        return this;
    }
    public CachePolicyBuilder applySizeMultiply(int size) {
        currentItem.addApply(new CachePolicyApplySizeMultiply(""+size));
        return this;
    }

    public CachePolicyBuilder applyTtlMultiply(int multipleBy) {
        currentItem.addApply(new CachePolicyApplyTtlMultiply(""+multipleBy));
        return this;
    }
    public CachePolicyBuilder applyTtlDivide(int divideBy) {
        currentItem.addApply(new CachePolicyApplyTtlMultiply(""+divideBy));
        return this;
    }
    public CachePolicyBuilder applyTtlAdd(int addValue) {
        currentItem.addApply(new CachePolicyApplyTtlAdd(""+addValue));
        return this;
    }
    public CachePolicyBuilder applyMode(CacheMode.Mode mode) {
        currentItem.addApply(new CachePolicyApplyMode(mode.name()));
        return this;
    }

    /** create new policy from this builder */
    public CachePolicy create() {
        next();
        return currentPolicy;
    }

    /** empty */
    public static CachePolicyBuilder empty() {
        return new CachePolicyBuilder();
    }

    /** creates policy builder starting from String */
    public CachePolicyBuilder fromString(String policyStr) {
        return empty().parse(policyStr);
    }

    /** parse policy items from full String */
    private static List<CachePolicyItem> parsePolicyItems(String policyStr) {
        List<CachePolicyItem> items = new LinkedList<>();
        String[] policyItems = policyStr.split(";");
        for (String policyItemStr: policyItems) {
            CachePolicyItem item = parsePolicyItem(policyItemStr.trim());
            items.add(item);
        }
        return items;
    }

    /** parse single policy item from String */
    private static CachePolicyItem parsePolicyItem(String itemStr) {
        List<String[]> rulesStrList = DistUtils.splitBySeparationEqual(itemStr, ",", '=', true);
        CachePolicyItem item = new CachePolicyItem();
        for (String[] ruleStr: rulesStrList) {
            String attrName = ruleStr[0].trim();
            String attrValue = ruleStr[1].trim();
            if (attrName.startsWith("apply")) {
                CachePolicyApply apply = createApply(attrName, attrValue);
                item.addApply(apply);
            } else {
                CachePolicyCheck check = createCheck(attrName, attrValue);
                item.addCheck(check);
            }
        }
        return item;
    }

    /** all possible check classes by attribute name */
    private static Map<String, Class> checkClasses = populateAllChecks();
    /** default empty check, always returning true */
    private static CachePolicyCheck defaultCheck = new CachePolicyCheckEmpty("");
    /** all possible apply classes by attribute name */
    private static Map<String, Class> applyClasses = populateAllApplies();    /** default empty check, always returning true */
    /** default apply that is not applying anything */
    private static CachePolicyApply defaultApply = new CachePolicyApplyEmpty("");

    /** populate all known checks */
    private static Map<String, Class> populateAllChecks() {
        Map<String, Class> checks = new HashMap<>();
        checks.put("empty", CachePolicyCheckEmpty.class);
        checks.put("sizeMin", CachePolicyCheckSizeMin.class);
        checks.put("sizeMax", CachePolicyCheckSizeMax.class);
        checks.put("ttlMin", CachePolicyCheckTtlMin.class);
        checks.put("ttlMax", CachePolicyCheckTtlMax.class);
        checks.put("priorityMin", CachePolicyCheckPriorityMin.class);
        checks.put("priorityMax", CachePolicyCheckPriorityMax.class);
        checks.put("acquireTimeMin", CachePolicyCheckAcquireTimeMin.class);
        checks.put("acquireTimeMax", CachePolicyCheckAcquireTimeMax.class);
        checks.put("mode", CachePolicyCheckMode.class);
        checks.put("class", CachePolicyCheckClass.class);
        checks.put("classSuper", CachePolicyCheckClassSuper.class);
        checks.put("className", CachePolicyCheckClassName.class);
        checks.put("threadName", CachePolicyCheckThreadName.class);
        checks.put("threadGroup", CachePolicyCheckThreadGroup.class);
        checks.put("memFree", CachePolicyCheckMemFree.class);
        checks.put("memTotal", CachePolicyCheckMemTotal.class);
        checks.put("memMax", CachePolicyCheckMemMax.class);
        checks.put("appName", CachePolicyApplyEmpty.class);
        checks.put("keyStarts", CachePolicyCheckKeyStarts.class);
        checks.put("keyContains", CachePolicyCheckKeyContains.class);
        checks.put("keyEnds", CachePolicyCheckKeyEnds.class);
        checks.put("keyNotContains", CachePolicyCheckKeyNotContains.class);
        checks.put("tags", CachePolicyApplyEmpty.class);
        return checks;
    }

    /** populate all known applies */
    private static Map<String, Class> populateAllApplies() {
        Map<String, Class> applies = new HashMap<>();
        applies.put("applyEmpty", CachePolicyApplyEmpty.class);
        applies.put("applyPrioritySet", CachePolicyApplyPrioritySet.class);
        applies.put("applyPriorityIncrease", CachePolicyApplyPriorityIncrease.class);
        applies.put("applyPriorityDecrease", CachePolicyApplyPriorityDecrease.class);
        applies.put("applySizeAdd", CachePolicyApplySizeAdd.class);
        applies.put("applySizeMultiply", CachePolicyApplySizeMultiply.class);
        applies.put("applyTtlMultiply", CachePolicyApplyTtlMultiply.class);
        applies.put("applyTtlDivide", CachePolicyApplyTtlDivide.class);
        applies.put("applyTtlAdd", CachePolicyApplyTtlAdd.class);
        applies.put("applyMode", CachePolicyApplyMode.class);
        applies.put("applyStorageInternal", CachePolicyApplyStorageInternal.class);
        applies.put("applyStorageExternal", CachePolicyApplyStorageExternal.class);
        applies.put("applyStorageSet", CachePolicyApplyStorageSet.class);
        applies.put("applyGroupSet", CachePolicyApplyGroupSet.class);
        return applies;
    }

    /** create check by name and value */
    private static CachePolicyCheck createCheck(String ruleItem, String ruleValue) {
        try {
            Class cl = checkClasses.get(ruleItem);
            CachePolicyCheck ch = (CachePolicyCheck)cl.getDeclaredConstructor(String.class).newInstance(ruleValue);
            return ch;
        } catch (Exception ex) {
            return defaultCheck;
        }
    }
    /** create apply by name and value */
    private static CachePolicyApply createApply(String ruleItem, String ruleValue) {
        try {
            Class cl = applyClasses.get(ruleItem);
            CachePolicyApply ap = (CachePolicyApply)cl.getDeclaredConstructor(String.class).newInstance(ruleValue);
            return ap;
        } catch (Exception ex) {
            return defaultApply;
        }
    }

}
