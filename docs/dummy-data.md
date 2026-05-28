# Datos dummy — usuarios y pedidos de prueba

Resumen de los usuarios sembrados en la base de datos local/dev y los pedidos asociados.
Sirve como guía rápida para loguearse y probar el sistema con distintos roles y estados.

Origen de los datos:
- **DevDataSeeder** (`@Profile("dev")`, corre al arrancar el backend): crea 3 usuarios base.
- **Flyway V1.6** (`src/main/resources/db/migration/V1.6__more_seed_data.sql`): siembra el resto.

> **Password común a todos los usuarios de V1.6**: `Password123!`
> Los 3 usuarios del DevDataSeeder mantienen sus passwords originales (ver tablas).

---

## Admin (ROLE_ADMIN)

| Email                   | Password       | Origen          |
| ----------------------- | -------------- | --------------- |
| `admin@virtualpet.com`  | `admin1234`    | DevDataSeeder   |
| `admin2@virtualpet.com` | `Password123!` | V1.6            |

---

## Empleados (ROLE_EMPLOYEE)

| Email                      | Password       | Nombre                                 | Warehouse     |
| -------------------------- | -------------- | -------------------------------------- | ------------- |
| `staff@virtualpet.com`     | `staff1234`    | Empleado Demo (legajo EMP-0001)        | 1 (MdP)       |
| `operario1@virtualpet.com` | `Password123!` | Carlos Rodríguez (OPR-0001)            | 1 (MdP)       |
| `operario2@virtualpet.com` | `Password123!` | Lucía Fernández (OPR-0002)             | 1 (MdP)       |
| `operario3@virtualpet.com` | `Password123!` | Diego Sosa (OPR-0003)                  | 2 (CABA)      |

Warehouses disponibles:

| id | Nombre                    | Ciudad        |
| -- | ------------------------- | ------------- |
| 1  | Depósito Central MdP      | Mar del Plata |
| 2  | Sucursal CABA Norte       | CABA          |
| 3  | Sucursal Córdoba Capital  | Córdoba       |

---

## Customers (ROLE_CUSTOMER)

Todos con password `Password123!`, excepto `cliente1234@gmail.com` (DevDataSeeder).

| Email                       | Nombre               | Órdenes propias (estado de envío)                       |
| --------------------------- | -------------------- | ------------------------------------------------------- |
| `cliente1234@gmail.com`     | Cliente Demo (pw: `cliente1234`) | — sin órdenes                               |
| `lucia.gomez@example.com`   | Lucía Gómez          | O1 ✅ DELIVERED · O16 ❌ CANCELLED (REFUNDED)            |
| `martin.perez@example.com`  | Martín Pérez         | O2 ✅ DELIVERED                                          |
| `sofia.lopez@example.com`   | Sofía López          | O3 ✅ DELIVERED · O17 ❌ CANCELLED (FAILED)              |
| `juan.romero@example.com`   | Juan Romero          | O4 ✅ DELIVERED (2 intentos)                             |
| `camila.ruiz@example.com`   | Camila Ruiz          | O5 ✅ DELIVERED                                          |
| `nicolas.diaz@example.com`  | Nicolás Díaz         | O6 🚚 IN_TRANSIT · O18 ❌ CANCELLED (FAILED)             |
| `agustina.f@example.com`    | Agustina Fernández   | O7 🚚 IN_TRANSIT                                         |
| `matias.silva@example.com`  | Matías Silva         | O8 🚚 IN_TRANSIT                                         |
| `florencia.b@example.com`   | Florencia Báez       | O9 🚚 IN_TRANSIT                                         |
| `rodrigo.alv@example.com`   | Rodrigo Álvarez      | O10 📦 PREPARED                                          |
| `valentina.m@example.com`   | Valentina Méndez     | O11 📦 PREPARED · O19 ❌ CANCELLED (REFUNDED)            |
| `tomas.castro@example.com`  | Tomás Castro         | O12 📦 PREPARED                                          |
| `bianca.aguero@example.com` | Bianca Agüero        | O13 🟦 CONFIRMED · O20 ❌ CANCELLED (FAILED)             |
| `ezequiel.b@example.com`    | Ezequiel Benítez     | O14 🟦 CONFIRMED                                         |
| `paula.molinas@example.com` | Paula Molinas        | O15 🛑 shipment CANCELLED                                |

Leyenda de estados de envío:

- 🟦 **CONFIRMED**: orden tomada, sin preparar todavía.
- 📦 **PREPARED**: empaquetada en depósito, esperando despacho.
- 🚚 **IN_TRANSIT**: en camino al cliente.
- ✅ **DELIVERED**: entregada.
- 🛑 **CANCELLED**: envío cancelado (la orden puede estar CONFIRMED igualmente).

---

## Recorridos sugeridos para testing

| Caso a probar                        | Login recomendado            |
| ------------------------------------ | ---------------------------- |
| Cliente con historial mixto          | `lucia.gomez@example.com`    |
| Cliente con envío en tránsito        | `nicolas.diaz@example.com`   |
| Cliente con orden CANCELLED reembolsada | `valentina.m@example.com` |
| Empleado con muchas asignaciones (W1)   | `operario1@virtualpet.com` |
| Empleado en otra sucursal (W2 CABA)     | `operario3@virtualpet.com` |
| Empleado de Córdoba (W3)                | `operario3@virtualpet.com` (también atiende W3 vía O4) |
| Admin con visión cross-warehouse        | `admin2@virtualpet.com`    |

---

## Cómo regenerar los datos

Los datos viven en Postgres a través de Flyway. Si necesitás recrearlos desde cero:

```bash
docker compose down -v   # ⚠ destruye el volumen de la DB
docker compose up -d
```

Al arrancar:
1. Flyway aplica `V1.0` → `V1.6` en orden.
2. `DevDataSeeder` (profile `dev`) crea los 3 usuarios base si no existen.

Si sólo aplicaste V1.6 sobre una base que ya tenía datos: Flyway sólo la corre una vez,
y como usa `INSERT` puros (sin `ON CONFLICT`), volver a ejecutarla manualmente fallará
por PKs duplicadas — usá la receta de `down -v` si querés un reset limpio.
