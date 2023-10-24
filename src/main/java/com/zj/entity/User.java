package com.zj.entity;

/**
 * The type User.
 *
 * @author zhengjie
 * @version 1.0
 * @date 2023 /10/24 16:49
 */
public class User {
    String id;
    String name;
    String email;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
