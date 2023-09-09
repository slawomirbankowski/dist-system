package com.distsystem.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ElasticInsertInfo {
    private String _index;
    private String _id;
    private int _version;
    private String result;

    public ElasticInsertInfo() {
    }

    public ElasticInsertInfo(String _index, String _id, int _version, String result) {
        this._index = _index;
        this._id = _id;
        this._version = _version;
        this.result = result;
    }

    public String get_index() {
        return _index;
    }

    public void set_index(String _index) {
        this._index = _index;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public int get_version() {
        return _version;
    }

    public void set_version(int _version) {
        this._version = _version;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
