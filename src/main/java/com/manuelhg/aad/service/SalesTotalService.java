package com.manuelhg.aad.service;

import com.manuelhg.aad.common.SaleRecord;
import com.manuelhg.aad.repository.CsvReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class SalesTotalService {

    private final CsvReader csvReader;

    public BigDecimal total(BufferedReader reader) throws IOException {
        BigDecimal total = BigDecimal.ZERO;
        for (SaleRecord record : csvReader.readFrom(reader)) {
            total = total.add(record.salesAmount());
        }
        return total;
    }
}


