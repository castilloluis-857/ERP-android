package com.tony.erp.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo; // Importante añadir
import androidx.appcompat.app.AppCompatActivity;
import com.tony.erp.databinding.ActivityLoginBinding;
import com.tony.erp.model.AuthResponse;
import com.tony.erp.model.LoginRequest;
import com.tony.erp.network.ApiClient;
import com.tony.erp.ui.dashboard.DashboardActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Pantalla de Login del ERP.
 *
 * Flujo:
 *   1. El usuario introduce usuario y contraseña.
 *   2. Se envía al backend POST /api/usuarios/login.
 *   3. Si es correcto → guarda el token en SessionManager → abre DashboardActivity.
 *   4. Si falla → muestra el mensaje de error del backend.
 *
 * Si ya hay sesión activa (token guardado), salta directamente al Dashboard.
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private ApiClient apiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiClient = ApiClient.getInstance(this);

        // Si ya hay sesión activa, no mostramos el login
        if (apiClient.getSession().isLoggedIn()) {
            goToDashboard();
            return;
        }

        binding.btnLogin.setOnClickListener(v -> attemptLogin());

        // Lanzar login de forma segura al pulsar "Done" en el teclado
        binding.etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin();
                return true;
            }
            return false;
        });
    }

    /**
     * Valida los campos localmente y llama al backend.
     */
    private void attemptLogin() {
        String username = binding.etUsername.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        // Validación local usando tu caja de error común para evitar crasheos de Material Design
        if (username.isEmpty()) {
            showError("Introduce tu usuario");
            binding.etUsername.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            showError("Introduce tu contraseña");
            binding.etPassword.requestFocus();
            return;
        }

        setLoading(true);
        hideError();

        apiClient.getApi().login(new LoginRequest(username, password))
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        setLoading(false);
                        if (response.isSuccessful() && response.body() != null) {
                            AuthResponse auth = response.body();
                            if (auth.success && auth.token != null) {
                                // Guardamos la sesión y vamos al Dashboard
                                apiClient.getSession().saveSession(
                                        auth.token,
                                        auth.username != null ? auth.username : username,
                                        auth.role != null ? auth.role : "ROLE_EMPLOYEE"
                                );
                                goToDashboard();
                            } else {
                                showError(auth.message != null ? auth.message
                                        : "Usuario o contraseña incorrectos.");
                            }
                        } else {
                            showError("Error del servidor. Código: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        setLoading(false);
                        showError("No se pudo conectar con el servidor.\n" +
                                "Verifica que el backend esté activo.");
                    }
                });
    }

    private void goToDashboard() {
        startActivity(new Intent(this, DashboardActivity.class));
        finish();
    }

    private void setLoading(boolean loading) {
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!loading);
        binding.etUsername.setEnabled(!loading);
        binding.etPassword.setEnabled(!loading);
        binding.btnLogin.setText(loading ? "Conectando..." : "Iniciar sesión");
    }

    private void showError(String message) {
        binding.tvError.setText(message);
        binding.tvError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        binding.tvError.setVisibility(View.GONE);
    }
}