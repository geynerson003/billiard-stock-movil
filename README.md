# Billiards Stock

Aplicacion Android para administrar un negocio de billar con control de inventario, ventas, mesas, clientes, deudas, gastos y reportes.

## Resumen

El proyecto esta construido con Kotlin, Jetpack Compose, Hilt y Firebase. Su enfoque principal es centralizar la operacion diaria de un billar:

- autenticacion de usuarios
- inventario de productos
- ventas externas y ventas asociadas a mesas
- control de clientes y deudas
- registro de pagos
- gastos operativos
- reportes y dashboard

## Stack principal

- Kotlin
- Android SDK 34
- Jetpack Compose + Material 3
- Hilt para inyeccion de dependencias
- Firebase Auth
- Cloud Firestore
- Vico Charts
- JUnit4 para pruebas unitarias

## Funcionalidades actuales

- inicio de sesion, registro y recuperacion de contrasena
- CRUD de productos
- CRUD de clientes
- registro de ventas con multiples items
- gestion de mesas y partidas
- control de pagos y deudas por cliente
- registro de gastos
- dashboard con resumen financiero
- reportes por rango de fechas

## Documentacion del repositorio

- [Guia de arquitectura](docs/ARCHITECTURE.md)
- [Mapa del codigo](docs/CODEMAP.md)
- [Guia de instalacion y puesta en marcha](docs/SETUP.md)
- [Configuracion de Firebase](docs/FIREBASE.md)
- [Proceso de release](docs/RELEASE.md)
- [Estado actual del proyecto](docs/PROJECT_STATUS.md)
- [Guia de contribucion](CONTRIBUTING.md)
- [Changelog](CHANGELOG.md)

## Estructura general

```text
.
|-- app/
|   |-- src/main/java/com/lealcode/inventariobillar/
|   |   |-- data/
|   |   |-- di/
|   |   |-- ui/
|   |   |-- util/
|   |   `-- utils/
|   |-- src/main/res/
|   `-- build.gradle.kts
|-- .github/workflows/
|-- gradle/
|-- firestore.rules
|-- build.gradle.kts
|-- settings.gradle.kts
`-- .releaserc.json
```

## Requisitos

- Android Studio reciente
- JDK 17
- Android SDK 34
- proyecto de Firebase configurado
- archivo `app/google-services.json`

## Inicio rapido

1. Abrir el proyecto en Android Studio.
2. Verificar `local.properties` y el SDK de Android.
3. Confirmar que `app/google-services.json` corresponde al proyecto de Firebase deseado.
4. Sincronizar Gradle.
5. Ejecutar la app en un emulador o dispositivo con Android 8.0 o superior.

Mas detalle en [docs/SETUP.md](docs/SETUP.md).

## Pruebas

El proyecto incluye pruebas unitarias enfocadas en calculos financieros, deudas, ventas y pagos.

Comando esperado:

```bash
./gradlew test
```

Nota: el wrapper puede necesitar descargar Gradle en una maquina nueva.

## Release

El proyecto incluye `semantic-release` con workflow de GitHub Actions sobre la rama `main`.

- configuracion: [.releaserc.json](.releaserc.json)
- workflow: [.github/workflows/release.yml](.github/workflows/release.yml)

Mas detalle en [docs/RELEASE.md](docs/RELEASE.md).

## Estado del proyecto

El sistema es funcional y tiene una base tecnica clara, pero aun existen mejoras recomendadas en consistencia de datos, manejo de deuda, dashboard y flujo de partidas.

Consulta [docs/PROJECT_STATUS.md](docs/PROJECT_STATUS.md) para ver el estado actual y los pendientes tecnicos mas importantes.

## Documentos historicos

Los siguientes archivos se conservan como referencia de analisis previos:

- [ANALISIS_SISTEMA.md](ANALISIS_SISTEMA.md)
- [SOLUCION_ROBUSTA_ENTERPRISE.md](SOLUCION_ROBUSTA_ENTERPRISE.md)

No deben considerarse como la unica fuente de verdad del estado actual. La referencia principal para mantenimiento del repositorio es esta documentacion nueva.
