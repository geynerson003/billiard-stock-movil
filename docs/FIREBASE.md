# Firebase

## Servicios usados

El codigo actual usa principalmente:

- Firebase Authentication
- Cloud Firestore

Tambien existe dependencia de Firebase Messaging, pero no forma parte del flujo principal visible en la implementacion actual.

## Archivos relacionados

- `app/google-services.json`
- `firestore.rules`
- `app/src/main/java/com/lealcode/inventariobillar/data/di/FirebaseModule.kt`
- `app/src/main/java/com/lealcode/inventariobillar/data/repository/AuthRepositoryImpl.kt`

## Inicializacion

La inicializacion ocurre en:

- `InventarioBillarApp.kt`

Y la provision de dependencias ocurre en:

- `FirebaseModule.kt`

## Estructura de colecciones

### Usuarios

```text
/users/{userId}
```

Se almacena informacion del usuario autenticado.

### Datos del negocio

```text
/businesses/{userId}
/businesses/{userId}/products/{productId}
/businesses/{userId}/sales/{saleId}
/businesses/{userId}/clients/{clientId}
/businesses/{userId}/tables/{tableId}
/businesses/{userId}/table_sessions/{sessionId}
/businesses/{userId}/games/{gameId}
/businesses/{userId}/expenses/{expenseId}
/businesses/{userId}/payments/{paymentId}
```

El modelo implementado es multi-tenant por usuario.

## Reglas de seguridad

Las reglas actuales limitan lectura y escritura al usuario autenticado cuyo `uid` coincide con el documento raiz del negocio.

Resumen:

- cada usuario solo accede a su documento en `/users/{userId}`
- cada usuario solo accede a `/businesses/{userId}` y sus subcolecciones
- todo lo demas se deniega

## Recomendaciones operativas

- validar reglas antes de publicar cambios
- evitar modificar la estructura de colecciones sin actualizar repositorios y docs
- revisar migraciones cuando se agreguen campos nuevos
- auditar cambios sobre deuda, ventas e inventario porque impactan integridad de datos

## Configuracion esperada en Firebase

- Authentication habilitado con email y password
- Firestore creado en modo nativo
- reglas desplegadas
- app Android registrada con el `applicationId`:

```text
com.lealcode.inventariobillar
```

## Nota sobre `google-services.json`

Este archivo debe corresponder al proyecto Firebase del entorno donde se va a ejecutar la app. Antes de publicar el repositorio, revisar que el archivo versionado sea el correcto para el flujo del equipo.
