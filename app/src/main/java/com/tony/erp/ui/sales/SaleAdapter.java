package com.tony.erp.ui.sales;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tony.erp.R;
import com.tony.erp.model.Sale;
import java.util.List;

/**
 * Adapter del historial de ventas.
 */
public class SaleAdapter extends RecyclerView.Adapter<SaleAdapter.ViewHolder> {

    private final List<Sale> sales;

    public SaleAdapter(List<Sale> sales) {
        this.sales = sales;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sale, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Sale sale = sales.get(pos);
        h.tvId.setText("Venta #" + sale.id);
        h.tvClient.setText(sale.client != null ? sale.client.name : "Anónimo");
        h.tvDate.setText(sale.saleDate != null ? sale.saleDate.substring(0, Math.min(10, sale.saleDate.length())) : "");
        h.tvTotal.setText(String.format("%.2f €", sale.totalAmount));
        h.tvStatus.setText(sale.status);

        // Color del estado
        int color = "COMPLETED".equals(sale.status)
            ? android.R.color.holo_green_dark
            : android.R.color.holo_red_dark;
        h.tvStatus.setTextColor(h.itemView.getContext().getColor(color));
    }

    @Override public int getItemCount() { return sales.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvClient, tvDate, tvTotal, tvStatus;
        ViewHolder(View v) {
            super(v);
            tvId     = v.findViewById(R.id.tvSaleId);
            tvClient = v.findViewById(R.id.tvClient);
            tvDate   = v.findViewById(R.id.tvDate);
            tvTotal  = v.findViewById(R.id.tvTotal);
            tvStatus = v.findViewById(R.id.tvStatus);
        }
    }
}
