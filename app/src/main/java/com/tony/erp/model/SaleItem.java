package com.tony.erp.model;

import com.google.gson.annotations.SerializedName;

public class SaleItem {
    public long id;
    public Product product;
    public Integer quantity;
    @SerializedName("unitPrice")
    public Double unitPrice;

    // Constructor para crear ítems del carrito
    public SaleItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = product.price;
    }

    public SaleItem() {}

    public double getSubtotal() {
        if (unitPrice != null && quantity != null) return unitPrice * quantity;
        return 0;
    }
}
