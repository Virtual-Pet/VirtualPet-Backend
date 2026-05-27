# Cambios pendientes — VirtualPet-Backend

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

---

> Todos los cambios son compatibles con el deploy a AWS. El perfil `@Profile("!prod")` protege los endpoints de simulación de pago en producción.
