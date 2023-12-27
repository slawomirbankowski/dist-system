package com.distsystem.api;

import java.util.LinkedList;
import java.util.List;

public class AgentSearchResults {

    private List<AgentSearchResultItem> items;

    public AgentSearchResults(List<AgentSearchResultItem> items) {
        this.items = items;
    }
    public AgentSearchResults() {
        this.items = new LinkedList<AgentSearchResultItem>();
    }
    public List<AgentSearchResultItem> getItems() {
        return items;
    }
}
