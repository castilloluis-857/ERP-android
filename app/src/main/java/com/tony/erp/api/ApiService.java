package com.tony.erp.api;

import com.tony.erp.model.*;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

/**
 * Interfaz Retrofit — define todos los endpoints del backend ERP.
 * Retrofit genera la implementación automáticamente.
 */
public interface ApiService {

    // -------------------------------------------------------------------------
    // Autenticación — públicos (sin token)
    // -------------------------------------------------------------------------

    @POST("api/usuarios/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("api/usuarios/registro")
    Call<AuthResponse> register(@Body User user);

    // -------------------------------------------------------------------------
    // Usuarios — solo ROLE_ADMIN
    // -------------------------------------------------------------------------

    @GET("api/usuarios")
    Call<List<User>> getUsers();

    @PUT("api/usuarios/admin/{id}")
    Call<AuthResponse> changePassword(@Path("id") long userId, @Body java.util.Map<String, String> body);

    @DELETE("api/usuarios/{id}")
    Call<AuthResponse> deleteUser(@Path("id") long userId);

    // -------------------------------------------------------------------------
    // Productos
    // -------------------------------------------------------------------------

    @GET("api/products")
    Call<List<Product>> getProducts(@Query("search") String search);

    @GET("api/products")
    Call<List<Product>> getAllProducts();

    @POST("api/products")
    Call<Product> createProduct(@Body Product product);

    @PUT("api/products/{id}")
    Call<Product> updateProduct(@Path("id") long id, @Body Product product);

    @DELETE("api/products/{id}")
    Call<ResponseBody> deleteProduct(@Path("id") long id);

    @GET("api/products/export/pdf")
    @Streaming
    Call<ResponseBody> exportProductsPdf();

    @GET("api/products/export/excel")
    @Streaming
    Call<ResponseBody> exportProductsCsv();

    // -------------------------------------------------------------------------
    // Categorías
    // -------------------------------------------------------------------------

    @GET("api/categories")
    Call<List<Category>> getCategories();

    @POST("api/categories")
    Call<Category> createCategory(@Body Category category);

    // -------------------------------------------------------------------------
    // Clientes
    // -------------------------------------------------------------------------

    @GET("api/clients")
    Call<List<Client>> getClients(@Query("search") String search);

    @GET("api/clients")
    Call<List<Client>> getAllClients();

    @POST("api/clients")
    Call<Client> createClient(@Body Client client);

    @PUT("api/clients/{id}")
    Call<Client> updateClient(@Path("id") long id, @Body Client client);

    @DELETE("api/clients/{id}")
    Call<ResponseBody> deleteClient(@Path("id") long id);

    // -------------------------------------------------------------------------
    // Ventas
    // -------------------------------------------------------------------------

    @GET("api/sales")
    Call<List<Sale>> getSales();

    @POST("api/sales")
    Call<Sale> createSale(@Body CreateSaleRequest request);

    @PUT("api/sales/{id}/cancel")
    Call<Sale> cancelSale(@Path("id") long id);

    @GET("api/sales/{id}/pdf")
    @Streaming
    Call<ResponseBody> exportSalePdf(@Path("id") long id);

    @GET("api/sales/{id}/excel")
    @Streaming
    Call<ResponseBody> exportSaleCsv(@Path("id") long id);
}
