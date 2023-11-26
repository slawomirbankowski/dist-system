package com.distsystem.api;

import java.util.List;
import java.util.Map;

public class AgentSearchQuery {
    private Map<String, List<String>> query;
    private String searchText;
    private int maxResults;
    private int maxSearchingTime;
    private List<String> searchScope;

    public AgentSearchQuery(Map<String, List<String>> query, String searchText, int maxResults, int maxSearchingTime, List<String> searchScope) {
        this.query = query;

        query.getOrDefault("", List.of(""));


        this.searchText = searchText;
        this.maxResults = maxResults;
        this.maxSearchingTime = maxSearchingTime;
        this.searchScope = searchScope;
    }

    public Map<String, List<String>> getQuery() {
        return query;
    }

    public String getSearchText() {
        return searchText;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public int getMaxSearchingTime() {
        return maxSearchingTime;
    }

    public List<String> getSearchScope() {
        return searchScope;
    }
}
