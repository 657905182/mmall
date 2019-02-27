package com.mmall.common;

/**
 * 〈常量〉
 *
 * @author liu
 * @create 2019/2/25
 * @since 1.0.0
 */
public class Const {
    public static final String CURRENT_USER = "currentUser";

    public static final String USERNAME = "username";

    public static final String EMAIL = "email";

    public interface Role{
        int ROLE_CUSTOMER = 0;//普通用户
        int ROLE_ADMIN = 1;//管理员
    }
}
