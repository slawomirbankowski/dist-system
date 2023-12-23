package com.distsystem.test.custom.model;

import java.time.LocalDateTime;

public abstract class BaseTable {

    public LocalDateTime createdDate = LocalDateTime.now();

    public int id;
    public abstract String getKey();
    public abstract String getParentKey();
    public boolean rowSearch(String str) {
        return getKey().contains(str);
    }

}
