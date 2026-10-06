package com.manuelhg.aad;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Slf4j
public class BinaryFileExample {

    private static final String filename = "foto.jpg";

    public static void processBinaryFile() {
        File file = new File(filename);

        if (!file.exists()) {
            System.err.println("No se encuentra el fichero binario: " + filename);
            return;
        }

        try (BufferedInputStream input = new BufferedInputStream(new FileInputStream(file))) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = input.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }

            String hash = HexFormat.of().formatHex(digest.digest());
            System.out.println("Fichero: " + file.getName());
            System.out.println("Tamaño: " + (file.length() / 1024) + " KB");
            System.out.println("SHA-256: " + hash);

        } catch (IOException | NoSuchAlgorithmException e) {
            System.err.println("Error procesando fichero binario: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        processBinaryFile();
    }
}