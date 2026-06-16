package com.tony.erp.model;

public class Category {
    public long id;
    public String name;

    // Constructor vacío necesario para Gson
    public Category() {}
    public Category(long id, String name) { this.id = id; this.name = name; }

    @Override
    public String toString() { return name; } // para los Spinners
}
