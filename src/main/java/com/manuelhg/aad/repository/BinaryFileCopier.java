package com.manuelhg.aad.repository;

import org.springframework.stereotype.Component;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

//@Component hace que Spring cree objetos de esta clase y lo reparte a quien loi pida.
@Component
public class BinaryFileCopier {

    private static final int BUFFER_SIZE = 4096;

    public void copy(InputStream in, Path target) throws IOException {
        try (BufferedOutputStream out = new BufferedOutputStream(Files.newOutputStream(target))) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }
}


