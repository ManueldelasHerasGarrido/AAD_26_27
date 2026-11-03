package com.manuelhg.aad.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.manuelhg.aad.common.AppPaths.*;

@Slf4j
@Service
public class DirectoryService {

    //El array new Path[] con 5 carpetas es recorrido por el for. Crea las carpetas que falten
    public void ensureDirectories() throws IOException {
        for (Path dir : new Path[]{INPUT_DIR, OUTPUT_DIR, ERROR_DIR, PROCESSED_DIR, SIGNATURES_DIR}) {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
                log.info("Carpeta creada: {}", dir);
            }
        }
    }
}


