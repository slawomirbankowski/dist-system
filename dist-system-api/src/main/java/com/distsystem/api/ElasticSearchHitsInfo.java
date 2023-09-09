package com.distsystem.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ElasticSearchHitsInfo {
    private int took;
    private double max_score;
    private List<ElasticDocumentInfo> hits;

    public ElasticSearchHitsInfo() {
    }
    public ElasticSearchHitsInfo(int took, double max_score, List<ElasticDocumentInfo> hits) {
        this.took = took;
        this.max_score = max_score;
        this.hits = hits;
    }
    public int getTook() {
        return took;
    }

    public void setTook(int took) {
        this.took = took;
    }

    public double getMax_score() {
        return max_score;
    }

    public void setMax_score(double max_score) {
        this.max_score = max_score;
    }

    public List<ElasticDocumentInfo> getHits() {
        return hits;
    }

    public void setHits(List<ElasticDocumentInfo> hits) {
        this.hits = hits;
    }
}
