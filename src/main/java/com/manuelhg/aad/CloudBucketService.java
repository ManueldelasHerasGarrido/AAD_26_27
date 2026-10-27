package com.manuelhg.aad;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
public class CloudBucketService {

    private static final Path BUCKET_DIR =
            Path.of("workspace/store_data/output/bucket/aadtex-daily-reports/2026/09");

    public void upload(String fileName, String json) throws IOException {
        Files.createDirectories(BUCKET_DIR);
        Files.writeString(BUCKET_DIR.resolve(fileName), json, StandardCharsets.UTF_8);
        log.info("Resumen publicado en el bucket: {}", fileName);
    }
}


