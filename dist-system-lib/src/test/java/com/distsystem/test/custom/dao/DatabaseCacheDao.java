package com.distsystem.test.custom.dao;

import com.distsystem.interfaces.Cache;
import com.distsystem.test.custom.model.*;

public class DatabaseCacheDao extends DatabaseDao {

    protected Cache cache;
    protected DatabaseDao dao;
    protected DatabaseCacheTable<Warehouse> warehousesCache; // warehouses with products
    protected DatabaseCacheTable<User> usersCache; // list of users/ customers
    protected DatabaseCacheTable<Product> productsCache; // products to be sold
    protected DatabaseCacheTable<WarehouseStock> stocksCache; // products in warehouses
    protected DatabaseCacheTable<Order> ordersCache; // order for user with products
    protected DatabaseCacheTable<OrderItem> orderItemsCache; // single product with item
    protected DatabaseCacheTable<UserPreference> preferencesCache; // preferences of users toward products

    public DatabaseCacheDao(Cache cache, DatabaseDao dao) {
        this.cache = cache;
        this.dao = dao;
        this.warehouses = dao.warehouses;
        this.users = dao.users;
        this.products = dao.products;
        this.stocks = dao.stocks;
        this.orders = dao.orders;
        this.orderItems = dao.orderItems;
        this.preferences = dao.preferences;
        warehousesCache = new DatabaseCacheTable<Warehouse>(dao.getWarehouses(), cache);
        usersCache = new DatabaseCacheTable<User>(dao.getUsers(), cache);
        productsCache = new DatabaseCacheTable<Product>(dao.getProducts(), cache);
        stocksCache = new DatabaseCacheTable<WarehouseStock>(dao.getWarehouseStocks(), cache);
        ordersCache = new DatabaseCacheTable<Order>(dao.getOrders(), cache);
        orderItemsCache = new DatabaseCacheTable<OrderItem>(dao.getOrderItems(), cache);
        preferencesCache = new DatabaseCacheTable<UserPreference>(dao.getUserPreferences(), cache);
    }

    public DatabaseTable<Warehouse> getWarehouses() { return warehousesCache; }
    public DatabaseTable<User> getUsers() { return usersCache; }
    public DatabaseTable<Product> getProducts() { return productsCache; }
    public DatabaseTable<WarehouseStock> getWarehouseStocks() { return stocksCache; }
    public DatabaseTable<Order> getOrders() { return ordersCache; }
    public DatabaseTable<OrderItem> getOrderItems() { return orderItemsCache; }
    public DatabaseTable<UserPreference> getUserPreferences() { return preferencesCache; }

}
