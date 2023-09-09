package com.distsystem.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ElasticSearchInfo {
    private int took;
    private Boolean timed_out;
    private ElasticSearchHitsInfo hits;

    public ElasticSearchInfo() {
    }

    public ElasticSearchInfo(int took, Boolean timed_out, ElasticSearchHitsInfo hits) {
        this.took = took;
        this.timed_out = timed_out;
        this.hits = hits;
    }
    public int getTook() {
        return took;
    }
    public void setTook(int took) {
        this.took = took;
    }
    public Boolean getTimed_out() {
        return timed_out;
    }
    public void setTimed_out(Boolean timed_out) {
        this.timed_out = timed_out;
    }
    public ElasticSearchHitsInfo getHits() {
        return hits;
    }
    public void setHits(ElasticSearchHitsInfo hits) {
        this.hits = hits;
    }
    public List<ElasticDocumentInfo> getDocuments() {
        if (hits == null) {
            return List.of();
        }
        return hits.getHits();
    }
    public List<Map<String, Object>> getMaps() {
        return hits.getHits().stream().flatMap(x -> x.getMap().stream()).collect(Collectors.toList());
    }
}
