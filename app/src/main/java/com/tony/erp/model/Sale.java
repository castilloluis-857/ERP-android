package com.tony.erp.model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class Sale {
    public long id;
    public Client client;
    public String saleDate;
    public String status = "COMPLETED";

    @SerializedName("total")
    public double totalAmount;

    public List<SaleItem> items = new ArrayList<>();

    public double getCalculatedTotal() {
        double total = 0;
        if (items != null) for (SaleItem item : items) total += item.getSubtotal();
        return total;
    }
}
