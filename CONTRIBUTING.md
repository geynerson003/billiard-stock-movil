# Contributing

Gracias por contribuir a Billiards Stock.

## Objetivo

Mantener un repositorio claro, estable y facil de evolucionar.

## Flujo recomendado

1. Crear una rama desde `main`.
2. Hacer cambios pequenos y enfocados.
3. Actualizar pruebas y documentacion cuando aplique.
4. Abrir un Pull Request con contexto funcional y tecnico.

## Convenciones de ramas

Sugerencias:

- `feat/nombre-corto`
- `fix/nombre-corto`
- `docs/nombre-corto`
- `refactor/nombre-corto`

## Convenciones de commits

El repositorio usa `semantic-release`, asi que conviene seguir commits semanticos:

- `feat: agrega ...`
- `fix: corrige ...`
- `docs: actualiza ...`
- `refactor: reorganiza ...`
- `test: agrega ...`
- `chore: ajusta ...`

## Reglas de codigo

- Mantener Kotlin idiomatico y legible.
- Separar UI, estado y acceso a datos.
- Evitar logica de negocio compleja dentro de pantallas Compose.
- Priorizar cambios atomicos y faciles de revisar.
- No introducir dependencias nuevas sin justificar su necesidad.
- Si se modifica una regla de negocio, actualizar pruebas y documentacion.

## Reglas de documentacion

Actualizar documentacion cuando cambie cualquiera de estos puntos:

- arquitectura
- flujo de datos
- configuracion de Firebase
- proceso de release
- reglas de negocio

## Validacion antes de abrir PR

- compila localmente
- las pruebas relevantes pasan o se explica por que no se ejecutaron
- no se suben archivos locales
- no se exponen secretos
- la descripcion del PR explica impacto funcional

## Secretos y configuracion sensible

- no subir credenciales privadas
- revisar cambios en `google-services.json`
- validar cambios en `firestore.rules`
- no versionar archivos temporales del IDE

## Alcance de la documentacion actual

La documentacion principal del proyecto esta en:

- [README.md](README.md)
- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)
- [docs/CODEMAP.md](docs/CODEMAP.md)
- [docs/SETUP.md](docs/SETUP.md)
- [docs/FIREBASE.md](docs/FIREBASE.md)
- [docs/RELEASE.md](docs/RELEASE.md)
- [docs/PROJECT_STATUS.md](docs/PROJECT_STATUS.md)
