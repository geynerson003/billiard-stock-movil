# Project Status

## Estado general

El proyecto esta funcional y tiene una base clara para un entorno real, pero todavia hay temas tecnicos que conviene resolver antes de considerarlo listo para una evolucion mas intensiva o una publicacion abierta sin contexto.

## Fortalezas actuales

- stack moderno Android
- separacion razonable por capas
- uso de Hilt y Firebase
- cobertura de pruebas unitarias sobre calculos
- flujo de release automatizado
- documentacion base del repositorio

## Riesgos conocidos

### 1. Integridad de inventario

El flujo de ventas permite escenarios donde el mismo producto puede impactar mal el stock si se agrega varias veces dentro de una misma venta y la transaccion no consolida cantidades por producto.

### 2. Consistencia de mesas

Hay flujos donde el identificador de mesa y el nombre visible se mezclan, lo que puede afectar filtros, detalles y reportes.

### 3. Reinicio de partidas

La funcionalidad de reiniciar partida necesita endurecerse para evitar multiples partidas activas sobre una misma mesa o sesion.

### 4. Deuda y pagos

La logica de deuda existe y funciona, pero aun merece mejoras de atomicidad y simplificacion para escenarios concurrentes.

### 5. Dashboard y reportes

Parte de la logica financiera sigue distribuida en varios puntos del codigo. Conviene centralizarla.

### 6. Escalabilidad de consultas

Dashboard y reportes consumen mucha agregacion en cliente. Con alto volumen de datos esto puede afectar rendimiento.

## Recomendaciones prioritarias

1. consolidar calculos financieros y de deuda en una fuente unica de verdad
2. hacer mas transaccionales los cambios de deuda e inventario
3. corregir el flujo de ventas por mesa para usar IDs reales
4. reforzar el flujo de partidas activas y reinicios
5. mover parte del filtrado pesado a consultas mas eficientes

## Alcance de esta documentacion

Estos documentos dejan el repositorio mejor preparado para GitHub, onboarding y mantenimiento. No sustituyen una fase posterior de hardening tecnico.

## Fuente de verdad

Para mantenimiento actual del repositorio, tomar como referencia:

- `README.md`
- carpeta `docs/`

Los archivos de analisis historicos se mantienen como contexto, no como definicion final del sistema.
