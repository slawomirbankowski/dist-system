package com.distsystem.test.dao;

import com.distsystem.interfaces.Cache;
import com.distsystem.test.model.BaseTable;

import java.util.List;

public class DatabaseCacheTable<T extends BaseTable> extends DatabaseTable<T> {

    public DatabaseTable<T> table;
    private Cache cache;

    public DatabaseCacheTable(DatabaseTable<T> table, Cache cache) {
        this.cache = cache;
        this.table = table;
        this.initialTable = table.initialTable;
        this.obs = table.obs;
        this.objsById = table.objsById;
        this.objsByName = table.objsByName;
    }
    public void insert(T newObj) {
        // TODO: remove object from cache
        this.table.insert(newObj);
    }
    /** */
    public void insertMany(T[] newObjs) {
        // TODO: remove objects from cache
        this.table.insertMany(newObjs);
    }
    public T getById(int id) {
        // TODO: wrap with cache
        return this.table.getById(id);
    }
    public T getByKey(String key) {
        return this.table.getByKey(key);
    }
    public List<T> search(String str) {
        return this.table.search(str);
    }
    public List<T> getByParentKey(String parentKey) {
        return this.table.getByParentKey(parentKey);
    }
    public T getLast() {
        return this.table.getLast();
    }

}
