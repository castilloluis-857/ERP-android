package com.tony.erp.ui.products;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tony.erp.R;
import com.tony.erp.model.Product;
import java.util.List;

/**
 * Adapter del RecyclerView de productos.
 * Muestra nombre, categoría, precio y stock de cada producto.
 */
public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    public interface OnProductClick { void onClick(Product product); }

    private final List<Product> products;
    private final OnProductClick listener;

    public ProductAdapter(List<Product> products, OnProductClick listener) {
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product p = products.get(position);
        holder.tvName.setText(p.name);
        holder.tvCategory.setText(p.category != null ? p.category.name : "Sin categoría");
        holder.tvPrice.setText(String.format("%.2f €", p.price));
        holder.tvStock.setText("Stock: " + p.stock);

        // Color del stock según disponibilidad
        int colorRes;
        if (p.stock == 0)      colorRes = android.R.color.holo_red_dark;
        else if (p.stock <= 5) colorRes = android.R.color.holo_orange_dark;
        else                   colorRes = android.R.color.holo_green_dark;
        holder.tvStock.setTextColor(holder.itemView.getContext().getColor(colorRes));

        holder.itemView.setOnClickListener(v -> listener.onClick(p));
    }

    @Override
    public int getItemCount() { return products.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory, tvPrice, tvStock;
        ViewHolder(View v) {
            super(v);
            tvName     = v.findViewById(R.id.tvProductName);
            tvCategory = v.findViewById(R.id.tvCategory);
            tvPrice    = v.findViewById(R.id.tvPrice);
            tvStock    = v.findViewById(R.id.tvStock);
        }
    }
}
