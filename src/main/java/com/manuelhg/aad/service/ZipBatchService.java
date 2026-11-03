package com.manuelhg.aad.service;

import com.manuelhg.aad.repository.BinaryFileCopier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.manuelhg.aad.common.AppPaths.PROCESSED_DIR;
import static com.manuelhg.aad.common.AppPaths.SIGNATURES_DIR;

//Procesa un lote .zip con ZipInputStream, sin extraerlo a disco. Recorre las entradas con getNextEntry().
//El CSV de dentro se suma y la firma se copia. Solo se usa el nombre de cada entrada (evita Zip Slip).
//El reader del CSV no se cierra porque cerraría el zip entero y no se podrían leer las demás entradas.

@Slf4j
@Service
@RequiredArgsConstructor
public class ZipBatchService {

    private final SalesTotalService salesTotalService;
    private final SummaryService summaryService;
    private final BinaryFileCopier binaryFileCopier;

    public void process(Path zipFile) throws IOException {
        String zipName = zipFile.getFileName().toString();

        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile), StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                // Solo el nombre, por seguridad (sin rutas internas del zip)
                String entryName = Path.of(entry.getName()).getFileName().toString();
                String lowerEntry = entryName.toLowerCase();

                if (lowerEntry.endsWith(".csv")) {
                    // No cerramos este reader: cerraría también el zip
                    BufferedReader reader = new BufferedReader(new InputStreamReader(zis, StandardCharsets.UTF_8));
                    BigDecimal total = salesTotalService.total(reader);
                    log.info("ZIP {} -> {} procesado. Total de ventas: {}", zipName, entryName, total);
                    summaryService.publish(zipName + "/" + entryName, total);
                } else if (lowerEntry.endsWith(".jpg")) {
                    binaryFileCopier.copy(zis, SIGNATURES_DIR.resolve(entryName));
                    log.info("ZIP {} -> firma copiada: {}", zipName, entryName);
                }
            }
        }

        Files.move(zipFile, PROCESSED_DIR.resolve(zipName), StandardCopyOption.REPLACE_EXISTING);
    }
}


