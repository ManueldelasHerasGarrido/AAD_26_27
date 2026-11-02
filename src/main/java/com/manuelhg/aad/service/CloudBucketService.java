package com.manuelhg.aad.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

//Con esta clase simulamos subir ficheros a la nube. En este programa se suben a una carpeta local.
//Si llega el momento y queremos subirlo a la nube de verdad, solo habría que cambiar esta clase.

@Slf4j //Nos da el log.
@Service
public class CloudBucketService {

    //Establecemos que BUCKET_DIR será la carpeta donde vayan los registros.
    private static final Path BUCKET_DIR =
            Path.of("workspace/store_data/output/bucket/aadtex-daily-reports/2026/09");
    //Path.of representa la ruta a un fichero o carpeta. Es la forma moderna con NIO.2, en lugar de string.
    //Path.of indica solo la dirección, pero puede que no exista la carpeta.

    //Este mét0d0 recibe el nombre del fichero y su contenido en texto JSON.
    public void upload(String fileName, String json) throws IOException {
        Files.createDirectories(BUCKET_DIR);
        Files.writeString(BUCKET_DIR.resolve(fileName), json, StandardCharsets.UTF_8);
        log.info("Resumen publicado en el bucket: {}", fileName);
    }
}


