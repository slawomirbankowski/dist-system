package com.distsystem.test.custom.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

class ComplexTestObject implements Serializable {

    private BasicTestObject obj;
    protected List<BasicTestObject> objs = new LinkedList<>();
    public ComplexTestObject(int n) {
        obj = new BasicTestObject("item", n);
        for (int i=0; i<n; i++) {
            objs.add(new BasicTestObject("item" + i, i));
        }
    }
    public String toString() {
        return "ComplexObject:objs=" + objs.size();
    }
    public int size() {
        return objs.size();
    }
}