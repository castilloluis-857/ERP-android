package com.tony.erp.ui.users;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.tony.erp.R;
import com.tony.erp.model.User;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    public interface OnClick { void onClick(User u); }

    private final List<User> list;
    private final OnClick listener;

    public UserAdapter(List<User> list, OnClick listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        User u = list.get(pos);
        h.tvUsername.setText(u.username);
        h.tvEmail.setText(u.email != null ? u.email : "");
        h.tvRole.setText(u.getPrimaryRole().replace("ROLE_", ""));

        // Color del rol
        boolean isAdmin = u.isAdmin();
        h.tvRole.setTextColor(h.itemView.getContext().getColor(
            isAdmin ? android.R.color.holo_blue_dark : android.R.color.darker_gray));

        h.itemView.setOnClickListener(v -> listener.onClick(u));
    }

    @Override public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvEmail, tvRole;
        ViewHolder(View v) {
            super(v);
            tvUsername = v.findViewById(R.id.tvUsername);
            tvEmail    = v.findViewById(R.id.tvEmail);
            tvRole     = v.findViewById(R.id.tvRole);
        }
    }
}
