package com.distsystem.test.custom.model;

import com.distsystem.utils.DistUtils;

public class Product extends BaseTable {

    public String productName;
    public double productPrice;
    /** */
    public String productDescription;
    public String productCategory;
    public String getKey() { return productName; }
    public String getParentKey() { return productCategory; }
    public static Product createProduct(int id, String productName, double productPrice, String productDescription, String productCategory) {
        Product obj = new Product();
        obj.id = id;
        obj.productName = productName;
        obj.productPrice = productPrice;
        obj.productDescription = productDescription;
        return obj;
    }
    /** create many products*/
    public static Product[] createProducts(int objectsCount, int categoriesCount) {
        Product[] users = new Product[objectsCount];
        for (int i=0; i<objectsCount; i++) {
            String category = "category" + DistUtils.randomInt(categoriesCount);
            double price = DistUtils.randomDouble()*10;
            users[i] = createProduct(i, "product" + i, i, "description" + i, category);
        }
        return users;
    }

}
