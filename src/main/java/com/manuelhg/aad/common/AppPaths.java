package com.manuelhg.aad.common;

import java.nio.file.Path;

//Constante y accesible desde cualquier clase, sin crear objetos.
//Esta clase reúne todas las rutas del proyecto en un único sitio. Las demás clases las usan con import static.
//Es final y su constructor es private porque es una clase de utilidades: no tiene sentido hacer new AppPaths().

public final class AppPaths {

    public static final Path BASE_DIR = Path.of("workspace/store_data");
    public static final Path INPUT_DIR = BASE_DIR.resolve("input");
    public static final Path OUTPUT_DIR = BASE_DIR.resolve("output");
    public static final Path ERROR_DIR = OUTPUT_DIR.resolve("error");
    public static final Path PROCESSED_DIR = OUTPUT_DIR.resolve("processed");
    public static final Path SIGNATURES_DIR = PROCESSED_DIR.resolve("signatures");
    public static final Path BUCKET_DIR = OUTPUT_DIR.resolve("bucket/aadtex-daily-reports/2026/09");
    public static final Path STAGING_DIR = BASE_DIR.resolve("staging");
    public static final Path KEYS_DIR = BASE_DIR.resolve("keys");

    private AppPaths() {
    }
}

