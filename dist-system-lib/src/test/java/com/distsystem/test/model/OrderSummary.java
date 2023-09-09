package com.distsystem.test.model;

public class OrderSummary extends BaseTable {

    public String userLogin;
    public int ordersCount;
    public int productsCount;

    public String getKey() { return userLogin; }
    public String getParentKey() { return userLogin; }

    /** create many orders from users and resources */
    public static OrderSummary calculateOrderSummary(String userLogin, int ordersCount, int productsCount) {
        OrderSummary summary = new OrderSummary();
        summary.id = 0;
        summary.userLogin = userLogin;
        summary.ordersCount = ordersCount;
        summary.productsCount = productsCount;
        return summary;
    }

}
