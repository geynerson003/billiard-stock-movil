# Mapa del codigo

## Objetivo

Este documento sirve como guia rapida para entender donde vive cada responsabilidad del proyecto.

## Raiz del repositorio

| Ruta | Proposito |
|---|---|
| `build.gradle.kts` | Plugins del proyecto raiz |
| `settings.gradle.kts` | Nombre del proyecto y modulos |
| `app/build.gradle.kts` | Configuracion Android y dependencias |
| `firestore.rules` | Reglas de seguridad de Firestore |
| `.releaserc.json` | Configuracion de `semantic-release` |
| `.github/workflows/release.yml` | Pipeline de release en GitHub Actions |

## Entrada de la app

| Archivo | Responsabilidad |
|---|---|
| `InventarioBillarApp.kt` | Inicializacion de app, StrictMode y Firebase |
| `MainActivity.kt` | Shell principal, navegacion y rutas |

## Inyeccion de dependencias

| Archivo | Responsabilidad |
|---|---|
| `di/RepositoryModule.kt` | Binds de interfaces a implementaciones |
| `data/di/FirebaseModule.kt` | Provision de FirebaseAuth y FirebaseFirestore |

## Modelos de datos

| Archivo | Responsabilidad |
|---|---|
| `data/model/User.kt` | Usuario autenticado |
| `data/model/AuthState.kt` | Estado de autenticacion |
| `data/model/Client.kt` | Cliente y deuda |
| `data/model/Product.kt` | Producto de inventario |
| `data/model/Sale.kt` | Venta y sus items |
| `data/model/Payment.kt` | Pago de deuda |
| `data/model/Expense.kt` | Gasto |
| `data/model/Table.kt` | Mesa |
| `data/model/TableSession.kt` | Sesion de mesa |
| `data/model/Game.kt` | Partida, participantes y apuestas |
| `data/model/DashboardSummary.kt` | Resumen del dashboard |
| `data/model/ChartData.kt` | Datos de graficas |
| `data/model/ReportFilter.kt` | Filtros de reportes |
| `data/model/ReportResult.kt` | Resultado agregado de reportes |
| `data/model/ReportType.kt` | Tipos de reporte |

## Repositorios

| Archivo | Responsabilidad |
|---|---|
| `data/repository/AuthRepository*.kt` | Login, registro, logout, reset password |
| `data/repository/InventoryRepository*.kt` | CRUD de productos |
| `data/repository/SalesRepository*.kt` | Lectura, creacion, borrado y pago de ventas |
| `data/repository/ClientRepository*.kt` | CRUD de clientes |
| `data/repository/PaymentRepository*.kt` | CRUD y observacion de pagos |
| `data/repository/ExpensesRepository*.kt` | CRUD y observacion de gastos |
| `data/repository/TablesRepository*.kt` | CRUD de mesas y sesiones |
| `data/repository/GamesRepository*.kt` | Partidas, participantes, apuestas y cierre |
| `data/repository/ReportsRepository*.kt` | Agregacion de datos para reportes |

## Servicios

| Archivo | Responsabilidad |
|---|---|
| `data/service/DebtCalculationService.kt` | Logica de deuda, pagos y estado de ventas pendientes |

## Utilidades

| Archivo | Responsabilidad |
|---|---|
| `util/MathUtils.kt` | Calculos y formato financiero |
| `util/PerformanceConfig.kt` | Ajustes de rendimiento para arranque |
| `utils/EmailValidator.kt` | Validacion de email |
| `utils/PasswordValidator.kt` | Validacion y fuerza de contrasena |

## UI por feature

### Auth

| Archivo | Responsabilidad |
|---|---|
| `ui/feature/auth/AuthViewModel.kt` | Estado de autenticacion y validaciones |
| `ui/feature/auth/LoginScreen.kt` | Inicio de sesion |
| `ui/feature/auth/RegisterScreen.kt` | Registro |
| `ui/feature/auth/ForgotPasswordScreen.kt` | Recuperacion de contrasena |
| `ui/feature/auth/components/*` | Componentes reutilizables del flujo auth |

### Main

| Archivo | Responsabilidad |
|---|---|
| `ui/feature/main/MainViewModel.kt` | Preparacion inicial de la app |

### Dashboard

