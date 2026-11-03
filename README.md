# ra1_act1: AadTex Store Ingestion Engine (App 1)

Actividad del módulo **Acceso a Datos (AAD)**, RA1: Manejo de Ficheros.

## Repositorio

- Repositorio de GitHub: https://github.com/ManueldelasHerasGarrido/AAD_26_27
- Rama de la entrega (con la reorganización en
  paquetes): https://github.com/ManueldelasHerasGarrido/AAD_26_27/tree/actividad-1-refactor

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
- Maven (incluido en IntelliJ IDEA)

## Cómo ejecutarla

- **Desde el IDE:** ejecutar la clase `AadApplication`.
- **Con Maven:** `mvn spring-boot:run` (o, en IntelliJ, panel Maven > Plugins > spring-boot > `spring-boot:run`).

Al arrancar, `MultiLanguageDataGenerator` crea automáticamente los ficheros de prueba en `workspace/store_data/input/`,
y el scheduler los procesa en los segundos siguientes. El resultado se ve en el log de la consola y en las carpetas de
`workspace/store_data/output/`.

## Estructura de carpetas de datos

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

## Estructura del código

El proyecto está organizado en paquetes bajo `com.manuelhg.aad`, con una clase por tarea para poder reutilizarlas:

```text
com.manuelhg.aad
├── AadApplication              <- clase principal (@EnableScheduling)
├── StoreIngestionScheduler     <- tarea programada cada 5 s: escanea input/ y reparte cada fichero
├── common
│   ├── AppPaths                <- rutas de todas las carpetas, en un único sitio
│   └── SaleRecord              <- record con una línea de venta
├── repository
│   ├── CrudRepository<T, ID>   <- interfaz genérica (crear, leer, actualizar, borrar)
│   ├── CsvReader               <- implementa CrudRepository<SaleRecord, Path>
│   ├── TxtReader               <- implementa CrudRepository<String, Path>
│   └── BinaryFileCopier        <- copia binaria con buffer de 4 KB
└── service
    ├── CryptoService           <- cifrado y descifrado AES-128
    ├── CloudBucketService      <- publica los JSON en el bucket local
    ├── DirectoryService        <- crea las carpetas necesarias
    ├── SalesTotalService       <- suma las ventas de un CSV
    ├── CsvBatchService         <- procesa lotes .csv
    ├── EncBatchService         <- procesa lotes .enc (descifra en RAM)
    ├── ZipBatchService         <- procesa lotes .zip con ZipInputStream
    ├── SignatureService        <- copia las firmas .jpg
    ├── SummaryService          <- genera el resumen JSON de cada lote
    └── MultiLanguageDataGenerator <- CommandLineRunner que genera los datos de prueba al arrancar
```


