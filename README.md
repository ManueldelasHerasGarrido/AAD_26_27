# ra1_act1: AadTex Store Ingestion Engine (App 1)

Actividad del módulo **Acceso a Datos (AAD)**, RA1: Manejo de Ficheros.

## Repositorio

- Repositorio de GitHub: https://github.com/ManueldelasHerasGarrido/AAD_26_27
- Rama de la actividad: https://github.com/ManueldelasHerasGarrido/AAD_26_27/tree/actividad-1

## Descripción

Aplicación Spring Boot que, cada 5 segundos, recoge los lotes que depositan las tiendas en la carpeta `input/` y los
procesa sin intervención manual:

| Fichero de entrada | Tratamiento                                                                               |
|--------------------|-------------------------------------------------------------------------------------------|
| `.csv`             | Lectura en UTF-8 (tildes, eñes y caracteres chinos) y suma del importe de ventas          |
| `.enc`             | Descifrado AES-128 en memoria (sin escribir el texto en claro en disco) y lectura del CSV |
| `.zip`             | Lectura con `ZipInputStream`, sin ficheros temporales: procesa el CSV y copia la firma    |
| `.jpg`             | Copia de la firma con buffer de 4 KB a `output/processed/signatures/`                     |
| Fichero de 0 bytes | Alerta simulada a `soporte@aadtex.com` en el log y movimiento a `output/error/`           |

Por cada lote correcto se genera un resumen `summary_batch_N.json` que se publica en un bucket local.

## Requisitos

- Java 21
- Maven (o el wrapper `mvnw` incluido en el proyecto)

## Cómo ejecutarla

Desde la raíz del proyecto:

```bash
mvn spring-boot:run
```

O desde el IDE, ejecutando la clase `AadApplication`.

Al arrancar, `MultiLanguageDataGenerator` crea automáticamente los ficheros de prueba en `workspace/store_data/input/`,
y el scheduler los procesa en los segundos siguientes. El resultado se puede ver en el log de la consola y en las
carpetas de `workspace/store_data/output/`.

## Estructura de carpetas

Todas las rutas son relativas a la raíz del proyecto:

```text
workspace/store_data/
├── keys/                     <- clave AES (AadTexSecret.key)
├── input/                    <- entrada de lotes de las tiendas
└── output/
    ├── processed/            <- lotes procesados
    │   └── signatures/       <- firmas JPG copiadas
    ├── error/                <- ficheros vacíos o corruptos
    └── bucket/aadtex-daily-reports/2026/09/   <- resúmenes JSON
```

## Clases principales

- `AadApplication`: clase principal; activa el planificador con `@EnableScheduling`.
- `StoreIngestionScheduler`: tarea programada (`@Scheduled`, cada 5 s) que escanea `input/` y procesa cada lote.
- `CryptoService`: cifrado y descifrado AES-128 (`AES/ECB/PKCS5Padding`).
- `CloudBucketService`: publica los resúmenes JSON en el bucket local.
- `MultiLanguageDataGenerator`: `CommandLineRunner` que genera los datos de prueba al arrancar.


