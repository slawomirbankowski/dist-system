package com.distsystem.test.custom.model;

import com.distsystem.utils.DistUtils;

public class OrderItem extends BaseTable {

    public long orderSeq;
    public String orderCode;
    public String productName; // each order have many resources
    public double productPrice; // order item has price
    public String getKey() { return ""+id+""+orderSeq+"-"+productName; }
    public String getParentKey() { return orderCode; }
    public static OrderItem createOrderItem(int id, long orderSeq, String orderCode, String productName, double productPrice) {
        OrderItem obj = new OrderItem();
        obj.id = id;
        obj.orderCode = orderCode;
        obj.orderSeq = orderSeq;
        obj.productName = productName;
        obj.productPrice = productPrice;
        return obj;
    }
    /** create many orders from users and resources */
    public static OrderItem[] createOrderItems(int objectsCount, Order[] orders, Product[] products) {
        OrderItem[] objs = new OrderItem[objectsCount];
        for (int i=0; i<objectsCount; i++) {
            Product product = products[DistUtils.randomInt(products.length)];
            Order order = orders[DistUtils.randomInt(orders.length)];
            objs[i] = createOrderItem(i, order.orderSeq, order.orderCode, product.productName, product.productPrice);
        }
        return objs;
    }

}
