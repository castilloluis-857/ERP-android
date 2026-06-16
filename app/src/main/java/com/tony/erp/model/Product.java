package com.tony.erp.model;

public class Product {
    public long id;
    public String name;
    public String description;
    public double price;
    public int stock;
    public Category category;
    public boolean active;

    @Override
    public String toString() { return name; } // para los Spinners
}
