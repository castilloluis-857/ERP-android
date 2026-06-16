package com.tony.erp.ui.sales;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.tabs.TabLayout;
import com.tony.erp.R;
import com.tony.erp.databinding.FragmentSalesBinding;
import com.tony.erp.model.*;
import com.tony.erp.network.ApiClient;
import java.util.*;
import retrofit2.*;

/**
 * Fragment de Ventas con dos pestañas:
 *   - CAJA: seleccionar cliente y productos, ver carrito y finalizar venta.
 *   - HISTORIAL: lista de todas las ventas registradas.
 */
public class SalesFragment extends Fragment {

    private FragmentSalesBinding binding;
    private ApiClient apiClient;

    // Datos del servidor
    private final List<Product> productList = new ArrayList<>();
    private final List<Client>  clientList  = new ArrayList<>();
    private final List<Sale>    saleList    = new ArrayList<>();

    // Estado del carrito
    private final List<SaleItem> cartItems = new ArrayList<>();
    private Client selectedClient = null;

    // Adapters
    private CartAdapter cartAdapter;
    private SaleAdapter saleAdapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSalesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        apiClient = ApiClient.getInstance(requireContext());

        // RecyclerViews
        cartAdapter = new CartAdapter(cartItems, pos -> {
            cartItems.remove(pos);
            cartAdapter.notifyDataSetChanged();
            updateCartTotal();
        });
        saleAdapter = new SaleAdapter(saleList);
        binding.rvCart.setAdapter(cartAdapter);
        binding.rvHistory.setAdapter(saleAdapter);

