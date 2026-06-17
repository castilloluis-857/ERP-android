<div align="center">

# 📱 ERP Tony — Android

**Aplicación móvil nativa del sistema ERP corporativo**

[![Android](https://img.shields.io/badge/Android-26+-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Retrofit](https://img.shields.io/badge/Retrofit-2.11.0-48B983?style=for-the-badge)](https://square.github.io/retrofit/)
[![Material](https://img.shields.io/badge/Material_Design-3-757575?style=for-the-badge&logo=material-design&logoColor=white)](https://m3.material.io/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](LICENSE)

*Accede a tu ERP desde cualquier lugar — mismo backend, mismos datos, cualquier dispositivo.*

[📸 Capturas de pantalla](#-capturas-de-pantalla) · [🚀 Instalación](#-instalación) · [🐛 Reportar bug](../../issues)

</div>

---

## 📋 Tabla de contenidos

- [Sobre el proyecto](#-sobre-el-proyecto)
- [Capturas de pantalla](#-capturas-de-pantalla)
- [Tecnologías](#-tecnologías)
- [Arquitectura](#-arquitectura)
- [Instalación](#-instalación)
- [Configuración de red](#-configuración-de-red)
- [Módulos](#-módulos)
- [Seguridad y sesión](#-seguridad-y-sesión)
- [Comunicación con el backend](#-comunicación-con-el-backend)
- [Estructura del proyecto](#-estructura-del-proyecto)
- [Roadmap](#-roadmap)
- [Contribuir](#-contribuir)

---

## 🏗 Sobre el proyecto

Esta app es el cliente móvil del [ERP Tony Backend](https://github.com/tu-usuario/erp-backend). Comparte exactamente los mismos datos y lógica de negocio que el [cliente de escritorio JavaFX](https://github.com/tu-usuario/erp-frontend) — cualquier venta, producto o cliente creado desde el móvil aparece inmediatamente en el escritorio, y viceversa.

Desarrollada con **Android nativo en Java** y **XML clásico**, sin frameworks adicionales como Jetpack Compose, para mantener la base de código lo más familiar y directa posible.

### Funcionalidades principales

- 🔐 **Login seguro** con JWT y sesión persistente en SharedPreferences cifradas
- 📦 **Gestión de productos** — búsqueda, alta, edición y borrado lógico
- 👥 **Gestión de clientes** — búsqueda por nombre y NIF, CRUD completo
- 🧾 **Punto de venta** — carrito con validación de stock en tiempo real
- 📊 **Historial de ventas** — registro completo con estado y totales
- 👤 **Administración de usuarios** — cambio de contraseña y eliminación (solo ADMIN)
- 🛡 **Control de acceso por rol** — la UI se adapta automáticamente según ADMIN o EMPLOYEE

---

## 📸 Capturas de pantalla

> 💡 **Cómo añadir capturas:** ejecuta la app en el emulador, haz capturas de cada pantalla y guárdalas en `docs/screenshots/`. Luego sustituye las rutas de abajo.

### Login

![Login](docs/screenshots/login.png)

*Pantalla de autenticación con validación local y feedback de error del servidor.*

---

### Dashboard — Módulo Productos

![Productos](docs/screenshots/products.png)

*Lista de productos con búsqueda en tiempo real (debounce 500ms). Botón + solo visible para ADMIN.*

---

### Dialog de alta/edición de producto

![Dialog Producto](docs/screenshots/product_dialog.png)

*AlertDialog con formulario de campos, Spinner de categorías y botón "Desactivar" en modo edición.*

---

### Módulo Clientes

![Clientes](docs/screenshots/clients.png)

*Búsqueda por nombre o NIF. El botón "Desactivar" solo aparece para ROLE_ADMIN.*

---

### Módulo Ventas — Caja

![Caja](docs/screenshots/sales_cart.png)

*Selector de cliente, carrito de productos con subtotales y total calculado automáticamente.*

---

### Módulo Ventas — Historial

![Historial](docs/screenshots/sales_history.png)

*Registro de todas las ventas con estado (COMPLETED / CANCELLED) y total de cada una.*

---

### Módulo Usuarios *(solo ROLE_ADMIN)*

![Usuarios](docs/screenshots/users.png)

*Lista de usuarios con rol. Al pulsar → dialog para cambiar contraseña o eliminar.*

---

> 🎥 **Demo en vídeo:** [Ver demostración completa](https://youtube.com/tu-video)

---

## 🛠 Tecnologías

| Tecnología | Versión | Rol |
|---|---|---|
| **Java** | 17 | Lenguaje principal |
| **Android SDK** | API 26+ | Plataforma objetivo |
| **Material Components** | 1.12.0 | UI components (Cards, FAB, Dialogs, BottomNav) |
| **Retrofit 2** | 2.11.0 | Cliente HTTP REST |
| **OkHttp 3** | 4.12.0 | Capa de red con interceptor JWT |
| **Gson** | 2.11.0 | Serialización/deserialización JSON |
| **EncryptedSharedPreferences** | 1.1.0 | Almacenamiento seguro del token JWT |
| **RecyclerView** | 1.3.2 | Listas de alta performance |
| **ConstraintLayout** | 2.1.4 | Layouts responsivos |
| **ViewBinding** | — | Acceso a vistas sin findViewById() |
| **Navigation Component** | 2.7.7 | Navegación entre Fragments |

---

## 🏛 Arquitectura

La app sigue el patrón **MVC clásico de Android**:

```
┌─────────────────────────────────────────────────────────┐
│                     Vista (XML + Java)                   │
│   LoginActivity  │  DashboardActivity                   │
│                  │       │                              │
│              ┌───┴───────┴───────────┐                  │
│         ProductsFragment  ClientsFragment               │
│         SalesFragment     UsersFragment                 │
└─────────────────────────────────────────────────────────┘
                        │ llama a
┌─────────────────────────────────────────────────────────┐
│                    Controlador (ApiClient)               │
│   SessionManager  │  OkHttp Interceptor JWT             │
│   Retrofit API    │  Callbacks onResponse/onFailure     │
└─────────────────────────────────────────────────────────┘
                        │ petición HTTP
┌─────────────────────────────────────────────────────────┐
│               Backend Spring Boot                        │
│              http://10.0.2.2:8080                       │
└─────────────────────────────────────────────────────────┘
```

### Flujo de una petición

```
Usuario pulsa botón
        │
        ▼
Fragment valida campos
        │
        ▼
ApiClient.getApi().<método>()
        │  Retrofit construye la petición HTTP
        │  OkHttp añade "Authorization: Bearer <token>"
        ▼
Backend Spring Boot procesa y responde
        │
        ▼
Callback onResponse() → actualiza la UI en el hilo principal
Callback onFailure()  → muestra Toast de error de red
```

### Navegación entre pantallas

```
LoginActivity
    │  login correcto → finish() (no se puede volver con Atrás)
    ▼
DashboardActivity  ← BottomNavigationView
    │
    ├── ProductsFragment  → AlertDialog (alta/edición)
    ├── ClientsFragment   → AlertDialog (alta/edición)
    ├── SalesFragment     → Tab Caja | Tab Historial
    └── UsersFragment     → AlertDialog (cambiar contraseña / eliminar)
                                      [solo ROLE_ADMIN]
```

---

## 🚀 Instalación

### Prerrequisitos

- [Android Studio Hedgehog](https://developer.android.com/studio) o superior
- JDK 17+
- **Backend ERP** en ejecución — [ver instrucciones del backend](https://github.com/tu-usuario/erp-backend)
- Dispositivo Android (API 26+) o emulador configurado en Android Studio

### Pasos

**1. Clona el repositorio**

```bash
git clone https://github.com/tu-usuario/erp-android.git
cd erp-android
```

**2. Abre el proyecto en Android Studio**

```
File → Open → selecciona la carpeta raíz (la que contiene settings.gradle)
```

Espera a que Gradle sincronice las dependencias. La primera vez puede tardar 2-5 minutos.

**3. Configura la URL del backend** *(ver sección siguiente)*

**4. Ejecuta la app**

```
Run → Run 'app'  (o pulsa ▶️)
```

---

## 🌐 Configuración de red

La URL del backend se configura en `app/build.gradle` mediante `buildConfigField`:

```groovy
buildTypes {
    debug {
        buildConfigField "String", "BASE_URL", "\"http://10.0.2.2:8080/\""
    }
}
```

### ¿Qué IP usar?

| Dónde ejecutas la app | URL del backend |
|---|---|
| **Emulador** de Android Studio | `http://10.0.2.2:8080/` ✅ (ya configurado) |
| **Dispositivo físico** por USB/WiFi | `http://TU_IP_LOCAL:8080/` ❌ debes cambiarla |

Para saber tu IP local:
- **Windows:** abre CMD → escribe `ipconfig` → busca "Dirección IPv4"
- **Mac/Linux:** abre Terminal → escribe `ifconfig` → busca `inet`

Después de cambiar la IP en `build.gradle`, también añádela en `res/xml/network_security_config.xml`:

```xml
<domain-config cleartextTrafficPermitted="true">
    <domain includeSubdomains="false">192.168.1.X</domain>  ← tu IP
</domain-config>
```

### El backend debe escuchar en todas las interfaces

Asegúrate de que `application.properties` del backend tiene:

```properties
server.address=0.0.0.0
server.port=8080
```

---

## 📱 Módulos

### 🔐 Login (`LoginActivity`)

- Validación local de campos vacíos antes de llamar al servidor
- Petición `POST /api/usuarios/login` con Retrofit
- Si el login es correcto: guarda `token`, `username` y `role` en `EncryptedSharedPreferences`
- Navega a `DashboardActivity` y hace `finish()` — el usuario no puede volver al Login con "Atrás"
- Si ya hay sesión activa al abrir la app, salta directamente al Dashboard

---

### 🏠 Dashboard (`DashboardActivity`)

- `BottomNavigationView` con 4 tabs: Productos, Clientes, Ventas, Usuarios
- El tab **Usuarios** solo aparece si el rol es `ROLE_ADMIN`
- Toolbar con nombre de usuario y rol
- Botón de logout en la toolbar → limpia la sesión y vuelve al Login

---

### 📦 Productos (`ProductsFragment` + `ProductAdapter`)

| Funcionalidad | Implementación |
|---|---|
| Lista de productos | `RecyclerView` + `ProductAdapter` |
| Búsqueda | `TextWatcher` con debounce de 500ms → `GET /api/products?search=` |
| Color del stock | Verde (>5), Naranja (1-5), Rojo (0) |
| Alta de producto | FAB → `AlertDialog` con formulario + `Spinner` de categorías |
| Edición | Click en ítem → mismo `AlertDialog` con datos precargados |
| Borrado lógico | Botón "Desactivar" en el dialog → `DELETE /api/products/{id}` |
| Acceso | FAB y acciones de escritura **solo visibles para ROLE_ADMIN** |

---

### 👥 Clientes (`ClientsFragment` + `ClientAdapter`)

| Funcionalidad | Implementación |
|---|---|
| Lista de clientes | `RecyclerView` + `ClientAdapter` |
| Búsqueda | Debounce 500ms por nombre o NIF → `GET /api/clients?search=` |
| Alta | FAB → `AlertDialog` con campos NIF, nombre, email y teléfono |
| Edición | Click en ítem → dialog precargado → `PUT /api/clients/{id}` |
| Borrado lógico | Solo `ROLE_ADMIN` → `DELETE /api/clients/{id}` |
| NIF | Se normaliza automáticamente a mayúsculas antes de enviar |

---

### 🧾 Ventas (`SalesFragment`)

El módulo tiene **dos pestañas** dentro del mismo Fragment:

**Pestaña Caja:**
- Selector de cliente mediante `AlertDialog` con lista de clientes activos
- Selector de producto: muestra nombre, precio y stock disponible (descontando lo ya en el carrito)
- Si la cantidad en el carrito iguala el stock real, el producto no aparece en el selector
- Carrito en `RecyclerView` con `CartAdapter` — botón ✕ para quitar ítems
- Total calculado en tiempo real al añadir/quitar productos
- Botón "Finalizar venta" → `POST /api/sales` → stock descontado automáticamente en el servidor

**Pestaña Historial:**
- Lista todas las ventas en `RecyclerView` con `SaleAdapter`
- Muestra: número de venta, cliente, fecha, total y estado (COMPLETED / CANCELLED)
- El estado se colorea en verde (COMPLETED) o rojo (CANCELLED)

---

### 👤 Usuarios (`UsersFragment` + `UserAdapter`) — Solo ROLE_ADMIN

| Funcionalidad | Implementación |
|---|---|
| Lista de usuarios | `RecyclerView` + `UserAdapter` con rol coloreado |
| Cambiar contraseña | Click → `AlertDialog` con campo de nueva contraseña y confirmación |
| Eliminar usuario | Botón "Eliminar" en el dialog, con confirmación previa |
| Protección propia | El usuario actualmente en sesión **no puede eliminarse a sí mismo** |
| Validación | Contraseña mínimo 4 caracteres; los dos campos deben coincidir |

---

## 🔐 Seguridad y sesión

### Token JWT

El token JWT recibido del backend se almacena en **EncryptedSharedPreferences** (cifrado AES-256-GCM):

```java
// Guardar sesión tras login correcto
sessionManager.saveSession(token, username, role);

// Leer en cualquier parte de la app
String token = sessionManager.getToken();
boolean isAdmin = sessionManager.isAdmin();

// Limpiar al hacer logout
sessionManager.clearSession();
```

A diferencia del cliente JavaFX (que guarda el token solo en memoria), en Android lo persistimos para que la sesión sobreviva a que el sistema mate el proceso en segundo plano.

### Interceptor JWT

Cada petición HTTP añade automáticamente la cabecera de autorización sin que los Fragments tengan que preocuparse de ello:

```java
// En ApiClient — se aplica a TODAS las peticiones
OkHttpClient client = new OkHttpClient.Builder()
    .addInterceptor(chain -> {
        Request request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer " + sessionManager.getToken())
            .build();
        return chain.proceed(request);
    })
    .build();
```

### Control de acceso visual

La UI se adapta al rol en `onCreate()` / `onViewCreated()` de cada Activity y Fragment:

```java
// DashboardActivity — oculta el tab de Usuarios para EMPLOYEE
binding.bottomNav.getMenu()
    .findItem(R.id.nav_users)
    .setVisible(session.isAdmin());

// ProductsFragment — oculta el FAB para EMPLOYEE
binding.fab.setVisibility(
    apiClient.getSession().isAdmin() ? View.VISIBLE : View.GONE
);
```

---

## 🔗 Comunicación con el backend

Todos los endpoints están definidos en `ApiService.java` como interfaz Retrofit:

```java
// Ejemplo — cargar productos con búsqueda opcional
@GET("api/products")
Call<List<Product>> getProducts(@Query("search") String search);

// Ejemplo — crear una venta
@POST("api/sales")
Call<Sale> createSale(@Body CreateSaleRequest request);
```

Las llamadas se hacen siempre de forma **asíncrona** con `.enqueue()`, nunca bloqueando el hilo principal:

```java
apiClient.getApi().getAllProducts().enqueue(new Callback<List<Product>>() {
    @Override
    public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
        // Se ejecuta en el hilo principal → actualizar UI directamente
        if (response.isSuccessful() && response.body() != null) {
            productList.clear();
            productList.addAll(response.body());
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onFailure(Call<List<Product>> call, Throwable t) {
        // Error de red → mostrar mensaje al usuario
        Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
    }
});
```

---

## 📁 Estructura del proyecto

```
app/src/main/
│
├── java/com/tony/erp/
│   │
│   ├── 📁 api/
│   │   └── ApiService.java           # Interfaz Retrofit con todos los endpoints
│   │
│   ├── 📁 model/                     # POJOs que mapean el JSON del backend
│   │   ├── AuthResponse.java
│   │   ├── User.java + Role.java
│   │   ├── Product.java + Category.java
│   │   ├── Client.java
│   │   ├── Sale.java + SaleItem.java
│   │   ├── LoginRequest.java
│   │   └── CreateSaleRequest.java    # Payload para POST /api/sales
│   │
│   ├── 📁 network/
│   │   └── ApiClient.java            # Singleton: Retrofit + OkHttp + SessionManager
│   │
│   └── 📁 ui/
│       ├── login/
│       │   └── LoginActivity.java
│       ├── dashboard/
│       │   └── DashboardActivity.java
│       ├── products/
│       │   ├── ProductsFragment.java
│       │   └── ProductAdapter.java
│       ├── clients/
│       │   ├── ClientsFragment.java
│       │   └── ClientAdapter.java
│       ├── sales/
│       │   ├── SalesFragment.java    # Caja + Historial en TabLayout
│       │   ├── CartAdapter.java      # Carrito de la venta actual
│       │   └── SaleAdapter.java      # Historial de ventas
│       └── users/
│           ├── UsersFragment.java
│           └── UserAdapter.java
│
└── res/
    ├── layout/
    │   ├── activity_login.xml
    │   ├── activity_dashboard.xml
    │   ├── fragment_products.xml
    │   ├── fragment_clients.xml
    │   ├── fragment_sales.xml        # TabLayout con dos secciones
    │   ├── fragment_users.xml
    │   ├── item_product.xml          # Tarjeta de producto en la lista
    │   ├── item_client.xml
    │   ├── item_cart.xml             # Ítem del carrito con botón ✕
    │   ├── item_sale.xml             # Fila del historial de ventas
    │   ├── item_user.xml
    │   ├── dialog_product.xml        # Formulario de alta/edición de producto
    │   ├── dialog_client.xml
    │   └── dialog_change_password.xml
    ├── menu/
    │   ├── menu_bottom_nav.xml       # Tabs de la BottomNavigationView
    │   └── menu_dashboard.xml        # Menú de la toolbar (logout)
    ├── values/
    │   ├── colors.xml
    │   ├── strings.xml
    │   └── themes.xml
    └── xml/
        └── network_security_config.xml  # Permite HTTP local para desarrollo
```

---

## 🗺 Roadmap

- [x] Autenticación JWT con sesión persistente cifrada
- [x] CRUD de productos con búsqueda y borrado lógico
- [x] CRUD de clientes con búsqueda por nombre y NIF
- [x] Punto de venta con carrito y validación de stock en tiempo real
- [x] Historial de ventas con estado y totales
- [x] Gestión de usuarios (solo ADMIN)
- [x] Control de acceso visual por rol
- [x] Búsqueda con debounce para optimizar llamadas al servidor
- [ ] Exportación de facturas a PDF desde el móvil
- [ ] Paginación en listas con muchos registros
- [ ] Modo offline con caché local (Room Database)
- [ ] Notificaciones push para alertas de stock bajo
- [ ] Modo oscuro

---

## 🤝 Contribuir

1. Haz un fork del proyecto
2. Crea tu rama: `git checkout -b feature/nueva-funcionalidad`
3. Commitea tus cambios: `git commit -m 'feat: añadir nueva funcionalidad'`
4. Push a la rama: `git push origin feature/nueva-funcionalidad`
5. Abre un Pull Request

---

## 📄 Licencia

Distribuido bajo la licencia MIT. Consulta el archivo `LICENSE` para más información.

---

<div align="center">

Desarrollado por **Tony** · [GitHub](https://github.com/tu-usuario)

⭐ Si este proyecto te ha sido útil, considera darle una estrella

**Parte del ecosistema ERP Tony:**
[Backend Spring Boot](https://github.com/tu-usuario/erp-backend) ·
[Frontend JavaFX](https://github.com/tu-usuario/erp-frontend) ·
**Android** ← estás aquí

</div>
