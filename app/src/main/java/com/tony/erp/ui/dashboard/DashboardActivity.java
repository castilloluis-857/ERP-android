package com.tony.erp.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tony.erp.R;
import com.tony.erp.databinding.ActivityDashboardBinding;
import com.tony.erp.network.ApiClient;
import com.tony.erp.ui.clients.ClientsFragment;
import com.tony.erp.ui.login.LoginActivity;
import com.tony.erp.ui.products.ProductsFragment;
import com.tony.erp.ui.sales.SalesFragment;
import com.tony.erp.ui.users.UsersFragment;

/**
 * Pantalla principal del ERP.
 *
 * Usa un BottomNavigationView para navegar entre módulos.
 * Cada módulo es un Fragment que se carga en el contenedor central.
 * El item "Usuarios" solo aparece si el usuario tiene ROLE_ADMIN.
 */
public class DashboardActivity extends AppCompatActivity {

    private ActivityDashboardBinding binding;
    private ApiClient.SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        session = ApiClient.getInstance(this).getSession();

        // Toolbar con nombre de usuario y rol
        setSupportActionBar(binding.toolbar);
        String userInfo = session.getUsername() + " · " +
            session.getRole().replace("ROLE_", "");
        binding.toolbar.setSubtitle(userInfo);

        // Ocultar el tab de Usuarios si no es ADMIN
        binding.bottomNav.getMenu()
            .findItem(R.id.nav_users)
            .setVisible(session.isAdmin());

        // Cargar el primer Fragment al abrir
        if (savedInstanceState == null) {
            loadFragment(new ProductsFragment());
            binding.bottomNav.setSelectedItemId(R.id.nav_products);
        }

        // Listener de la barra de navegación inferior
        binding.bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();

            if      (id == R.id.nav_products) fragment = new ProductsFragment();
            else if (id == R.id.nav_clients)  fragment = new ClientsFragment();
            else if (id == R.id.nav_sales)    fragment = new SalesFragment();
            else if (id == R.id.nav_users)    fragment = new UsersFragment();

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit();
    }

    private void logout() {
        ApiClient.getInstance(this).getSession().clearSession();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
