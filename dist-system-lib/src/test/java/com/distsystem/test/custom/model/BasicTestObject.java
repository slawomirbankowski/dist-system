package com.distsystem.test.custom.model;

import java.io.Serializable;

/** */
public class BasicTestObject implements Serializable {
    private String name;
    private int number;
    public BasicTestObject(String name, int number) {
        this.name = name;
        this.number = number;
    }
    public String toString() {
        return "SimpleObject:name=" + name + ",number=" + number;
    }
}