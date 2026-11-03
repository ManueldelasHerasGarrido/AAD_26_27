package com.manuelhg.aad.repository;

import com.manuelhg.aad.common.SaleRecord;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

//Implementa CrudRepository<SaleRecord, Path>: trabaja con ficheros CSV y objetos SaleRecord.
//readFrom() acepta cualquier BufferedReader (fichero, bytes en memoria o entrada de un zip), por eso se reutiliza.
//Es la clase que sabe de comas y columnas, así el resto del programa no necesita saberlo.

@Component
public class CsvReader implements CrudRepository<SaleRecord, Path> {

    private static final String HEADER = "StoreID,StoreName,SalesAmount,Category,TransactionDate";

    @Override
    //Al usar CREATE_NEW nos aseguramos de que si el fichero existe, falla para no pisar nada
    public void create(Path file, List<SaleRecord> records) throws IOException {
        Files.writeString(file, toCsv(records), StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
    }

    @Override
    public List<SaleRecord> read(Path file) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return readFrom(reader);
        }
    }

    // Lee de cualquier BufferedReader: un fichero, bytes en memoria o una entrada de zip
    //Convierte cada linea leia en un SaleRecord y devuelve una lista
    public List<SaleRecord> readFrom(BufferedReader reader) throws IOException {
        List<SaleRecord> records = new ArrayList<>();
        reader.readLine(); // cabecera, la descartamos

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isBlank()) {
                continue;
            }
            String[] f = line.split(",");
            records.add(new SaleRecord(
                    f[0].trim(),
                    f[1].trim(),
                    new BigDecimal(f[2].trim()),
                    f[3].trim(),
                    LocalDate.parse(f[4].trim())));
        }
        return records;
    }

    @Override
    //Se usa TRUNCATE_EXISTING sin CREATE para que solo funciones si ya hay algo que exista y sobreescriba
    public void update(Path file, List<SaleRecord> records) throws IOException {
        Files.writeString(file, toCsv(records), StandardCharsets.UTF_8,
                StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    @Override
    public void delete(Path file) throws IOException {
        //Delete if exist para que no de fallo si el fichero no está
        Files.deleteIfExists(file);
    }

    private String toCsv(List<SaleRecord> records) {
        StringBuilder sb = new StringBuilder(HEADER).append("\n");
        for (SaleRecord r : records) {
            sb.append(r.storeId()).append(",")
                    .append(r.storeName()).append(",")
                    .append(r.salesAmount().toPlainString()).append(",")
                    .append(r.category()).append(",")
                    .append(r.transactionDate()).append("\n");
        }
        return sb.toString();
    }
}


