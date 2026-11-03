package com.manuelhg.aad.repository;

import org.springframework.stereotype.Component;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

//@Component hace que Spring cree objetos de esta clase y lo reparte a quien loi pida.
//Copia datos binarios de un InputStream a un fichero usando un buffer de 4 KB (byte[4096]).
//Solo hay 4 KB en memoria a la vez, así se evita OutOfMemoryError con ficheros grandes.
//No cierra el InputStream que recibe: quien lo abre es quien lo cierra (importante con el zip).

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