        // Tabs
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Caja"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Historial"));
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                boolean isCaja = tab.getPosition() == 0;
                binding.layoutCaja.setVisibility(isCaja ? View.VISIBLE : View.GONE);
                binding.layoutHistorial.setVisibility(isCaja ? View.GONE : View.VISIBLE);
                if (!isCaja) loadSales();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Botones de la caja
        binding.btnSelectClient.setOnClickListener(v -> showClientPicker());
        binding.btnAddProduct.setOnClickListener(v -> showProductPicker());
        binding.btnFinalizeSale.setOnClickListener(v -> finalizeSale());

        // Vista inicial
        binding.layoutCaja.setVisibility(View.VISIBLE);
        binding.layoutHistorial.setVisibility(View.GONE);

        loadProductsAndClients();
    }

    // -------------------------------------------------------------------------
    // Carga de datos
    // -------------------------------------------------------------------------

    private void loadProductsAndClients() {
        apiClient.getApi().getAllProducts().enqueue(new Callback<List<Product>>() {
            @Override public void onResponse(Call<List<Product>> c, Response<List<Product>> r) {
                if (r.isSuccessful() && r.body() != null) {
                    productList.clear();
                    productList.addAll(r.body());
                }
            }
            @Override public void onFailure(Call<List<Product>> c, Throwable t) {}
        });

        apiClient.getApi().getAllClients().enqueue(new Callback<List<Client>>() {
            @Override public void onResponse(Call<List<Client>> c, Response<List<Client>> r) {
                if (r.isSuccessful() && r.body() != null) {
                    clientList.clear();
                    clientList.addAll(r.body());
                }
            }
            @Override public void onFailure(Call<List<Client>> c, Throwable t) {}
        });
    }

    private void loadSales() {
        binding.progressBar.setVisibility(View.VISIBLE);
        apiClient.getApi().getSales().enqueue(new Callback<List<Sale>>() {
            @Override public void onResponse(Call<List<Sale>> c, Response<List<Sale>> r) {
                binding.progressBar.setVisibility(View.GONE);
                if (r.isSuccessful() && r.body() != null) {
                    saleList.clear();
                    saleList.addAll(r.body());
                    saleAdapter.notifyDataSetChanged();
                }
            }
            @Override public void onFailure(Call<List<Sale>> c, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    // -------------------------------------------------------------------------
    // Lógica del carrito
    // -------------------------------------------------------------------------

    private void showClientPicker() {
        if (clientList.isEmpty()) {
            Toast.makeText(getContext(), "No hay clientes disponibles", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] names = new String[clientList.size()];
        for (int i = 0; i < clientList.size(); i++) {
            names[i] = clientList.get(i).name + " (" + clientList.get(i).nif + ")";
        }
        new AlertDialog.Builder(getContext())
            .setTitle("Seleccionar cliente")
            .setItems(names, (d, which) -> {
                selectedClient = clientList.get(which);
                binding.tvSelectedClient.setText(selectedClient.name);
            })
            .show();
    }

    private void showProductPicker() {
        // Solo productos con stock disponible
        List<Product> available = new ArrayList<>();
        for (Product p : productList) {
            if (availableStock(p) > 0) available.add(p);
        }

        if (available.isEmpty()) {
            Toast.makeText(getContext(), "No hay productos con stock disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] items = new String[available.size()];
        for (int i = 0; i < available.size(); i++) {
            Product p = available.get(i);
            items[i] = p.name + "  —  " + String.format("%.2f€", p.price)
                + "  (Disp: " + availableStock(p) + ")";
        }

        new AlertDialog.Builder(getContext())
            .setTitle("Añadir al carrito")
            .setItems(items, (d, which) -> addToCart(available.get(which)))
            .show();
    }

    /**
     * Añade 1 unidad al carrito o incrementa si ya estaba.
     * Valida stock disponible restando lo ya en el carrito.
     */
    private void addToCart(Product product) {
        if (availableStock(product) <= 0) {
            Toast.makeText(getContext(), "Sin stock disponible para: " + product.name, Toast.LENGTH_SHORT).show();
            return;
        }

        // Si ya está en el carrito, incrementamos
        for (SaleItem item : cartItems) {
            if (item.product.id == product.id) {
                item.quantity++;
                cartAdapter.notifyDataSetChanged();
                updateCartTotal();
                return;
            }
        }

        // Si no estaba, lo añadimos
        cartItems.add(new SaleItem(product, 1));
        cartAdapter.notifyDataSetChanged();
        updateCartTotal();
    }

    /** Stock real del servidor menos las unidades ya en el carrito. */
    private int availableStock(Product product) {
        int inCart = 0;
        for (SaleItem item : cartItems) {
            if (item.product.id == product.id) { inCart = item.quantity; break; }
        }
        return product.stock - inCart;
    }

    private void updateCartTotal() {
        double total = 0;
        for (SaleItem item : cartItems) total += item.getSubtotal();
        binding.tvTotal.setText(String.format("Total: %.2f €", total));
    }

    // -------------------------------------------------------------------------
    // Finalizar venta
    // -------------------------------------------------------------------------

    private void finalizeSale() {
        if (selectedClient == null) {
            Toast.makeText(getContext(), "Selecciona un cliente primero", Toast.LENGTH_SHORT).show();
            return;
        }
        if (cartItems.isEmpty()) {
            Toast.makeText(getContext(), "El carrito está vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        // Construimos el request con los datos del carrito
        CreateSaleRequest request = new CreateSaleRequest();
        request.client = new CreateSaleRequest.ClientRef(selectedClient.id);
        request.status = "COMPLETED";
        request.items  = new ArrayList<>();
        for (SaleItem item : cartItems) {
            request.items.add(new CreateSaleRequest.SaleItemRequest(
                item.product.id, item.quantity, item.product.price));
        }

        binding.btnFinalizeSale.setEnabled(false);
        binding.btnFinalizeSale.setText("Procesando...");

        apiClient.getApi().createSale(request).enqueue(new Callback<Sale>() {
            @Override public void onResponse(Call<Sale> c, Response<Sale> r) {
                binding.btnFinalizeSale.setEnabled(true);
                binding.btnFinalizeSale.setText("Finalizar venta");

                if (r.isSuccessful()) {
                    Toast.makeText(getContext(), "✅ Venta registrada correctamente", Toast.LENGTH_LONG).show();
                    // Limpiar carrito y recargar productos (stock actualizado)
                    cartItems.clear();
                    cartAdapter.notifyDataSetChanged();
                    updateCartTotal();
                    selectedClient = null;
                    binding.tvSelectedClient.setText("Sin cliente seleccionado");
                    loadProductsAndClients();
                } else {
                    Toast.makeText(getContext(), "Error al procesar la venta: " + r.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<Sale> c, Throwable t) {
                binding.btnFinalizeSale.setEnabled(true);
                binding.btnFinalizeSale.setText("Finalizar venta");
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
