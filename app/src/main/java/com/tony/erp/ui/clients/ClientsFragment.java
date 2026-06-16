package com.tony.erp.ui.clients;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.*;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.tony.erp.R;
import com.tony.erp.databinding.FragmentClientsBinding;
import com.tony.erp.model.Client;
import com.tony.erp.network.ApiClient;
import java.util.*;
import retrofit2.*;

public class ClientsFragment extends Fragment {

    private FragmentClientsBinding binding;
    private ApiClient apiClient;
    private ClientAdapter adapter;
    private final List<Client> clientList = new ArrayList<>();
    private final android.os.Handler searchHandler = new android.os.Handler();
    private Runnable searchRunnable;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentClientsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        apiClient = ApiClient.getInstance(requireContext());

        adapter = new ClientAdapter(clientList, this::showClientDialog);
        binding.recyclerView.setAdapter(adapter);

        // FAB — cualquier rol puede crear clientes
        binding.fab.setOnClickListener(v -> showClientDialog(null));

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchHandler.removeCallbacks(searchRunnable);
                searchRunnable = () -> loadClients(s.toString());
                searchHandler.postDelayed(searchRunnable, 500);
            }
        });

        loadClients(null);
    }

    private void loadClients(String search) {
        binding.progressBar.setVisibility(View.VISIBLE);
        Call<List<Client>> call = (search == null || search.isEmpty())
            ? apiClient.getApi().getAllClients()
            : apiClient.getApi().getClients(search);

        call.enqueue(new Callback<List<Client>>() {
            @Override public void onResponse(Call<List<Client>> c, Response<List<Client>> r) {
                binding.progressBar.setVisibility(View.GONE);
                if (r.isSuccessful() && r.body() != null) {
                    clientList.clear();
                    clientList.addAll(r.body());
                    adapter.notifyDataSetChanged();
                    binding.tvEmpty.setVisibility(clientList.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }
            @Override public void onFailure(Call<List<Client>> c, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showClientDialog(@Nullable Client client) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_client, null);
        EditText etNif   = dialogView.findViewById(R.id.etNif);
        EditText etName  = dialogView.findViewById(R.id.etName);
        EditText etEmail = dialogView.findViewById(R.id.etEmail);
        EditText etPhone = dialogView.findViewById(R.id.etPhone);

        if (client != null) {
            etNif.setText(client.nif);
            etName.setText(client.name);
            etEmail.setText(client.email);
            etPhone.setText(client.phone);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
            .setTitle(client == null ? "Nuevo cliente" : "Editar cliente")
            .setView(dialogView)
            .setPositiveButton("Guardar", (d, w) -> {
                String name = etName.getText().toString().trim();
                String nif  = etNif.getText().toString().trim().toUpperCase();
                if (name.isEmpty() || nif.isEmpty()) {
                    Toast.makeText(getContext(), "NIF y nombre son obligatorios", Toast.LENGTH_SHORT).show();
                    return;
                }
                Client c = client != null ? client : new Client();
                c.nif   = nif;
                c.name  = name;
                c.email = etEmail.getText().toString().trim();
                c.phone = etPhone.getText().toString().trim();
                c.active = true;
                saveClient(c, client != null);
            })
            .setNegativeButton("Cancelar", null);

        // Borrar solo si es ADMIN y es edición
        if (client != null && apiClient.getSession().isAdmin()) {
            builder.setNeutralButton("Desactivar", (d, w) -> confirmDelete(client));
        }
        builder.show();
    }

    private void saveClient(Client client, boolean isEdit) {
        Call<Client> call = isEdit
            ? apiClient.getApi().updateClient(client.id, client)
            : apiClient.getApi().createClient(client);

        call.enqueue(new Callback<Client>() {
            @Override public void onResponse(Call<Client> c, Response<Client> r) {
                if (r.isSuccessful()) {
                    Toast.makeText(getContext(), "Cliente guardado", Toast.LENGTH_SHORT).show();
                    loadClients(null);
                } else {
                    Toast.makeText(getContext(), "Error: " + r.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<Client> c, Throwable t) {
                Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDelete(Client client) {
        new AlertDialog.Builder(getContext())
            .setTitle("Confirmar")
            .setMessage("¿Desactivar a '" + client.name + "'?\nSus ventas históricas se conservarán.")
            .setPositiveButton("Desactivar", (d, w) ->
                apiClient.getApi().deleteClient(client.id).enqueue(new Callback<okhttp3.ResponseBody>() {
                    @Override public void onResponse(Call<okhttp3.ResponseBody> c, Response<okhttp3.ResponseBody> r) {
                        Toast.makeText(getContext(), "Cliente desactivado", Toast.LENGTH_SHORT).show();
                        loadClients(null);
                    }
                    @Override public void onFailure(Call<okhttp3.ResponseBody> c, Throwable t) {}
                }))
            .setNegativeButton("Cancelar", null)
            .show();
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
