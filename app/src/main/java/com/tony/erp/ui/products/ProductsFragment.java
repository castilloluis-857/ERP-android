package com.tony.erp.ui.products;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.tony.erp.R;
import com.tony.erp.databinding.FragmentProductsBinding;
import com.tony.erp.model.Category;
import com.tony.erp.model.Product;
import com.tony.erp.network.ApiClient;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fragment del módulo de Productos.
 *
 * - Lista de productos con búsqueda por nombre (con debounce 500ms).
 * - Botón FAB para añadir nuevo producto.
 * - Click en ítem → dialog de edición/borrado.
 * - Control de acceso: FAB solo visible para ROLE_ADMIN.
 */
public class ProductsFragment extends Fragment {

    private FragmentProductsBinding binding;
    private ApiClient apiClient;
    private ProductAdapter adapter;
    private List<Product> productList = new ArrayList<>();
    private List<Category> categoryList = new ArrayList<>();
    private android.os.Handler searchHandler = new android.os.Handler();
    private Runnable searchRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProductsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        apiClient = ApiClient.getInstance(requireContext());

        // Configurar RecyclerView
        adapter = new ProductAdapter(productList, this::onProductClick);
        binding.recyclerView.setAdapter(adapter);

        // FAB solo visible para admin
        boolean isAdmin = apiClient.getSession().isAdmin();
        binding.fab.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        binding.fab.setOnClickListener(v -> showProductDialog(null));

        // Búsqueda con debounce 500ms — no llama al servidor en cada letra
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchHandler.removeCallbacks(searchRunnable);
                searchRunnable = () -> loadProducts(s.toString());
                searchHandler.postDelayed(searchRunnable, 500);
            }
        });

        loadProducts(null);
        loadCategories();
    }

    private void loadProducts(String search) {
        binding.progressBar.setVisibility(View.VISIBLE);
        Call<List<Product>> call = (search == null || search.isEmpty())
            ? apiClient.getApi().getAllProducts()
            : apiClient.getApi().getProducts(search);

        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> c, Response<List<Product>> r) {
                binding.progressBar.setVisibility(View.GONE);
                if (r.isSuccessful() && r.body() != null) {
                    productList.clear();
                    productList.addAll(r.body());
                    adapter.notifyDataSetChanged();
                    binding.tvEmpty.setVisibility(productList.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }
            @Override
            public void onFailure(Call<List<Product>> c, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCategories() {
        apiClient.getApi().getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> c, Response<List<Category>> r) {
                if (r.isSuccessful() && r.body() != null) {
                    categoryList.clear();
                    categoryList.addAll(r.body());
                }
            }
            @Override public void onFailure(Call<List<Category>> c, Throwable t) {}
        });
    }

    /**
     * Muestra un dialog para crear (product=null) o editar/borrar un producto existente.
     */
    private void showProductDialog(@Nullable Product product) {
        boolean isAdmin = apiClient.getSession().isAdmin();
        if (!isAdmin) return;

        View dialogView = LayoutInflater.from(getContext())
            .inflate(R.layout.dialog_product, null);

        EditText etName  = dialogView.findViewById(R.id.etName);
        EditText etDesc  = dialogView.findViewById(R.id.etDescription);
        EditText etPrice = dialogView.findViewById(R.id.etPrice);
        EditText etStock = dialogView.findViewById(R.id.etStock);
        Spinner spCategory = dialogView.findViewById(R.id.spinnerCategory);

        // Spinner de categorías
        ArrayAdapter<Category> catAdapter = new ArrayAdapter<>(requireContext(),
            android.R.layout.simple_spinner_item, categoryList);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(catAdapter);

        // Si es edición, rellenamos los campos
        if (product != null) {
            etName.setText(product.name);
            etDesc.setText(product.description);
            etPrice.setText(String.valueOf(product.price));
            etStock.setText(String.valueOf(product.stock));
            for (int i = 0; i < categoryList.size(); i++) {
                if (categoryList.get(i).id == product.category.id) {
                    spCategory.setSelection(i); break;
                }
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
            .setTitle(product == null ? "Nuevo producto" : "Editar producto")
            .setView(dialogView)
            .setPositiveButton("Guardar", (dialog, which) -> {
                String name = etName.getText().toString().trim();
                if (name.isEmpty()) { Toast.makeText(getContext(), "El nombre es obligatorio", Toast.LENGTH_SHORT).show(); return; }

                Product p = product != null ? product : new Product();
                p.name = name;
                p.description = etDesc.getText().toString().trim();
                p.price = parseDouble(etPrice.getText().toString());
                p.stock = parseInt(etStock.getText().toString());
                p.active = true;
                if (!categoryList.isEmpty()) p.category = categoryList.get(spCategory.getSelectedItemPosition());

                saveProduct(p, product != null);
            })
            .setNegativeButton("Cancelar", null);

        // Botón de eliminar solo en edición
        if (product != null) {
            builder.setNeutralButton("Desactivar", (dialog, which) -> deleteProduct(product));
        }

        builder.show();
    }

    private void saveProduct(Product product, boolean isEdit) {
        Call<Product> call = isEdit
            ? apiClient.getApi().updateProduct(product.id, product)
            : apiClient.getApi().createProduct(product);

        call.enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> c, Response<Product> r) {
                if (r.isSuccessful()) {
                    Toast.makeText(getContext(), "Producto guardado", Toast.LENGTH_SHORT).show();
                    loadProducts(null);
                } else {
                    Toast.makeText(getContext(), "Error al guardar: " + r.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Product> c, Throwable t) {
                Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteProduct(Product product) {
        new AlertDialog.Builder(getContext())
            .setTitle("Confirmar")
            .setMessage("¿Desactivar el producto '" + product.name + "'?")
            .setPositiveButton("Desactivar", (d, w) ->
                apiClient.getApi().deleteProduct(product.id).enqueue(new Callback<okhttp3.ResponseBody>() {
                    @Override
                    public void onResponse(Call<okhttp3.ResponseBody> c, Response<okhttp3.ResponseBody> r) {
                        Toast.makeText(getContext(), "Producto desactivado", Toast.LENGTH_SHORT).show();
                        loadProducts(null);
                    }
                    @Override public void onFailure(Call<okhttp3.ResponseBody> c, Throwable t) {}
                }))
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void onProductClick(Product product) {
        showProductDialog(product);
    }

    private double parseDouble(String s) { try { return Double.parseDouble(s); } catch (Exception e) { return 0.0; } }
    private int parseInt(String s) { try { return Integer.parseInt(s); } catch (Exception e) { return 0; } }

    @Override
    public void onDestroyView() { super.onDestroyView(); binding = null; }
}
