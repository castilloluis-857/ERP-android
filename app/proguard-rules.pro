# Retrofit + OkHttp
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclassmembernames interface * { @retrofit2.http.* <methods>; }
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**

# Modelos de datos (Gson necesita los nombres de los campos para el JSON)
-keep class com.tony.erp.model.** { *; }

# Gson
-keep class com.google.gson.** { *; }
-keepattributes EnclosingMethod
