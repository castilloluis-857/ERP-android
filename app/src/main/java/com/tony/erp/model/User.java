package com.tony.erp.model;

import java.util.List;

public class User {
    public long id;
    public String username;
    public String email;
    public String password;
    public boolean active;
    public List<Role> roles;

    public String getPrimaryRole() {
        if (roles != null && !roles.isEmpty()) return roles.get(0).name;
        return "ROLE_EMPLOYEE";
    }

    public boolean isAdmin() {
        return "ROLE_ADMIN".equals(getPrimaryRole());
    }
}
