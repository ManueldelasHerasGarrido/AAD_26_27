package com.manuelhg.aad;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Slf4j
@Component
public class StoreIngestionScheduler {

    private static final Path INPUT_DIR = Path.of("workspace/store_data/input");
    private static final Path OUTPUT_DIR = Path.of("workspace/store_data/output");
    private static final Path ERROR_DIR = OUTPUT_DIR.resolve("error");

    @Scheduled(fixedRate = 5000)
    public void processStoreBatches() {
        try {
            createDirectories();
            scanInput();
        } catch (IOException e) {
            log.error("Error procesando lotes: {}", e.getMessage(), e);
        }
    }

    private void createDirectories() throws IOException {
        for (Path dir : new Path[]{INPUT_DIR, OUTPUT_DIR, ERROR_DIR}) {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
                log.info("Carpeta creada: {}", dir);
            }
        }
    }

    private void scanInput() throws IOException {
        try (DirectoryStream<Path> files = Files.newDirectoryStream(INPUT_DIR)) {
            for (Path file : files) {
                if (!Files.isRegularFile(file)) {
                    continue;
                }
                String fileName = file.getFileName().toString();

                if (Files.size(file) == 0) {
                    log.error("[ALERT EMAIL SENT -> soporte@aadtex.com] CRITICAL: Empty/Corrupt batch file: {}", fileName);
                    Files.move(file, ERROR_DIR.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                    continue;
                }

                String lowerName = fileName.toLowerCase();
                if (lowerName.endsWith(".csv") || lowerName.endsWith(".enc")
                        || lowerName.endsWith(".jpg") || lowerName.endsWith(".zip")) {
                    log.info("Fichero detectado: {}", fileName);
                }
            }
        }
    }
}