| Archivo | Responsabilidad |
|---|---|
| `ui/feature/dashboard/DashboardViewModel.kt` | Carga de resumen, chart y top products |
| `ui/feature/dashboard/DashboardScreen.kt` | Vista principal del dashboard |
| `ui/feature/dashboard/ProfitChart.kt` | Grafica de resultados |
| `ui/feature/dashboard/ChartFilterSelector.kt` | Selector de filtro del chart |

### Inventory

| Archivo | Responsabilidad |
|---|---|
| `ui/feature/inventory/InventoryViewModel.kt` | Estado de inventario |
| `ui/feature/inventory/InventoryListScreen.kt` | Lista de productos |
| `ui/feature/inventory/ProductFormScreen.kt` | Alta y edicion de productos |
| `ui/feature/inventory/DeleteProductDialog.kt` | Confirmacion de borrado |

### Sales

| Archivo | Responsabilidad |
|---|---|
| `ui/feature/sales/SalesViewModel.kt` | Carga y filtros de ventas |
| `ui/feature/sales/SalesListScreen.kt` | Lista principal de ventas |
| `ui/feature/sales/SaleFormScreen.kt` | Formulario de venta |
| `ui/feature/sales/SaleDetailScreen.kt` | Detalle de venta |
| `ui/feature/sales/DateFilterSelector.kt` | Selector avanzado de rango de fechas |

### Tables and Games

| Archivo | Responsabilidad |
|---|---|
| `ui/feature/tables/TablesViewModel.kt` | Estado de mesas |
| `ui/feature/tables/TablesListScreen.kt` | Lista de mesas |
| `ui/feature/tables/TableFormScreen.kt` | Formulario de mesa |
| `ui/feature/tables/GameViewModel.kt` | Estado de partida y logica de cierre |
| `ui/feature/tables/GameScreen.kt` | Flujo de partida |
| `ui/feature/tables/GameHistoryScreen.kt` | Historial de partidas |
| `ui/feature/tables/TableSessionScreen.kt` | Sesion de mesa |
| `ui/feature/tables/TableSummaryScreen.kt` | Resumen de mesa |

### Clients

| Archivo | Responsabilidad |
|---|---|
| `ui/feature/clients/ClientViewModel.kt` | Lista de clientes, busqueda y deuda resumida |
| `ui/feature/clients/ClientsListScreen.kt` | Lista de clientes |
| `ui/feature/clients/ClientFormScreen.kt` | Alta y edicion de cliente |
| `ui/feature/clients/ClientDebtViewModel.kt` | Detalle de deuda y pagos |
| `ui/feature/clients/ClientDebtScreen.kt` | Pantalla de deuda de un cliente |

### Expenses

| Archivo | Responsabilidad |
|---|---|
| `ui/feature/expenses/ExpensesViewModel.kt` | Estado de gastos |
| `ui/feature/expenses/ExpensesListScreen.kt` | Lista de gastos |
| `ui/feature/expenses/ExpenseFormScreen.kt` | Alta y edicion de gastos |
| `ui/feature/expenses/DeleteExpenseDialog.kt` | Confirmacion de borrado |

### Reports

| Archivo | Responsabilidad |
|---|---|
| `ui/feature/reports/ReportsViewModel.kt` | Filtro y consumo de reportes |
| `ui/feature/reports/ReportsScreen.kt` | Vista principal de reportes |
| `ui/feature/reports/ReportDetailScreen.kt` | Vista de detalle del reporte |

## Tema visual

| Archivo | Responsabilidad |
|---|---|
| `ui/theme/Color.kt` | Paleta de colores |
| `ui/theme/Theme.kt` | Theme Compose |
| `ui/theme/Type.kt` | Tipografia |

## Pruebas

El proyecto contiene 22 archivos de prueba en `app/src/test/java/com/lealcode/inventariobillar`.

Se enfocan sobre todo en:

- calculos financieros
- deudas
- pagos
- ganancias
- ventas
- formato de precios

## Notas de mantenimiento

- `MainActivity.kt` concentra mucha navegacion y es un buen candidato a modularizacion.
- `MathUtils.kt` contiene varias responsabilidades y tambien es candidato a extraccion.
- La documentacion principal del repo vive en `README.md` y la carpeta `docs/`.
