package com.manuelhg.aad.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static com.manuelhg.aad.common.AppPaths.BUCKET_DIR;

@Slf4j
@Service
public class CloudBucketService {

    public void upload(String fileName, String json) throws IOException {
        Files.createDirectories(BUCKET_DIR);
        Files.writeString(BUCKET_DIR.resolve(fileName), json, StandardCharsets.UTF_8);
        log.info("Resumen publicado en el bucket: {}", fileName);
    }
}


