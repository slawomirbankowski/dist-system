package com.distsystem.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ElasticDocumentInfo {
    private String _index;
    private String _id;
    private int _version;
    private Boolean found;
    private Map<String, Object> _source;

    public ElasticDocumentInfo() {
    }
    public ElasticDocumentInfo(String _index, String _id, int _version, Boolean found, Map<String, Object> _source) {
        this._index = _index;
        this._id = _id;
        this._version = _version;
        this.found = found;
        this._source = _source;
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

    public Boolean getFound() {
        return found;
    }

    public void setFound(Boolean found) {
        this.found = found;
    }

    public Map<String, Object> get_source() {
        return  _source;
    }
    public void set_source(Map<String, Object> _source) {
        this._source = _source;
    }
    /** get map of results or Empty */
    public Optional<Map<String, Object>> getMap() {
        if (_source != null) {
            return Optional.of(_source);
        } else {
            return Optional.empty();
        }
    }
}
