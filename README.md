# Nirvana FLAC Checklist

Aplicación Android ligera para organizar la discografía de Nirvana y marcar las canciones que ya tienes descargadas en tu dispositivo.

- Compatible con Android 10 (API 29) o superior.
- Funciona sin conexión permanente a Internet.
- Guarda el progreso localmente en el teléfono.
- APK objetivo: menor de 5 MB.
- No descarga ni distribuye música; es un catálogo y organizador personal.

## Funciones

- Lista de álbumes, EP, sencillos, recopilaciones y material pendiente de verificación.
- Lista de canciones por lanzamiento.
- Casillas para marcar canciones descargadas.
- Barra de progreso por álbum y porcentaje global.
- Contador de canciones descargadas.
- Búsqueda por álbum o canción.
- Filtros: todas, descargadas y pendientes.
- Guardado automático del progreso.
- Exportación e importación del checklist en JSON.
- Restablecimiento del progreso.
- Tema oscuro inspirado en la estética grunge.

## Catálogo ampliable

El catálogo está en `app/src/main/assets/catalog.json`. Puedes añadir lanzamientos o canciones editando ese archivo; la aplicación los leerá sin cambiar el código.

Cada lanzamiento incluye:

- `type`: `studio`, `live`, `ep`, `single`, `compilation`, `demos` o `special`.
- `official`: `true` para lanzamientos oficiales, `false` para material no oficial.
- `verification`: `verified` o `pending_verification`.
- `tracks`: títulos reales de las canciones; no se incluyen duraciones inventadas.

## Compilación y publicación

El workflow `.github/workflows/build-release.yml`:

1. Compila el proyecto.
2. Genera una APK release firmada.
3. Comprueba que el APK sea menor de 5 MB.
4. Permite compilación manual desde la pestaña **Actions**.
5. Al crear una etiqueta `v1.0.0`, publica automáticamente un GitHub Release con el APK, notas, tamaño y compatibilidad Android 10+.

## Instalación

1. Entra en **Releases**.
2. Descarga el archivo `nirvana-flac-checklist-v1.0.0.apk`.
3. Ábrelo desde el administrador de archivos del teléfono.
4. Si Android pregunta, permite instalar aplicaciones desde esa fuente.
5. Pulsa **Instalar** y abre **Nirvana FLAC Checklist**.
