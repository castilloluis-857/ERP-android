package com.tony.erp.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import com.tony.erp.BuildConfig;
import com.tony.erp.api.ApiService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Singleton que gestiona la sesión cifrada y el cliente Retrofit
 * con detección automática de IP para Emulador o Celular Físico.
 */
public class ApiClient {

    private static ApiClient instance;
    private final ApiService apiService;
    private final SessionManager sessionManager;

    private ApiClient(Context context) {
        this.sessionManager = new SessionManager(context);

        // Interceptor JWT automático
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request.Builder builder = chain.request().newBuilder()
                            .addHeader("Content-Type", "application/json");

                    String token = sessionManager.getToken();
                    if (token != null && !token.isEmpty()) {
                        builder.addHeader("Authorization", "Bearer " + token);
                    }
                    return chain.proceed(builder.build());
                })
                .addInterceptor(new HttpLoggingInterceptor()
                        .setLevel(HttpLoggingInterceptor.Level.BODY))
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        // Construcción de Retrofit con la URL dinámica detectada
        this.apiService = new Retrofit.Builder()
                .baseUrl(getBaseUrl())
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService.class);
    }

    /**
     * Helper que detecta inteligentemente si la app corre en emulador o móvil real.
     */
    private static String getBaseUrl() {
        boolean isEmulator = Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion");

        if (isEmulator) {
            return BuildConfig.BASE_URL_EMULATOR;
        } else {
            return BuildConfig.BASE_URL_PHYSICAL;
        }
    }

    public static synchronized ApiClient getInstance(Context context) {
        if (instance == null) {
            instance = new ApiClient(context.getApplicationContext());
        }
        return instance;
    }

    public ApiService getApi() { return apiService; }
    public SessionManager getSession() { return sessionManager; }

    // =========================================================================
    // SessionManager — token JWT en SharedPreferences cifradas
    // =========================================================================
    public static class SessionManager {

        private static final String PREFS_NAME  = "erp_session";
        private static final String KEY_TOKEN    = "jwt_token";
        private static final String KEY_USERNAME = "username";
        private static final String KEY_ROLE     = "role";

        private final SharedPreferences prefs;

        public SessionManager(Context context) {
            SharedPreferences p;
            try {
                MasterKey masterKey = new MasterKey.Builder(context)
                        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                        .build();
                p = EncryptedSharedPreferences.create(
                        context, PREFS_NAME, masterKey,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                );
            } catch (Exception e) {
                p = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            }
            this.prefs = p;
        }

        public void saveSession(String token, String username, String role) {
            prefs.edit()
                    .putString(KEY_TOKEN, token)
                    .putString(KEY_USERNAME, username)
                    .putString(KEY_ROLE, role)
                    .apply();
        }

        public void clearSession() {
            prefs.edit().clear().apply();
        }

        public String getToken()    { return prefs.getString(KEY_TOKEN, null); }
        public String getUsername() { return prefs.getString(KEY_USERNAME, null); }
        public String getRole()     { return prefs.getString(KEY_ROLE, null); }
        public boolean isLoggedIn() { return getToken() != null && !getToken().isEmpty(); }
        public boolean isAdmin()    { return "ROLE_ADMIN".equals(getRole()); }
    }
}