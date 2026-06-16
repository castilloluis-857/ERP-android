package com.tony.erp.ui.sales;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tony.erp.R;
import com.tony.erp.model.SaleItem;
import java.util.List;

/**
 * Adapter del carrito de la venta actual.
 * Muestra producto, cantidad, precio unitario y subtotal.
 * El botón X elimina el ítem del carrito.
 */
public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    public interface OnRemove { void onRemove(int position); }

    private final List<SaleItem> items;
    private final OnRemove onRemove;

    public CartAdapter(List<SaleItem> items, OnRemove onRemove) {
        this.items    = items;
        this.onRemove = onRemove;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        SaleItem item = items.get(pos);
        h.tvName.setText(item.product != null ? item.product.name : "Producto");
        h.tvQty.setText(item.quantity + " ×  " + String.format("%.2f €", item.unitPrice));
        h.tvSubtotal.setText(String.format("%.2f €", item.getSubtotal()));
        h.btnRemove.setOnClickListener(v -> onRemove.onRemove(h.getAdapterPosition()));
    }

    @Override public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvQty, tvSubtotal;
        ImageButton btnRemove;
        ViewHolder(View v) {
            super(v);
            tvName     = v.findViewById(R.id.tvProductName);
            tvQty      = v.findViewById(R.id.tvQty);
            tvSubtotal = v.findViewById(R.id.tvSubtotal);
            btnRemove  = v.findViewById(R.id.btnRemove);
        }
    }
}
