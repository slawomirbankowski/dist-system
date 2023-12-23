package com.distsystem.test.custom.model;

import java.io.Serializable;

class SimpleObject implements Serializable {
    public String name;
    public int number;
    public SimpleObject(String name, int number) {
        this.name = name;
        this.number = number;
    }

    public String toString() {
        return "name=" + name + ", number=" + number;
    }
}