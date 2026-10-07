package com.edu.aad.file;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

// Lombok crea automáticamente un logger llamado "log"
// que podemos utilizar con log.info(), log.error(), etc.
@Slf4j
public class NioLogExample {

    /* Este metodo recibe un mensaje y lo guarda en un archivo de log.
     * Al ser static, podemos llamarlo desde main sin necesidad
     * de crear un objeto NioLogExample.
     */

    public static void writeLogEntry(String message) {

        // Define la ruta donde se guardará el archivo, "logs" es la carpeta y "application.log" es el archivo.
        // La ruta es relativa al directorio de trabajo del proyecto.
        Path logFile = Path.of("logs", "application.log");

        try {
            // getParent() obtiene la carpeta que contiene el archivo.
            // En este caso devuelve: "logs".
            // Comprobamos que exista y, si no existe, la creamos.
            if (logFile.getParent() != null) {
                Files.createDirectories(logFile.getParent());
            }

            /* Escribe el mensaje en el archivo.
             * message:
             *      Es el texto que queremos guardar.
             *
             * System.lineSeparator():
             *      Añade un salto de línea después del mensaje para
             *      que cada entrada del log aparezca en una línea.
             *
             * CREATE:
             *      Si application.log no existe, lo crea.
             *
             * APPEND:
             *      Añade el mensaje al final del archivo sin borrar
             *      los mensajes que ya existen.
             */
            Files.writeString(
                    logFile,
                    message + System.lineSeparator(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );

            // Muestra mediante el logger la ubicación completa del archivo.
            log.info("Log registrado en: {}", logFile.toAbsolutePath());

            // Muestra el tamaño actual del archivo en bytes.
            log.info("Tamaño actual: {} bytes", Files.size(logFile));

        } catch (IOException e) {

            // Si ocurre algún problema al crear, escribir o acceder
            // al archivo, se registra el error y su stack trace.
            log.error(
                    "Error al escribir el archivo de log: {}",
                    e.getMessage(),
                    e
            );
        }
    }

    /*
     * Punto de entrada del programa.
     * La ejecución comienza aquí cuando ejecutamos la clase.
     */
    public static void main(String[] args) {

        // Como writeLogEntry() es static, realmente no necesitamos
        // crear un objeto NioLogExample.
        //
        // Por tanto, esta línea se podría eliminar.
        NioLogExample example = new NioLogExample();

        // Escribe la primera entrada en application.log.
        example.writeLogEntry("Aplicación iniciada");

        // Añade una segunda entrada al final del archivo.
        example.writeLogEntry("Procesando datos");

        // Añade una tercera entrada al final del archivo.
        example.writeLogEntry("Aplicación terminada");
    }
}
