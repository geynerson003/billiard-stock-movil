# Arquitectura

## Vision general

El proyecto sigue una arquitectura practica orientada a capas:

- `ui`: pantallas Compose, componentes visuales y `ViewModel`
- `data/model`: modelos de dominio y DTOs usados en Firestore
- `data/repository`: acceso a datos y reglas de persistencia
- `data/service`: logica de negocio transversal
- `data/di` y `di`: configuracion de dependencias
- `util` y `utils`: utilidades compartidas

La fuente principal de datos es Firebase Auth + Cloud Firestore.

## Flujo principal

```text
Pantalla Compose
    -> ViewModel
        -> Repository / Service
            -> Firebase Auth / Firestore
```

## Componentes principales

### Arranque de la app

- `InventarioBillarApp.kt`
  - inicializa Hilt
  - configura `StrictMode`
  - inicializa Firebase

- `MainActivity.kt`
  - monta la UI Compose
  - controla navegacion principal
  - define el shell general de la aplicacion

### Capa de datos

#### Modelos

Los modelos principales estan en `data/model`:

- `User`: usuario autenticado
- `Client`: cliente y datos de deuda
- `Product`: inventario
- `Sale` y `SaleItem`: ventas
- `Payment`: pagos de deuda
- `Expense`: gastos
- `Table` y `TableSession`: mesas y sesiones
- `Game`, `GameParticipant`, `GameBet`: partidas
- `DashboardSummary`, `ChartData`, `ReportFilter`, `ReportResult`: reporting

#### Repositorios

Los repositorios abstraen la persistencia:

- `AuthRepository`: autenticacion y estado de sesion
- `InventoryRepository`: productos
- `SalesRepository`: ventas
- `ClientRepository`: clientes
- `PaymentRepository`: pagos
- `ExpensesRepository`: gastos
- `TablesRepository`: mesas y sesiones
- `GamesRepository`: partidas
- `ReportsRepository`: agregacion de datos para reportes

#### Servicios

- `DebtCalculationService`
  - calcula deuda por cliente
  - registra pagos
  - decide como actualizar ventas pendientes

## Inyeccion de dependencias

- `RepositoryModule.kt`
  - enlaza interfaces con implementaciones

- `FirebaseModule.kt`
  - provee `FirebaseAuth`
  - provee `FirebaseFirestore`

## Navegacion principal

La navegacion esta centralizada en `MainActivity.kt`.

Rutas principales:

- `dashboard`
- `inventory`
- `sales`
- `tables`
- `expenses`
- `reports`
- `clients`

Tambien existen rutas de detalle y formularios:

- detalle y formulario de productos
- detalle de ventas
- formulario de gastos
- formulario de clientes
- detalle de deuda por cliente
- flujo de juego por mesa y sesion

## Modulos funcionales

### Auth

Pantallas:

- `LoginScreen`
- `RegisterScreen`
- `ForgotPasswordScreen`

Estado:

- `AuthViewModel`

### Inventory

Pantallas:

- `InventoryListScreen`
- `ProductFormScreen`
- `DeleteProductDialog`

Estado:

- `InventoryViewModel`

### Sales

Pantallas:

- `SalesListScreen`
- `SaleFormScreen`
- `SaleDetailScreen`
- `DateFilterSelector`

Estado:

- `SalesViewModel`

### Tables and Games

Pantallas:

- `TablesListScreen`
- `TableFormScreen`
- `TableSessionScreen`
- `GameScreen`
- `GameHistoryScreen`
- `TableSummaryScreen`

Estado:

- `TablesViewModel`
- `GameViewModel`

### Clients and Debt

Pantallas:

- `ClientsListScreen`
- `ClientFormScreen`
- `ClientDebtScreen`

Estado:

- `ClientViewModel`
- `ClientDebtViewModel`

### Expenses

Pantallas:

- `ExpensesListScreen`
- `ExpenseFormScreen`
- `DeleteExpenseDialog`

Estado:

- `ExpensesViewModel`

### Dashboard and Reports

Pantallas:

- `DashboardScreen`
- `ProfitChart`
- `ChartFilterSelector`
- `ReportsScreen`
- `ReportDetailScreen`

Estado:

- `DashboardViewModel`
- `ReportsViewModel`

## Persistencia y esquema logico

Colecciones principales en Firestore:

- `/users/{userId}`
- `/businesses/{userId}`
- `/businesses/{userId}/products/{productId}`
- `/businesses/{userId}/sales/{saleId}`
- `/businesses/{userId}/clients/{clientId}`
- `/businesses/{userId}/tables/{tableId}`
- `/businesses/{userId}/table_sessions/{sessionId}`
- `/businesses/{userId}/games/{gameId}`
- `/businesses/{userId}/expenses/{expenseId}`
- `/businesses/{userId}/payments/{paymentId}`

## Observaciones de arquitectura

- Firestore es la fuente de verdad principal.
- `Room`, `DataStore` y `Firebase Messaging` estan declarados como dependencias, pero no forman parte del flujo principal visible en la implementacion actual.
- Parte de la logica financiera sigue distribuida entre `MathUtils`, `DashboardViewModel`, `ReportsRepositoryImpl` y `DebtCalculationService`.

## Recomendaciones de evolucion

- centralizar la logica financiera en un servicio unico
- fortalecer operaciones atomicas relacionadas con deuda e inventario
- mover filtrados pesados al servidor cuando el volumen de datos crezca
- desacoplar mas la navegacion y el shell principal de `MainActivity.kt`
