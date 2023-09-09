package com.distsystem.api;

import com.distsystem.utils.CacheStats;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/** single item in policy
 * single item contains set of checks and set of applies to CacheObject if all checks are TRUE */
public class CachePolicyItem {

    /** rules to be checked with CacheObject */
    private List<CachePolicyCheck> checks = new LinkedList<>();
    /** rules to be applied to CacheObject */
    private List<CachePolicyApply> applies = new LinkedList<>();

    /** check if this item is not empty - so there are checks OR applies */
    public boolean isNonEmpty() {
        return checks.size() > 0 || applies.size() > 0;
    }
    /** add check to this policy item */
    void addCheck(CachePolicyCheck check) {
        checks.add(check);
    }
    /** add apply to this policy item */
    void addApply(CachePolicyApply apply) {
        applies.add(apply);
    }
    /** check if all rules are TRUE for this cache object */
    private boolean check(CacheObject co, CacheStats stats) {
        if (checks.isEmpty()) {
            return true;
        } else {
            return checks.stream().allMatch(ch -> ch.check(co, stats));
        }
    }

    /** apply all rules to CacheObject */
    private void apply(CacheObject co) {
        applies.stream().forEach(ap -> ap.apply(co));
    }
    /** check all rules - if all are true then apply applies to this CacheObject */
    public boolean checkAndApply(CacheObject co, CacheStats stats) {
        if (check(co, stats)) {
            apply(co);
            return true;
        }
        return false;
    }
    /** create String describing this item */
    public String toString() {
        return String.join(",", checks.stream().map(x -> x.toString()).collect(Collectors.toList())) + "," + String.join(",", applies.stream().map(x -> x.toString()).collect(Collectors.toList()));
    }

}
