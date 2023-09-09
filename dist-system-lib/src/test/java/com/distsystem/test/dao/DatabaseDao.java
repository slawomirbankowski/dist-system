package com.distsystem.test.dao;

import com.distsystem.test.model.*;

public class DatabaseDao {

    protected DatabaseTable<Warehouse> warehouses; // warehouses with products
    protected DatabaseTable<User> users; // list of users/ customers
    protected DatabaseTable<Product> products; // products to be sold
    protected DatabaseTable<WarehouseStock> stocks; // products in warehouses
    protected DatabaseTable<Order> orders; // order for user with products
    protected DatabaseTable<OrderItem> orderItems; // single product with item
    protected DatabaseTable<UserPreference> preferences; // preferences of users toward products
    public DatabaseDao() {
    }
    public void initializeItems(int warehousesCount, int usersCount, int userCategories, int productsCount, int categoriesCount,
                       int stockItemsCount, int maxItemsPerProduct,
                       int ordersCount, int preferencesCount) {
        warehouses = new DatabaseTable<Warehouse>();
        warehouses.setObjects(Warehouse.createWarehouses(warehousesCount));
        users = new DatabaseTable<User>();
        users.setObjects(User.createUsers(usersCount, userCategories));
        products = new DatabaseTable<Product>();
        products.setObjects(Product.createProducts(productsCount, categoriesCount));
        stocks = new DatabaseTable<WarehouseStock>();
        stocks.setObjects(WarehouseStock.createStocks(warehouses.initialTable, products.initialTable, stockItemsCount, maxItemsPerProduct));
        orders = new DatabaseTable<Order>();
        orders.setObjects(Order.createOrders(users.initialTable, warehouses.initialTable, ordersCount));
        orderItems = new DatabaseTable<OrderItem>();
        orderItems.setObjects(OrderItem.createOrderItems(10000, orders.initialTable, products.initialTable));
        preferences = new DatabaseTable<UserPreference>();
        preferences.setObjects(UserPreference.createPreferences(users.initialTable, products.initialTable, preferencesCount));
    }
    public DatabaseTable<Warehouse> getWarehouses() { return warehouses; }
    public DatabaseTable<User> getUsers() { return users; }
    public DatabaseTable<Product> getProducts() { return products; }
    public DatabaseTable<WarehouseStock> getWarehouseStocks() { return stocks; }
    public DatabaseTable<Order> getOrders() { return orders; }
    public DatabaseTable<OrderItem> getOrderItems() { return orderItems; }
    public DatabaseTable<UserPreference> getUserPreferences() { return preferences; }

}
