# GymBeast 🏋️ — app de gimnasio para Android

App nativa (Kotlin + Jetpack Compose, Material 3) pensada para el Google Pixel 9a
(funciona en cualquier Android 8.0 o superior). Todo se guarda en el móvil, sin cuentas ni internet.

## Qué hace

- **Rutinas**: 4 plantillas incluidas (Push, Pull, Pierna, Cuerpo completo) y creación/edición de las tuyas
  (series, repeticiones, peso inicial y descanso por ejercicio). Duplicar, borrar, guardar un entreno como rutina.
- **Entreno en vivo**: tabla de series con kg / reps, columna "anterior" con lo que hiciste la última vez
  (tócala para copiarlo), series de calentamiento, añadir/quitar/cambiar ejercicios, notas, cronómetro
  y **temporizador de descanso** automático con vibración y sonido (±15 s, saltar).
  El peso de la última sesión se precarga solo. La pantalla se mantiene encendida.
- **Comparaciones de peso**: al terminar te dice cosas como
  *"Hoy has levantado 130 kg en total, lo mismo que pesa un bebé tigre 🐯"*.
  Hay ~40 objetos/animales: desde un gato hasta la Torre Eiffel. También del total semanal y de toda tu vida,
  con el "próximo hito" a desbloquear.
- **Récords personales** detectados automáticamente (peso máximo, 1RM estimado, volumen por sesión).
- **Historial** por meses con detalle de cada entreno, valoración de cómo te sentiste y compartir el resumen.
- **Gráficos**: volumen / entrenos / series por semana, calendario de días entrenados, series por grupo
  muscular, evolución de cada ejercicio (1RM, peso máximo, volumen, reps) y del peso corporal.
- **Nutrición** (pestaña propia):
  - Diario de comidas por día (desayuno, comida, merienda, cena, pre y post-entreno) con anillo de calorías
    y barras de proteína, carbohidratos, grasas y fibra frente a tu objetivo.
  - Base de ~130 alimentos habituales en España (valores por 100 g), alimentos propios, "añadir rápido",
    recientes, raciones rápidas, copiar el día anterior y contador de agua.
  - **Mi cuerpo y objetivos**: guarda altura, peso, edad y medidas y calcula IMC con escala de colores,
    peso saludable, % de grasa (fórmula Navy o medido), masa magra, FFMI, índice cintura-altura,
    metabolismo basal (Mifflin-St Jeor / Katch-McArdle), gasto diario y tus calorías y macros según el
    objetivo (perder grasa, recomposición, mantener, ganar músculo) y el ritmo.
  - **Coach nutricional**: compara la tendencia real de tu peso con el ritmo buscado, estima tu gasto
    calórico real a partir de lo que comes y te propone ajustes de calorías.
  - Gráficos de calorías y proteína por día con línea de objetivo y estadísticas de adherencia.
  - 22 ideas de comidas con ingredientes, preparación y macros (se añaden al diario con un toque) y
    más de 30 consejos de nutrición deportiva (proteína, timing, suplementos, mitos…).
- **Peso y medidas**: peso, % grasa, cintura, pecho, brazo, muslo, con gráficos.
- **Biblioteca** de ~80 ejercicios por grupo muscular + ejercicios personalizados (peso, peso corporal o cardio).
- **Herramientas**: calculadora de 1RM con tabla de porcentajes, calculadora de discos por lado y
  "¿a qué equivale este peso?".
- **Ajustes**: objetivo semanal, descanso por defecto, peso de la barra, tema claro/oscuro, colores
  Material You, y **copia de seguridad** (exportar/importar a un archivo, p. ej. en Google Drive).

## Instalar en el móvil

1. Descarga `GymBeast.apk` en el Pixel (o pásalo por cable / Drive).
2. Ábrelo. Android pedirá permiso para "instalar apps desconocidas" desde el navegador o Archivos: actívalo.
3. Instalar → Abrir.

Una versión nueva se instala encima sin perder datos solo si está firmada con la misma clave.
Por seguridad la clave de firma no se sube al repositorio: si alguna vez instalas un APK firmado con otra
clave (por ejemplo el que genera GitHub Actions), Android pedirá desinstalar la anterior. Antes de hacerlo,
usa **Ajustes → Exportar copia** y luego **Importar copia** en la nueva instalación.

Para firmar con tu propia clave: pon el archivo en `keystore/gymapp.jks` y define
`GYMAPP_STORE_PASSWORD` (y opcionalmente `GYMAPP_KEY_ALIAS`, `GYMAPP_KEY_PASSWORD`) como variables de
entorno o en `~/.gradle/gradle.properties`. Sin ellas se usa la clave de depuración estándar.

## Compilar

Necesita JDK 17+ y el Android SDK (API 35):

```bash
cd GymApp
./gradlew assembleRelease        # APK en app/build/outputs/apk/release/app-release.apk
./gradlew testReleaseUnitTest    # tests de la lógica
```

También se compila solo en GitHub Actions (workflow *GymApp Android APK*) y el APK queda como artefacto.
