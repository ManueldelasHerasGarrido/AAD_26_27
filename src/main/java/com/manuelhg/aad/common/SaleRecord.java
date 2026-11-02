package com.manuelhg.aad.common;

import java.math.BigDecimal;
import java.time.LocalDate;

//Esta clase está pensada para guardar datos
public record SaleRecord(
        String storeId,
        String storeName,
        BigDecimal salesAmount,
        String category,
        LocalDate transactionDate
) {
}


