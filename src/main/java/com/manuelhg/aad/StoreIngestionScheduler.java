package com.manuelhg.aad;

import com.manuelhg.aad.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static com.manuelhg.aad.common.AppPaths.ERROR_DIR;
import static com.manuelhg.aad.common.AppPaths.INPUT_DIR;

@Slf4j  //Con @Slf4j creamos logs
@Component  //Con @Component, Spring crea un bean al arrancar.
@RequiredArgsConstructor  //Con esto, Lombok genera un constructor con todos los campos que sean "final"
//Spring los usa para entregar cada servicio a esta clase, inyección de dependencias.
public class StoreIngestionScheduler {

    //Ya no tiene la lógica de cada tipo de fichero. Solo decide quién se encarga de cada uno,
    //y cada servicio hace una sola tarea (responsabilidad única).
    private final DirectoryService directoryService;
    private final CsvBatchService csvBatchService;
    private final EncBatchService encBatchService;
    private final ZipBatchService zipBatchService;
    private final SignatureService signatureService;

    //Este mét0do solo hace dos cosas, preparar las carpetas y escanear las entradas.
    //Es el único con @Scheduled, así que es el único que Spring lanza cada 5 segundos, y él llama a los demás.
    //Por defecto, Spring ejecuta las tareas programadas en un solo hilo. Si una tarda +5 segundos, la siguiente espera.
    @Scheduled(fixedRate = 5000)
    public void processStoreBatches() {
        try {
            //Delegamos en DirectoryService, que crea las carpetas que falten.
            directoryService.ensureDirectories();
            scanInput();
        } catch (IOException e) {
            log.error("Error procesando lotes: {}", e.getMessage(), e);
        }
    }

    //Este método recorre la carpeta input y reparte cada fichero según su extensión.
    //Es llamado por processStoreBatches() en cada vuelta.
    //DirectoryStream va dentro de un try() para que se cierre solo al terminar.
    private void scanInput() throws IOException {
        try (DirectoryStream<Path> files = Files.newDirectoryStream(INPUT_DIR)) {
            for (Path file : files) {
                if (!Files.isRegularFile(file)) {
                    continue;
                }
                String fileName = file.getFileName().toString();

                //Se comprueba el tamaño antes que la extensión, así cualquier fichero vacío va a /error con la alerta.
                if (Files.size(file) == 0) {
                    log.error("[ALERT EMAIL SENT -> soporte@aadtex.com] CRITICAL: Empty/Corrupt batch file: {}", fileName);
                    Files.move(file, ERROR_DIR.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                    continue;
                }

                //toLowerCase() para que FOTO.JPG y foto.jpg se traten igual.
                //Una extensión desconocida no entra en ninguna rama y el fichero se queda en input.
                String lowerName = fileName.toLowerCase();
                if (lowerName.endsWith(".csv")) {
                    //CsvBatchService lee el CSV en UTF-8, calcula el total, lo anota en log y JSON, y lo mueve a /processed
                    csvBatchService.process(file);
                } else if (lowerName.endsWith(".jpg")) {
                    //SignatureService copia la firma a /signatures con buffer de 4 KB y borra el original de input.
                    signatureService.copy(file);
                } else if (lowerName.endsWith(".zip")) {
                    //ZipBatchService abre el zip sin extraerlo a disco, y procesa el CSV y la firma de dentro.
                    zipBatchService.process(file);
                } else if (lowerName.endsWith(".enc")) {
                    //EncBatchService descifra el fichero en RAM (AES) y lo lee como un CSV. Si falla, lo manda a /error.
                    encBatchService.process(file);
                }
            }
        }
    }
}


