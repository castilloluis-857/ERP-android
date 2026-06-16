package com.tony.erp.ui.users;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.tony.erp.R;
import com.tony.erp.databinding.FragmentUsersBinding;
import com.tony.erp.model.AuthResponse;
import com.tony.erp.model.User;
import com.tony.erp.network.ApiClient;
import java.util.*;
import retrofit2.*;

/**
 * Fragment de gestión de usuarios (solo ROLE_ADMIN).
 *
 * - Lista todos los usuarios.
 * - Al pulsar un usuario → dialog para cambiar contraseña o eliminar.
 * - No se puede eliminar la cuenta propia (protección).
 */
public class UsersFragment extends Fragment {

    private FragmentUsersBinding binding;
    private ApiClient apiClient;
    private UserAdapter adapter;
    private final List<User> userList = new ArrayList<>();

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentUsersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        apiClient = ApiClient.getInstance(requireContext());

        adapter = new UserAdapter(userList, this::showUserDialog);
        binding.recyclerView.setAdapter(adapter);

        loadUsers();
    }

    private void loadUsers() {
        binding.progressBar.setVisibility(View.VISIBLE);
        apiClient.getApi().getUsers().enqueue(new Callback<List<User>>() {
            @Override public void onResponse(Call<List<User>> c, Response<List<User>> r) {
                binding.progressBar.setVisibility(View.GONE);
                if (r.isSuccessful() && r.body() != null) {
                    userList.clear();
                    userList.addAll(r.body());
                    adapter.notifyDataSetChanged();
                    binding.tvEmpty.setVisibility(userList.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }
            @Override public void onFailure(Call<List<User>> c, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUserDialog(User user) {
        String currentUser = apiClient.getSession().getUsername();

        // Inflamos el layout del dialog de cambio de contraseña
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_change_password, null);
        EditText etNewPass     = dialogView.findViewById(R.id.etNewPassword);
        EditText etConfirmPass = dialogView.findViewById(R.id.etConfirmPassword);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
            .setTitle("Usuario: " + user.username)
            .setMessage("Rol: " + user.getPrimaryRole().replace("ROLE_", ""))
            .setView(dialogView)
            .setPositiveButton("Cambiar contraseña", (d, w) -> {
                String newPass     = etNewPass.getText().toString().trim();
                String confirmPass = etConfirmPass.getText().toString().trim();

                if (newPass.length() < 4) {
                    Toast.makeText(getContext(), "Mínimo 4 caracteres", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!newPass.equals(confirmPass)) {
                    Toast.makeText(getContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                    return;
                }
                changePassword(user.id, newPass);
            })
            .setNegativeButton("Cancelar", null);

        // No se puede eliminar la cuenta propia
        if (!user.username.equals(currentUser)) {
            builder.setNeutralButton("Eliminar usuario", (d, w) -> confirmDeleteUser(user));
        }

        builder.show();
    }

    private void changePassword(long userId, String newPassword) {
        Map<String, String> body = new HashMap<>();
        body.put("nuevaPasswordPlain", newPassword);

        apiClient.getApi().changePassword(userId, body).enqueue(new Callback<AuthResponse>() {
            @Override public void onResponse(Call<AuthResponse> c, Response<AuthResponse> r) {
                if (r.isSuccessful()) {
                    Toast.makeText(getContext(), "✅ Contraseña actualizada correctamente", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Error al actualizar: " + r.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<AuthResponse> c, Throwable t) {
                Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDeleteUser(User user) {
        new AlertDialog.Builder(getContext())
            .setTitle("Eliminar usuario")
            .setMessage("¿Eliminar definitivamente a '" + user.username + "'?\nEsta acción no se puede deshacer.")
            .setPositiveButton("Eliminar", (d, w) ->
                apiClient.getApi().deleteUser(user.id).enqueue(new Callback<AuthResponse>() {
                    @Override public void onResponse(Call<AuthResponse> c, Response<AuthResponse> r) {
                        if (r.isSuccessful()) {
                            Toast.makeText(getContext(), "Usuario eliminado", Toast.LENGTH_SHORT).show();
                            loadUsers();
                        }
                    }
                    @Override public void onFailure(Call<AuthResponse> c, Throwable t) {}
                }))
            .setNegativeButton("Cancelar", null)
            .show();
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
