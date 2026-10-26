package com.manuelhg.aad;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StoreIngestionScheduler {

    @Scheduled(fixedRate = 5000)
    public void processStoreBatches() {
        log.info("Buscando lotes de tiendas...");
    }
}

