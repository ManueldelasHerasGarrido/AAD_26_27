package com.manuelhg.aad.service;

import com.manuelhg.aad.repository.BinaryFileCopier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.manuelhg.aad.common.AppPaths.SIGNATURES_DIR;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignatureService {

    private final BinaryFileCopier binaryFileCopier;

    //Este mét0d0 copia una firma desde input.
    //Es usado por scanInput cuando el fichero acaba en .jpg
    //Abre la imagen, la delega a copyToFile() y al final, la borra de input.
    public void copy(Path file) throws IOException {
        String fileName = file.getFileName().toString();

        try (BufferedInputStream in = new BufferedInputStream(Files.newInputStream(file))) {
            binaryFileCopier.copy(in, SIGNATURES_DIR.resolve(fileName));
        }   //El destino al que va la copia procesada.

        log.info("Firma copiada: {}", fileName);
        Files.delete(file);
    }
}


