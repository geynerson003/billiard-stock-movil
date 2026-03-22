# Análisis de Riesgos y Lógica: Billiard Stock

Este documento detalla los hallazgos tras un análisis profundo del código fuente, centrándose en posibles fallos de lógica, riesgos de escalabilidad y problemas de integridad de datos en un entorno de producción.

## 1. Riesgos de Escalabilidad (Crítico)

### ⚠️ Agregación de Datos en Memoria
En `ReportsRepositoryImpl.kt` y `DashboardViewModel.kt`, se utilizan flujos combinados (`combine`) que obtienen **todas** las ventas, gastos y productos de Firestore para calcular totales y promedios en el dispositivo del usuario.

*   **Fallo**: A medida que el negocio crezca y acumule miles de ventas, la aplicación consumirá cantidades excesivas de memoria RAM y ancho de banda, lo que eventualmente provocará fallos por `OutOfMemoryError` o lentitud extrema.
*   **Recomendación**: Implementar consultas agregadas de Firestore o realizar el filtrado por fecha directamente en la consulta a la base de datos (server-side) en lugar de filtrar en Kotlin con `.filter { ... }`.

---

## 2. Integridad de Datos y Consistencia

### ❌ Falta de Atomicidad en Deudas (`DebtCalculationService`)
El método `registerPayment` lee el estado del cliente, calcula la nueva deuda y luego actualiza Firestore en pasos separados.
*   **Fallo**: Si dos dispositivos registran un pago o una venta simultáneamente para el mismo cliente, uno podría sobrescribir al otro basado en datos obsoletos, causando que la deuda del cliente sea incorrecta.
*   **Recomendación**: Utilizar `firestore.runTransaction` para asegurar que la lectura y escritura de la deuda del cliente sea atómica.

### ❌ Restauración de Inventario Incompleta
En `SalesRepositoryImpl.deleteSale`, existe una condición `if (!sale.isGameSale)` que evita restaurar el inventario si la venta proviene de una partida de billar finalizada.
*   **Fallo**: Si una venta de partida se elimina por error, los productos deduclidos (apuestas) no regresan al stock, perdiendo la trazabilidad del inventario.
*   **Recomendación**: Evaluar si las ventas de partidas deben comportarse igual que las ventas normales al ser eliminadas.

### ❌ Borrado de Apuestas Frágil
En `GamesRepositoryImpl.removeBet`, se utiliza el índice de la lista como cadena (`index.toString()`) para identificar qué apuesta eliminar.
*   **Fallo**: Si la lista de apuestas cambia de orden entre la lectura y la ejecución, se eliminará la apuesta equivocada.
*   **Recomendación**: Añadir un ID único a cada `GameBet`.

---

## 3. Errores de Lógica Financiera

### ⚠️ Inconsistencia Histórica en Costos
`MathUtils.calculateTotalSupplierCostCorrected` utiliza la lista de productos **actual** para calcular el costo de ventas **pasadas**.
*   **Fallo**: Si un producto sube de precio hoy, todos los reportes de meses anteriores reflejarán una ganancia menor a la real, porque el sistema recalcula el costo antiguo con el precio nuevo.
*   **Recomendación**: Snapshotear (guardar una copia) del `supplierPrice` y `unitsPerPackage` dentro de cada `SaleItem` en el momento de la venta.

### ⚠️ Duplicación de Lógica Dashboard vs Reportes
El `DashboardViewModel` recalcula manualmente el ingreso, ganancia y productos top en lugar de consumir los resultados de `ReportsRepository`.
*   **Fallo**: Existe un alto riesgo de que los números del Dashboard no coincidan con los de los Reportes debido a sutiles diferencias en la lógica de filtrado o redondeo (ej. `netProfit` se calcula diferente en ambos sitios).
*   **Recomendación**: Centralizar el cálculo financiero en una única fuente de verdad (Repository o UseCase).

---

## 4. Otros Hallazgos

*   **Escrituras Ciegas**: `GamesRepositoryImpl.updateGame` utiliza `.set(game)`, lo que sobrescribe todo el documento. Si un usuario está agregando un participante mientras otro cambia el precio de la mesa, uno de los cambios se perderá.
*   **Precisión de Punto Flotante**: Se utiliza `Double` para moneda y comparaciones como `deuda <= 0.01`. Aunque es común, para aplicaciones financieras críticas es preferible `Long` (en centavos) o `BigDecimal` para evitar errores de redondeo acumulados.
*   **Dependencias Fantasma**: El código hace referencia a un `FinancialCalculationService` que no parece estar implementado adecuadamente, recurriendo a métodos `@Deprecated` en `MathUtils`.

## Conclusión
La aplicación es funcional para volúmenes bajos de datos, pero presenta riesgos estructurales importantes en cuanto a **consistencia de transacciones** y **escalabilidad de reportes**. Se recomienda priorizar la implementación de transacciones en el sistema de deudas y optimizar las consultas de reportes antes de una fase de producción masiva.
