package com.manuelhg.aad.repository;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

@Component
public class TxtReader implements CrudRepository<String, Path> {

    @Override
    public void create(Path file, List<String> lines) throws IOException {
        Files.write(file, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
    }

    @Override
    public List<String> read(Path file) throws IOException {
        return Files.readAllLines(file, StandardCharsets.UTF_8);
    }

    @Override
    public void update(Path file, List<String> lines) throws IOException {
        Files.write(file, lines, StandardCharsets.UTF_8,
                StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    @Override
    public void delete(Path file) throws IOException {
        Files.deleteIfExists(file);
    }
}


