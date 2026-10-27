package com.manuelhg.aad;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoreIngestionScheduler {

    private static final Path INPUT_DIR = Path.of("workspace/store_data/input");
    private static final Path OUTPUT_DIR = Path.of("workspace/store_data/output");
    private static final Path ERROR_DIR = OUTPUT_DIR.resolve("error");
    private static final Path PROCESSED_DIR = OUTPUT_DIR.resolve("processed");
    private static final Path SIGNATURES_DIR = PROCESSED_DIR.resolve("signatures");

    private final CryptoService cryptoService;

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
        for (Path dir : new Path[]{INPUT_DIR, OUTPUT_DIR, ERROR_DIR, PROCESSED_DIR, SIGNATURES_DIR}) {
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
                } else if (lowerName.endsWith(".jpg")) {
                    copySignature(file);
                } else if (lowerName.endsWith(".zip")) {
                    processZip(file);
                } else if (lowerName.endsWith(".enc")) {
                    processEnc(file);
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

    private void processEnc(Path file) throws IOException {
        String fileName = file.getFileName().toString();
        BigDecimal total;

        try {
            byte[] decrypted = cryptoService.decrypt(Files.readAllBytes(file));
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(decrypted), StandardCharsets.UTF_8))) {
                total = sumSales(reader);
            }
        } catch (GeneralSecurityException e) {
            log.error("[ALERT EMAIL SENT -> soporte@aadtex.com] CRITICAL: Empty/Corrupt batch file: {}", fileName);
            Files.move(file, ERROR_DIR.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            return;
        }

        log.info("Lote cifrado {} procesado. Total de ventas: {}", fileName, total);
        Files.move(file, PROCESSED_DIR.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
    }

    private BigDecimal sumSales(BufferedReader reader) throws IOException {
        BigDecimal total = BigDecimal.ZERO;
        reader.readLine(); // primera línea: cabecera, la descartamos

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isBlank()) {
                continue;
            }
            String[] fields = line.split(",");
            total = total.add(new BigDecimal(fields[2].trim()));
        }
        return total;
    }

    private void copySignature(Path file) throws IOException {
        String fileName = file.getFileName().toString();

        try (BufferedInputStream in = new BufferedInputStream(Files.newInputStream(file))) {
            copyToFile(in, SIGNATURES_DIR.resolve(fileName));
        }

        log.info("Firma copiada: {}", fileName);
        Files.delete(file);
    }

    private void copyToFile(InputStream in, Path target) throws IOException {
        try (BufferedOutputStream out = new BufferedOutputStream(Files.newOutputStream(target))) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    private void processZip(Path zipFile) throws IOException {
        String zipName = zipFile.getFileName().toString();

        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile), StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                // Nos quedamos solo con el nombre, por seguridad (sin rutas internas del zip)
                String entryName = Path.of(entry.getName()).getFileName().toString();
                String lowerEntry = entryName.toLowerCase();

                if (lowerEntry.endsWith(".csv")) {
                    // No cerramos este reader: cerraría también el zip
                    BufferedReader reader = new BufferedReader(new InputStreamReader(zis, StandardCharsets.UTF_8));
                    BigDecimal total = sumSales(reader);
                    log.info("ZIP {} -> {} procesado. Total de ventas: {}", zipName, entryName, total);
                } else if (lowerEntry.endsWith(".jpg")) {
                    copyToFile(zis, SIGNATURES_DIR.resolve(entryName));
                    log.info("ZIP {} -> firma copiada: {}", zipName, entryName);
                }
            }
        }

        Files.move(zipFile, PROCESSED_DIR.resolve(zipName), StandardCopyOption.REPLACE_EXISTING);
    }
}


