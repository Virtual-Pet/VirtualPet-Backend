# Changelog

Todos los cambios notables de este proyecto se documentan en este archivo.
Formato basado en [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

---

## [Unreleased] — 2026-06-01

Refactor del carrito: un único set de endpoints para usuarios autenticados e invitados, con la sesión del carrito invitado manejada por cookie.

### Agregado

- **`CartSessionCookie`** (`com.virtualpet.cart.web`): helper que construye la cookie `CART_SESSION` (HttpOnly) usada para identificar el carrito invitado. La cookie lleva **solo el id de sesión**; el contenido del carrito vive en Redis (`cart:anon:{id}`).
- **Propiedades configurables** `virtualpet.cart.cookie.same-site` (default `Lax`) y `virtualpet.cart.cookie.secure` (default `false`) en `VirtualPetProperties`, para soportar despliegues cross-domain en producción (`SameSite=None; Secure`).
- **`CartService.emptyCart()`**: devuelve un carrito vacío para el caso de `GET /cart` sin usuario ni cookie.

### Modificado

- **`CartController`**: se unificaron los endpoints del carrito. Ahora existe un único set —`GET /cart`, `PUT /cart/items/{skuId}`, `DELETE /cart/items/{skuId}`— que sirve tanto a usuarios autenticados (resueltos por user id) como anónimos (identificados por la cookie `CART_SESSION`). El id de sesión se crea en la primera mutación (`PUT`), se devuelve en `Set-Cookie` y el navegador lo reenvía automáticamente.
- **`AuthController` / `AuthService` / `AuthDTO`**: el merge del carrito anónimo en el login ahora se dispara desde la cookie `CART_SESSION` (se removió `cartSessionId` del body de `LoginRequest`). Tras el merge se elimina el carrito anónimo de Redis y se borra la cookie (`Set-Cookie: CART_SESSION=; Max-Age=0`).
- **`SecurityConfig`**: se reemplazaron los matchers públicos `/api/v1/cart/session/*` por `GET /api/v1/cart` y `PUT`/`DELETE /api/v1/cart/items/*`.
- **`CartService`**: el TTL en Redis ahora usa `virtualpet.cart.ttl-hours` (antes estaba hardcodeado en 24h) para alinearlo con el `Max-Age` de la cookie.
- **`virtualpet-openapi.yaml`**: se eliminaron los paths `/cart/session/*`; se documentó la cookie `CART_SESSION` de forma consistente mediante componentes reutilizables (parámetro `CartSession` y header `SetCartSession`) en `GET`/`PUT`/`DELETE /cart` y `/auth/login`, aclarando que la cookie lleva solo el id de sesión.

### Eliminado

- **Endpoints duplicados del carrito anónimo**: `GET /api/v1/cart/session/{sessionId}` y `PUT`/`DELETE /api/v1/cart/session/{sessionId}/items/{skuId}`. Su funcionalidad queda cubierta por el set unificado de `/cart` + cookie `CART_SESSION`.
- **Campo `cartSessionId`** del record `LoginRequestDTO` y el parámetro `CartSessionId` del OpenAPI (reemplazados por la cookie).

---

## [Unreleased] — 2026-05-29

### Agregado

- **Guest checkout** (`GuestCheckoutController`, `GuestCheckoutService`): nuevo endpoint `POST /api/v1/checkout/guest` que crea una orden sin requerir cuenta de usuario. Recibe datos del invitado (nombre, apellido, email), dirección de envío y líneas del carrito. Retorna `orderId` y `trackingToken` para seguimiento posterior.
- **`ConfirmOrchestrator.placeGuestOrder`**: método que crea la orden, descuenta stock y crea el envío para pedidos de invitados, almacenando `contactName`, `contactLastname`, `contactEmail` y `trackingToken` en la entidad.
- **Migración `V1.7__guest_checkout.sql`**: agrega columnas `contact_name`, `contact_lastname`, `contact_email` y `tracking_token` a la tabla de órdenes para soportar pedidos de invitados.
- **Endpoint público `/api/v1/orders/{id}/track`** (`GET`): permite consultar el estado de un pedido por `trackingToken` sin autenticación.

### Modificado

- **`SecurityConfig`**: se expusieron como públicos los endpoints de sesión de carrito (`GET /api/v1/cart/session/*`, `PUT` y `DELETE` sobre ítems) y `/api/v1/checkout/guest`. Esto permite que usuarios no autenticados operen el carrito y realicen el checkout como invitados.
- **`CartController` y `CartService`**: ajustes para soportar operaciones de carrito sin token de usuario.
- **`OrderEntity`**: agrega campos `contactName`, `contactLastname`, `contactEmail` y `trackingToken`.
- **`CheckoutDTO`**: nuevo record `GuestCheckoutRequestDTO` con `GuestInfoDTO`, `AddressDTO` y líneas del carrito.
- **`OrderDTO`**: actualizado para exponer `trackingToken` en las respuestas.
- **`OrderService` y `OrderController`**: ajustes menores de mapeo.
- **`AuthDTO` y `AuthService`**: limpieza y correcciones menores.
- **`CatalogDTO` y `CatalogService`**: ajustes de serialización.
- **`PaymentService`**: corrección menor.
- **`virtualpet-openapi.yaml`**: spec actualizada con los nuevos endpoints de guest checkout y tracking público.

---

## [1.0.0] — 27/05/2026

**Rama:** `fix/backoffice-orders-and-shipments`

---

## 1. ShipmentDTO.java (`ShipmentSummaryDTO`)

Se agregaron los campos `contactName`, `contactEmail` y `total` al record `ShipmentSummaryDTO` para que la API devuelva los datos del cliente y el monto real del pedido en el listado de envíos del backoffice. Anteriormente estos campos no existían, lo que provocaba que la tabla del backoffice mostrara el cliente vacío y el total en $0.

## 2. ShipmentService.java

- Se inyectó `CustomerRepository` para obtener los datos de perfil (nombre y apellido) de los clientes registrados desde la tabla `auth.customers`.
- Se corrigió un **error de compilación**: se eliminó la llamada a `user.getName()` sobre `UserEntity` (método inexistente). `UserEntity` solo contiene email y datos de autenticación; los datos personales están en `CustomerEntity`.
- Nueva lógica de resolución de nombre del cliente (en orden de prioridad):
  1. `contactName` / `contactLastname` del pedido (datos ingresados en el checkout).
  2. `name` / `lastname` de `CustomerEntity` (perfil del usuario registrado).
  3. `email` de `UserEntity` (fallback para cuentas sin perfil).
  4. `"Invitado"` (para pedidos anónimos).
- Misma lógica de fallback para el email de contacto.

## 3. CatalogController.java

Se ajustaron los mapeos de categorías y productos para asegurar compatibilidad con el formato de DTOs esperados por el catálogo web.

## 4. SecurityConfig.java

Se adaptó el filtro de seguridad para permitir solicitudes de simulación de pago en entornos locales (perfil `!prod`). En producción (AWS con perfil `prod`) estas rutas se desactivan automáticamente.

## 5. GlobalExceptionHandler.java

Se estructuró el formateador de excepciones globales para retornar respuestas de error consistentes en formato JSON.

## 6. Archivos nuevos

- `CategoryController.java`: controlador REST para categorías del catálogo.
- `CategoryRepository.java`: repositorio JPA para la entidad de categorías.
- `CatalogDataSeeder.java`: configuración y datos iniciales del módulo de catálogo.

## 7. CatalogDTO.java (`/products` ahora expone toda la data del producto)

Se ampliaron los records del módulo de catálogo para que el endpoint `/api/v1/products` devuelva todos los atributos del producto y de sus variantes que ya existen en la base de datos. Anteriormente la UI no podía mostrar marca, estado o detalle de stock porque el DTO los recortaba.

- `ProductSummaryDTO`: se agregaron `description`, `brand` y `active`.
- `ProductDTO`: se agregaron `brand`, `active` y `createdAt`.
- `SkuDTO`: se agregaron `sku` (código), `stock`, `stockMin`, `imageUrl`, `active` y `createdAt`. Se mantienen `attributes`, `price` y el derivado `available`.
- `CatalogService.java`: los métodos `toSummary`, `toDetail` y `toSku` ahora poblan los nuevos campos desde las entidades.

## 8. ProductVariantEntity.java (columnas faltantes mapeadas)

Se mapearon columnas que existían en `catalog.product_variants` pero que JPA estaba ignorando, para que el endpoint las pueda leer:

- `stock_min` → `stockMin` (int).
- `active` → `active` (boolean, default `true`).
- `created_at` → `createdAt` (`Instant`, inicializado con `Instant.now()`).
- Se omite intencionalmente `version` (columna de bloqueo optimista) para no alterar el comportamiento de las queries existentes de stock (`decrementStock`/`incrementStock`).

## 9. CartDTO.java (`CartItemDTO` con datos para renderizar la UI)

El item del carrito ya no era usable sin un segundo request a `/products`. Se enriqueció con los datos del producto/variante necesarios para mostrar cada línea:

- Nuevos campos: `sku` (código), `productId`, `productName`, `brand`, `attributes`, `imageUrl`, `available`.
- Se mantienen `skuId`, `quantity`, `unitPrice` y `subtotal`.
- `available` se calcula como `variant.stock >= quantity`, por lo que la UI puede marcar líneas sin stock suficiente.

## 10. CartService.java y ProductVariantRepository.java

- Nuevo método de repositorio `findAllByIdInWithProduct(Collection<UUID>)` con `JOIN FETCH v.product` para evitar N+1 al armar el carrito.
- `CartService.toDto` usa la nueva query y mapea los campos extra (nombre, marca, atributos, imagen) en `CartItemDTO`.
- Se parsea el JSON `attributes` de la variante en el servicio del carrito (helper `parseAttributes`).

## 11. virtualpet-openapi.yaml

Se actualizaron los schemas `ProductSummary`, `Product`, `Sku` y `CartItem` para reflejar los nuevos campos expuestos por el backend.

---

> Todos los cambios son compatibles con el deploy a AWS. El perfil `@Profile("!prod")` protege los endpoints de simulación de pago en producción.
