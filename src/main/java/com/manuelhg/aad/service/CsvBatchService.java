package com.manuelhg.aad.service;

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

import static com.manuelhg.aad.common.AppPaths.PROCESSED_DIR;

@Slf4j
@Service
@RequiredArgsConstructor
public class CsvBatchService {

    private final SalesTotalService salesTotalService;
    private final SummaryService summaryService;

    public void process(Path file) throws IOException {
        String fileName = file.getFileName().toString();
        BigDecimal total;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8))) {
            total = salesTotalService.total(reader);
        }

        log.info("Lote {} procesado. Total de ventas: {}", fileName, total);
        summaryService.publish(fileName, total);
        Files.move(file, PROCESSED_DIR.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
    }
}


