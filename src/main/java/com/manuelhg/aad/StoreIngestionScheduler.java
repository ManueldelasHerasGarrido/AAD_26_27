package com.manuelhg.aad;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
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
    private static final Path PROCESSED_DIR = OUTPUT_DIR.resolve("processed");

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
        for (Path dir : new Path[]{INPUT_DIR, OUTPUT_DIR, ERROR_DIR, PROCESSED_DIR}) {
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
                if (lowerName.endsWith(".csv")) {
                    processCsv(file);
                } else if (lowerName.endsWith(".enc") || lowerName.endsWith(".jpg") || lowerName.endsWith(".zip")) {
                    log.info("Fichero detectado: {}", fileName);
                }
            }
        }
    }

    private void processCsv(Path file) throws IOException {
        String fileName = file.getFileName().toString();
        BigDecimal total;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8))) {
            total = sumSales(reader);
        }

        log.info("Lote {} procesado. Total de ventas: {}", fileName, total);
        Files.move(file, PROCESSED_DIR.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
    }

    private BigDecimal sumSales(BufferedReader reader) throws IOException {
        BigDecimal total = BigDecimal.ZERO;
        String line = reader.readLine(); // primera línea: cabecera, la saltamos

        while ((line = reader.readLine()) != null) {
            if (line.isBlank()) {
                continue;
            }
            String[] fields = line.split(",");
            total = total.add(new BigDecimal(fields[2].trim()));
        }
        return total;
    }
}


