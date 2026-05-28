# [Changelog](https://keepachangelog.com/en/1.1.0/)

**Rama:** `fix/backoffice-orders-and-shipments`
**Fecha:** 27/05/2026

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
