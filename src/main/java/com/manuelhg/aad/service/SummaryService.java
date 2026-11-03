package com.manuelhg.aad.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

//Genera el JSON resumen de cada lote y se lo entrega a CloudBucketService.
//batchCounter da el número al lote (summary_batch_1.json, _2...) y se reinicia en cada arranque.

@Service
@RequiredArgsConstructor
public class SummaryService {

    private final CloudBucketService cloudBucketService;

    private int batchCounter = 0;

    //Recibe de donde viene el lote (sourceFile) y cuanto sumó (total), y hace un JSON, lo entrega a CloudBucketService
    public void publish(String sourceFile, BigDecimal total) throws IOException {
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


