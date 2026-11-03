package com.manuelhg.aad.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;

import static com.manuelhg.aad.common.AppPaths.ERROR_DIR;
import static com.manuelhg.aad.common.AppPaths.PROCESSED_DIR;

@Slf4j
@Service
@RequiredArgsConstructor
public class EncBatchService {

    private final CryptoService cryptoService;
    private final SalesTotalService salesTotalService;
    private final SummaryService summaryService;

    public void process(Path file) throws IOException {
        String fileName = file.getFileName().toString();
        BigDecimal total;

        try {
            byte[] decrypted = cryptoService.decrypt(Files.readAllBytes(file));
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(decrypted), StandardCharsets.UTF_8))) {
                total = salesTotalService.total(reader);
            }
        } catch (GeneralSecurityException e) {
            log.error("[ALERT EMAIL SENT -> soporte@aadtex.com] CRITICAL: Empty/Corrupt batch file: {}", fileName);
            Files.move(file, ERROR_DIR.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            return;
        }

        log.info("Lote cifrado {} procesado. Total de ventas: {}", fileName, total);
        summaryService.publish(fileName, total);
        Files.move(file, PROCESSED_DIR.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
    }
}


