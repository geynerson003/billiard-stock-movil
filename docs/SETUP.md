# Setup

## Requisitos previos

- Android Studio
- JDK 17
- Android SDK 34
- acceso a un proyecto de Firebase

## 1. Abrir el proyecto

1. Clonar el repositorio.
2. Abrir la carpeta raiz en Android Studio.
3. Esperar a que Gradle sincronice.

## 2. Configuracion local

Verifica:

- `local.properties`
- ruta del SDK Android
- JDK configurado en Android Studio

## 3. Firebase

El proyecto usa:

- Firebase Auth
- Cloud Firestore

Debes tener:

- `app/google-services.json`
- reglas de Firestore desplegadas o disponibles para el entorno de desarrollo

Mas detalle en [FIREBASE.md](FIREBASE.md).

## 4. Dependencias

La configuracion del modulo Android esta en:

- `build.gradle.kts`
- `app/build.gradle.kts`

Si es la primera vez que se ejecuta en la maquina, Gradle puede descargar el wrapper y dependencias remotas.

## 5. Ejecutar la app

Desde Android Studio:

- seleccionar un emulador o dispositivo
- ejecutar el modulo `app`

## 6. Ejecutar pruebas

Pruebas unitarias:

```bash
./gradlew test
```

Pruebas instrumentadas:

```bash
./gradlew connectedAndroidTest
```

## 7. Problemas comunes

### Falla la inicializacion de Firebase

Revisar:

- `app/google-services.json`
- configuracion del proyecto Firebase
- permisos de red del emulador o dispositivo

### Falla la descarga del wrapper

Posibles causas:

- no hay acceso a red
- proxy corporativo
- firewall

### La app abre pero no autentica

Revisar:

- Auth habilitado en Firebase
- credenciales correctas
- reglas de Firestore

## 8. Archivos importantes para setup

- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/lealcode/inventariobillar/InventarioBillarApp.kt`
- `app/src/main/java/com/lealcode/inventariobillar/data/di/FirebaseModule.kt`
- `firestore.rules`
