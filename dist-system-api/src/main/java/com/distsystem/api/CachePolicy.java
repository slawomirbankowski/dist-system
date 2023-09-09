package com.distsystem.api;

import com.distsystem.utils.CacheStats;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/** Cache policy - set of items to be checked and applied to change CacheObjects before entering them into Storages
 * each PolicyItem contains set of rules and set of applies */
public class CachePolicy {

    /** items to be checked and applied to CacheObject, each  */
    private final List<CachePolicyItem> items = new LinkedList<>();

    /** creates new policy for adjusting CacheObjects */
    CachePolicy() {
    }
    /** add cache item to this policy */
    public void addItem(CachePolicyItem item) {
        items.add(item);
    }
    public void addItems(List<CachePolicyItem> its) {
        items.addAll(its);
    }
    /** check all items and apply these that are */
    public List<CachePolicyItem> checkAndApply(CacheObject co, CacheStats stats) {
        return items.stream().filter(ap -> ap.checkAndApply(co, stats)).collect(Collectors.toList());
    }

    /** get number of items in this policy */
    public int getItemsCount() {
        return items.size();
    }
    /** */
    public List<String> getItemsDefinitions() {
        return items.stream().map(i -> i.toString()).collect(Collectors.toList());
    }
    public List<CachePolicyItem> getItems() {
        return items;
    }
    /** create String from given policy*/
    public String toString() {
        return String.join(";", items.stream().map(x -> x.toString()).collect(Collectors.toList()));
    }
}
