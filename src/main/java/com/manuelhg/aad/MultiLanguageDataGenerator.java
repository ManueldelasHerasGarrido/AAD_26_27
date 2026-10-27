package com.manuelhg.aad;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import.org.jspecify.annotations.NullMarked;

@NullMarked
@Slf4j
@Component
@RequiredArgsConstructor
public class MultiLanguageDataGenerator implements CommandLineRunner {

    private static final Path BASE_DIR = Path.of("workspace/store_data");
    private static final Path INPUT_DIR = BASE_DIR.resolve("input");
    private static final Path STAGING_DIR = BASE_DIR.resolve("staging");
    private static final Path KEYS_DIR = BASE_DIR.resolve("keys");

    private static final String HEADER = "StoreID,StoreName,SalesAmount,Category,TransactionDate\n";

    private final CryptoService cryptoService;

    @Override
    public void run(String... args) throws Exception {
        Files.createDirectories(INPUT_DIR);
        Files.createDirectories(STAGING_DIR);
        Files.createDirectories(KEYS_DIR);

        Files.writeString(KEYS_DIR.resolve("AadTexSecret.key"), "AadTexSecret2026", StandardCharsets.UTF_8);

        String spain = HEADER
                + "ZARA_ES_001,Zara Puerta del Sol Madrid,14500.50,WOMEN,2026-09-13\n"
                + "ZARA_ES_001,Zara Puerta del Sol Madrid,3600.30,MEN,2026-09-13\n"
                + "ZARA_ES_001,Zara Gran Vía Madrid,2000.00,KIDS,2026-09-13\n";

        String china = HEADER
                + "ZARA_CH_001,Zara 上海南京东路店,28900.00,WOMEN,2026-09-13\n"
                + "ZARA_CH_001,Zara 上海南京东路店,12400.80,MEN,2026-09-13\n";

        String financial = HEADER
                + "ZARA_CENTRAL_FIN,Central Financial Office,95000.00,FINANCIAL_REPORT,2026-09-13\n";

        String usa = HEADER
                + "ZARA_US_001,Zara Fifth Avenue New York,18200.00,WOMEN,2026-09-13\n"
                + "ZARA_US_001,Zara Fifth Avenue New York,9100.40,MEN,2026-09-13\n";

        deliver("daily_sales_spain.csv", spain.getBytes(StandardCharsets.UTF_8));
        deliver("signature_spain.jpg", createJpg(Color.BLUE));

        deliver("daily_sales_china.csv", china.getBytes(StandardCharsets.UTF_8));
        deliver("signature_china.jpg", createJpg(Color.RED));

        deliver("daily_sales_financial.enc", cryptoService.encrypt(financial.getBytes(StandardCharsets.UTF_8)));
        deliver("signature_financial.jpg", createJpg(Color.DARK_GRAY));

        deliver("batch_usa.zip", createZip(usa));

        deliver("daily_sales_broken_store.csv", new byte[0]);

        Files.deleteIfExists(STAGING_DIR);
    }

    // Escribe el fichero completo en "staging" y solo entonces lo mueve a input,
    // para que el scheduler nunca encuentre un fichero a medio escribir.
    private void deliver(String fileName, byte[] content) throws IOException {
        Path staged = STAGING_DIR.resolve(fileName);
        Files.write(staged, content);
        Files.move(staged, INPUT_DIR.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        log.info("Dato de prueba generado: {}", fileName);
    }

    private byte[] createJpg(Color color) throws IOException {
        BufferedImage image = new BufferedImage(200, 80, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 200, 80);
        g.setColor(color);
        g.setStroke(new BasicStroke(3));
        g.drawLine(20, 60, 90, 20);
        g.drawLine(90, 20, 120, 60);
        g.drawLine(120, 60, 180, 25);
        g.dispose();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", out);
        return out.toByteArray();
    }

    private byte[] createZip(String csvContent) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes, StandardCharsets.UTF_8)) {
            zip.putNextEntry(new ZipEntry("daily_sales_usa.csv"));
            zip.write(csvContent.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();

            zip.putNextEntry(new ZipEntry("signature_usa.jpg"));
            zip.write(createJpg(Color.GREEN.darker()));
            zip.closeEntry();
        }
        return bytes.toByteArray();
    }
}


