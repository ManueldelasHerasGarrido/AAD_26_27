package com.manuelhg.aad.common;

import java.math.BigDecimal;
import java.time.LocalDate;

//Esta clase está pensada para guardar datos.
//Un record es una clase para guardar datos. Java crea solo el constructor y los métodos para leer cada campo.
//SaleRecord representa una línea de venta del CSV: tienda, importe, categoría y fecha.
//Es el tipo concreto que usamos como T en CrudRepository<SaleRecord, Path>.

public record SaleRecord(
        String storeId,
        String storeName,
        BigDecimal salesAmount,
        String category,
        LocalDate transactionDate
) {
}


