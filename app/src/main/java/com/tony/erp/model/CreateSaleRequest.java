package com.tony.erp.model;

import java.util.List;

public class CreateSaleRequest {
    public ClientRef client;
    public String status = "COMPLETED";
    public List<SaleItemRequest> items;

    public static class ClientRef { public long id; public ClientRef(long id) { this.id = id; } }
    public static class ProductRef { public long id; public ProductRef(long id) { this.id = id; } }
    public static class SaleItemRequest {
        public ProductRef product;
        public int quantity;
        public double unitPrice;
        public SaleItemRequest(long productId, int qty, double price) {
            this.product = new ProductRef(productId);
            this.quantity = qty;
            this.unitPrice = price;
        }
    }
}
