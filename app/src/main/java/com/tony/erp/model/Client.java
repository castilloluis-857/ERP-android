package com.tony.erp.model;

public class Client {
    public long id;
    public String nif;
    public String name;
    public String email;
    public String phone;
    public boolean active;

    @Override
    public String toString() { return name + " (" + nif + ")"; }
}
