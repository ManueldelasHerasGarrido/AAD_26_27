package com.manuelhg.aad;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j

public class EncodingExample {

    public static void writeAndReadUtf8(Path path) {
        try {
            String text = "¡Hola! Canción, España, 数据库, こんにちは 🚀";

            Files.writeString(path, text, StandardCharsets.UTF_8);

            String content = Files.readString(path, StandardCharsets.UTF_8);

            log.info("Contenido leído correctamente: {}", content);

        } catch (IOException e) {
            log.error("Error de I/O gestionando codificación: {}", e.getMessage(), e);
        }
    }


    public static void main(String[] args) {
        writeAndReadUtf8(Path.of("texto.txt"));
    }
}