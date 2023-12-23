package com.distsystem.test.custom.model;

import com.distsystem.utils.DistUtils;

// user that is ordering items
public class User extends BaseTable {

    public String userName;
    public String userLogin;
    public long totalOrdersCount;
    public String userCategory;
    public String getKey() { return userLogin; }
    public String getParentKey() { return userCategory; }

    public static User createUser(int id, String userName, String userLogin, String userCategory) {
        User u = new User();
        u.id = id;
        u.userName = userName;
        u.userLogin = userLogin;
        u.userCategory = userCategory;
        u.totalOrdersCount = 0;
        return u;
    }
    /** create many standard users */
    public static User[] createUsers(int objectsCount, int categoriesCount) {
        User[] users = new User[objectsCount];
        for (int i=0; i<objectsCount; i++) {
            String category = "category" + DistUtils.randomInt(categoriesCount);
            users[i] = createUser(i, "user" + i, "login" + i, category);
        }
        return users;
    }

}
