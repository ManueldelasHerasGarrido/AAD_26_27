package com.manuelhg.aad;

import com.manuelhg.aad.service.CloudBucketService;
import com.manuelhg.aad.service.CryptoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.manuelhg.aad.common.AppPaths.*;

@Slf4j  //Con @Slf4j creamos logs
@Component  //Con @Component, Spring crea un bean al arrancar.
@RequiredArgsConstructor  //Con esto, Lombok genera un constructor con todos los campos que sena "final"
//Spring los usa para entregar CryptoService y CloudBucketService, inyección de dependencias.
public class StoreIngestionScheduler {

    private final CryptoService cryptoService;
    private final CloudBucketService cloudBucketService;

    private int batchCounter = 0;

    //ESte mét0d0 solo hace dos cosas, preparar las carpetas y escanear las entradas.
    //Por defecto, Spring ejecuta las tareas programadas en un solo hilo. Si una tarda +5 segundos, la siguiente espera.
    @Scheduled(fixedRate = 5000)
    public void processStoreBatches() {
        try {
            createDirectories();
            scanInput();
        } catch (IOException e) {
            log.error("Error procesando lotes: {}", e.getMessage(), e);
        }
    }

    //El array new Path[] con 5 carpetas es recorrido por el for. Crea las carpetas que falten
    private void createDirectories() throws IOException {
        for (Path dir : new Path[]{INPUT_DIR, OUTPUT_DIR, ERROR_DIR, PROCESSED_DIR, SIGNATURES_DIR}) {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
                log.info("Carpeta creada: {}", dir);
                //Solo se escribe el log cuando se crea una carpeta, por eso la consola no se llena cada 5 seg.
            }
        }
    }

    private void scanInput() throws IOException {
        try (DirectoryStream<Path> files = Files.newDirectoryStream(INPUT_DIR)) {
            for (Path file : files) {
                if (!Files.isRegularFile(file)) {
                    continue;
                }
                String fileName = file.getFileName().toString();

                if (Files.size(file) == 0) {
                    log.error("[ALERT EMAIL SENT -> soporte@aadtex.com] CRITICAL: Empty/Corrupt batch file: {}", fileName);
                    Files.move(file, ERROR_DIR.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                    continue;
                }

                String lowerName = fileName.toLowerCase();
                if (lowerName.endsWith(".csv")) {
                    processCsv(file);
                } else if (lowerName.endsWith(".jpg")) {
                    copySignature(file);
                } else if (lowerName.endsWith(".zip")) {
                    processZip(file);
                } else if (lowerName.endsWith(".enc")) {
                    processEnc(file);
                }
            }
        }
    }

    //Con este mét0d0, leemos CSV del disco.
    //Llamado por scanImput() cuando el fichero acaba en .csv
    //Recibe la ruta dle fichero y lee CSV, calcula el total, lo anota en log y JSON, y mueve fichero a /processed
    private void processCsv(Path file) throws IOException {
        String fileName = file.getFileName().toString();
        //Declaramos BigDeciaml fuera del try porque necesitamos que esxista fuera delbloque para el log y el resumen.
        BigDecimal total;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8))) {
            total = sumSales(reader);
        }

        log.info("Lote {} procesado. Total de ventas: {}", fileName, total);
        publishSummary(fileName, total);
        //Movemos el archivo cuando acabe el try. Mientras no se puede, y se mueve para no volver a leerlo en input.
        Files.move(file, PROCESSED_DIR.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
    }

    //Este mét0do lee un nfichero cifrado
    //Es llamado por scanInput cuando un fichero acaba en .enc
    //Funciona como processCsv(), pero debe descifrar contenido antes de leer, y puede fallar si un fichero no
    //se puede descifrar.
    private void processEnc(Path file) throws IOException {
        String fileName = file.getFileName().toString();
        BigDecimal total;

        try {                                     //leet t0d0 el fichero cifrado de golpe y devuelve un byte[]. En RAM
            byte[] decrypted = cryptoService.decrypt(Files.readAllBytes(file));
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(decrypted), StandardCharsets.UTF_8))) {
                total = sumSales(reader);    //Esto ^ convierte el array en flujo de lectura. Conecta el resto de cadena
            }
        } catch (GeneralSecurityException e) {
            log.error("[ALERT EMAIL SENT -> soporte@aadtex.com] CRITICAL: Empty/Corrupt batch file: {}", fileName);
            Files.move(file, ERROR_DIR.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            return;
        }

        log.info("Lote cifrado {} procesado. Total de ventas: {}", fileName, total);
        publishSummary(fileName, total);
        Files.move(file, PROCESSED_DIR.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
    }

    //mét0d0 usado en processCSV, processEnc y processZip
    //Usamos BufferedReade en lugar de fichero porque este mét0do no sabe de donde vienen los dato. Solo lee x lineas.
    private BigDecimal sumSales(BufferedReader reader) throws IOException {
        BigDecimal total = BigDecimal.ZERO;
        reader.readLine(); // primera línea: cabecera, la descartamos

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isBlank()) {
                continue;
            }
            String[] fields = line.split(",");
            total = total.add(new BigDecimal(fields[2].trim()));
        }
        return total;
    }

    //Este mét0d0 copia una firma desde input.
    //Es usado por scanInput cuando el fichero acaba en .jpg
    //Abre la imagen, la delega a copyToFile() y al final, la borra de input.
    private void copySignature(Path file) throws IOException {
        String fileName = file.getFileName().toString();
        //abre fichero y entrega bytes.
        try (BufferedInputStream in = new BufferedInputStream(Files.newInputStream(file))) {
            copyToFile(in, SIGNATURES_DIR.resolve(fileName));
            //El destino al que va la copia procesada.
        }

        log.info("Firma copiada: {}", fileName);
        Files.delete(file);
    }

    //Este es el mét0d0 que realmente copia. Recibe de donde leer (con el in = new del mét0do anterior) y a
    //qué fichero escribir
    private void copyToFile(InputStream in, Path target) throws IOException {
        try (BufferedOutputStream out = new BufferedOutputStream(Files.newOutputStream(target))) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    //Este mét0d0 abre un zip sin extraerlo a disco.
    //Es llamado `por scanInput cuando el fichero acaba en .zip
    private void processZip(Path zipFile) throws IOException {
        String zipName = zipFile.getFileName().toString();

        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile), StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                // Nos quedamos solo con el nombre, por seguridad (sin rutas internas del zip)
                String entryName = Path.of(entry.getName()).getFileName().toString();
                String lowerEntry = entryName.toLowerCase();

                if (lowerEntry.endsWith(".csv")) {
                    // No cerramos este reader: cerraría también el zip
                    BufferedReader reader = new BufferedReader(new InputStreamReader(zis, StandardCharsets.UTF_8));
                    BigDecimal total = sumSales(reader);
                    log.info("ZIP {} -> {} procesado. Total de ventas: {}", zipName, entryName, total);
                    publishSummary(zipName + "/" + entryName, total);
                } else if (lowerEntry.endsWith(".jpg")) {
                    copyToFile(zis, SIGNATURES_DIR.resolve(entryName));
                    log.info("ZIP {} -> firma copiada: {}", zipName, entryName);
                }
            }
        }

        Files.move(zipFile, PROCESSED_DIR.resolve(zipName), StandardCopyOption.REPLACE_EXISTING);
    }

    //Este mét0do es llamado por processCsv, processEnc y processZip, si un lote se ha leído bien.
    //Recibe de donde viene el lote (sourceFile) y cuanto sumó (total), y hace un JSON, lo entrega a CloudBucketService
    private void publishSummary(String sourceFile, BigDecimal total) throws IOException {
        //Esto le da el número al lote
        batchCounter++;
        String json = """
                {
                  "batchId": %d,
                  "sourceFile": "%s",
                  "totalSales": %s,
                  "processedAt": "%s"
                }
                """.formatted(batchCounter, sourceFile, total.toPlainString(), LocalDateTime.now());

        cloudBucketService.upload("summary_batch_" + batchCounter + ".json", json);
    }
}


