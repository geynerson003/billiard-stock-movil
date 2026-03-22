# Release

## Resumen

El proyecto usa `semantic-release` para automatizar versiones, changelog y publicacion desde GitHub Actions.

## Archivos principales

- `/.releaserc.json`
- `/.github/workflows/release.yml`
- `/CHANGELOG.md`

## Flujo actual

1. Se hace push a `main`.
2. GitHub Actions ejecuta el workflow de release.
3. El workflow instala dependencias de `semantic-release`.
4. `semantic-release` analiza commits.
5. Si corresponde, genera nueva version, notas y actualiza `CHANGELOG.md`.

## Configuracion actual

Plugins declarados en `.releaserc.json`:

- `@semantic-release/commit-analyzer`
- `@semantic-release/release-notes-generator`
- `@semantic-release/changelog`
- `@semantic-release/github`
- `@semantic-release/git`

## Buenas practicas para releases

- trabajar sobre Pull Requests pequenos
- usar commits semanticos
- no mezclar cambios de infraestructura con cambios funcionales grandes sin documentacion
- validar workflow de release despues de tocar `.releaserc.json` o `.github/workflows/release.yml`

## Commits recomendados

Ejemplos:

- `feat: agrega filtro de ventas por fecha`
- `fix: corrige calculo de deuda parcial`
- `docs: actualiza documentacion de arquitectura`
- `refactor: reorganiza repositorio de ventas`

## Consideraciones del workflow

El workflow actual ya fue ajustado para:

- usar `checkout` con historial completo
- traer tags
- usar versiones fijas de `semantic-release` y plugins

Esto reduce riesgos de:

- calculo incorrecto de version
- releases no deterministas
- fallos por ausencia de historial git

## Secretos requeridos

Para GitHub Actions:

- `GITHUB_TOKEN`

## Mantenimiento recomendado

- revisar periodicamente compatibilidad entre versiones fijadas de plugins
- evitar instalar paquetes `latest` en el workflow
- mantener `CHANGELOG.md` como archivo administrado por release automatica
