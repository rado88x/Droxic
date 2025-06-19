package com.xdesign.munrotable.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xdesign.munrotable.model.Hill;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class JsonDataLoader implements DataLoader {

    private final File file;
    private final ObjectMapper objectMapper;

    public JsonDataLoader(File file) {
        this.file = file;
        this.objectMapper = new ObjectMapper(); // or inject it if using Spring
    }

    @Override
    public List<Hill> load() {
        try {
            return objectMapper.readValue(file, new TypeReference<>() {});
        } catch (IOException e) {
            throw new RuntimeException("Failed to load hills from JSON", e);
        }
    }
}