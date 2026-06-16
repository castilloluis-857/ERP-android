package com.tony.erp.ui.clients;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tony.erp.R;
import com.tony.erp.model.Client;
import java.util.List;

public class ClientAdapter extends RecyclerView.Adapter<ClientAdapter.ViewHolder> {

    public interface OnClick { void onClick(Client c); }

    private final List<Client> list;
    private final OnClick listener;

    public ClientAdapter(List<Client> list, OnClick listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_client, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Client c = list.get(pos);
        h.tvName.setText(c.name);
        h.tvNif.setText("NIF: " + c.nif);
        h.tvEmail.setText(c.email != null ? c.email : "");
        h.itemView.setOnClickListener(v -> listener.onClick(c));
    }

    @Override public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvNif, tvEmail;
        ViewHolder(View v) {
            super(v);
            tvName  = v.findViewById(R.id.tvClientName);
            tvNif   = v.findViewById(R.id.tvNif);
            tvEmail = v.findViewById(R.id.tvEmail);
        }
    }
}
