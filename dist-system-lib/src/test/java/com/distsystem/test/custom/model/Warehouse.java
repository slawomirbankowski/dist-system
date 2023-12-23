package com.distsystem.test.custom.model;

// warehouse in given location with many products
public class Warehouse extends BaseTable {

    public String warehouseCode;
    public String warehouseAddress;
    public long totalItemsCount;
    public String getKey() { return warehouseCode; }
    public String getParentKey() { return warehouseCode; }

    public static Warehouse createWarehouse(int id, String warehouseCode, String warehouseAddress) {
        Warehouse obj = new Warehouse();
        obj.id = id;
        obj.warehouseCode = warehouseCode;
        obj.warehouseAddress = warehouseAddress;
        obj.totalItemsCount = 0L;
        return obj;
    }
    /** create many warehouses */
    public static Warehouse[] createWarehouses(int objectsCount) {
        Warehouse[] objs = new Warehouse[objectsCount];
        for (int i=0; i<objectsCount; i++) {
            objs[i] = createWarehouse(i, "warehouse" + i, "warehouse address"+i);
        }
        return objs;
    }

}
