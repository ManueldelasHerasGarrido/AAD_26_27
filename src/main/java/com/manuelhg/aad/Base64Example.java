package com.manuelhg.aad;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

@Slf4j
@UtilityClass
public class Base64Example {

    public static void processBase64(Path imagePath, Path base64Path, Path restoredPath) {

        if (!Files.exists(imagePath)) {
            log.error("El fichero no existe: {}", imagePath);
            return;
        }

        try {
            // 1. Leer la imagen
            byte[] binaryData = Files.readAllBytes(imagePath);
            log.info("Tamaño original: {} bytes", binaryData.length);

            // 2. Convertir la imagen a Base64
            String base64String =
                    Base64.getEncoder().encodeToString(binaryData);

            log.info(
                    "Tamaño en Base64: {} caracteres",
                    base64String.length()
            );

            // 3. Guardar el Base64 en un fichero TXT
            Files.writeString(base64Path, base64String);

            log.info("Base64 guardado en: {}", base64Path);

            // 4. Decodificar el Base64
            byte[] restoredData =
                    Base64.getDecoder().decode(base64String);

            // 5. Crear la imagen restaurada
            Files.write(restoredPath, restoredData);

            log.info(
                    "Fichero restaurado con éxito en: {}",
                    restoredPath
            );

        } catch (IOException e) {
            log.error(
                    "Error procesando Base64: {}",
                    e.getMessage(),
                    e
            );
        }
    }

    public static void main(String[] args) {

        Path imagePath = Path.of("foto.jpg");
        Path base64Path = Path.of("foto_base64.txt");
        Path restoredPath = Path.of("foto_restaurada.jpg");

        processBase64(imagePath, base64Path, restoredPath);
    }
}
