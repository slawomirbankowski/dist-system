package com.distsystem.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ElasticDocumentDeleteInfo {
    private int took;
    private String timed_out;
    private int total;
    private int deleted;

    public ElasticDocumentDeleteInfo() {
    }
    public ElasticDocumentDeleteInfo(int took, String timed_out, int total, int deleted) {
        this.took = took;
        this.timed_out = timed_out;
        this.total = total;
        this.deleted = deleted;
    }

    public int getTook() {
        return took;
    }
    public void setTook(int took) {
        this.took = took;
    }
    public String getTimed_out() {
        return timed_out;
    }
    public void setTimed_out(String timed_out) {
        this.timed_out = timed_out;
    }
    public int getTotal() {
        return total;
    }
    public void setTotal(int total) {
        this.total = total;
    }
    public int getDeleted() {
        return deleted;
    }
    public void setDeleted(int deleted) {
        this.deleted = deleted;
    }
}
