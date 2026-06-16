package com.tony.erp.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Modelos de datos que mapean las respuestas JSON del backend ERP.
 * Son POJOs simples de Java — sin anotaciones complejas.
 */

// =============================================================================
// AuthResponse — respuesta del endpoint de login
// =============================================================================
public class AuthResponse {
    @SerializedName("exito")   public boolean success;
    @SerializedName("mensaje") public String message;
    @SerializedName("token")   public String token;
    @SerializedName("rol")     public String role;
    @SerializedName("usuario") public String username;
}
