package com.distsystem.test.model;

import com.distsystem.utils.DistUtils;

public class Order extends BaseTable {

    public long orderSeq;
    public String orderCode;
    public String userLogin; // each order is having one assigned user
    public double totalPrice;
    public String warehouseCode;
    public String getKey() { return orderCode; }
    public String getParentKey() { return userLogin; }
    public static Order createOrder(int id, long orderSeq, String orderCode, String userLogin, String warehouseCode) {
        Order obj = new Order();
        obj.id = id;
        obj.orderSeq = orderSeq;
        obj.orderCode = orderCode;
        obj.userLogin = userLogin;
        obj.totalPrice = 0.0;
        obj.warehouseCode = warehouseCode;
        return obj;
    }
    /** create many orders from users and resources */
    public static Order[] createOrders(User[] users, Warehouse[] warehouses, int totalOrders) {
        Order[] objs = new Order[totalOrders];
        for (int i=0; i<totalOrders; i++) {
            User user = users[DistUtils.randomInt(users.length)];
            Warehouse warehouse = warehouses[DistUtils.randomInt(warehouses.length)];
            objs[i] = createOrder(i, i, "order"+i, user.userLogin, warehouse.warehouseCode);
        }
        return objs;
    }

}
