package com.xdesign.munrotable.util;

import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.exceptions.CsvValidationException;
import com.xdesign.munrotable.model.Hill;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.File;

import static com.xdesign.munrotable.model.Hill.Category.MUNRO;
import static com.xdesign.munrotable.model.Hill.Category.TOP;
import static com.xdesign.munrotable.util.CsvColumnHeading.*;

public class CsvDataLoader implements DataLoader {

    private final File file;

    public CsvDataLoader(File file) {
        this.file = file;
    }

    @Override
    public List<Hill> load() {
        try (var reader = new CSVReaderHeaderAware(new FileReader(file, StandardCharsets.UTF_8))) {
            Map<String, String> row;
            var hills = new ArrayList<Hill>();

            while ((row = reader.readMap()) != null) {
                if (isValidHill(row)) {
                    hills.add(createHill(row));
                }
            }
            return hills;
        } catch (CsvValidationException | IOException e) {
            throw new RuntimeException("Failed to load CSV", e);
        }
    }

    private boolean isValidHill(Map<String, String> row) {
        return hasEssentialData(row) && !isDeleted(row);
    }

    private boolean hasEssentialData(Map<String, String> row) {
        return List.of(NAME_FIELD, HEIGHT_FIELD, GRID_REFERENCE_FIELD, CATEGORY_FIELD).stream()
                .allMatch(key -> row.containsKey(key) && !row.get(key).isBlank());
    }

    private boolean isDeleted(Map<String, String> row) {
        return row.get(CATEGORY_FIELD).isBlank();
    }

    private Hill createHill(Map<String, String> row) {
        return new Hill(
                row.get(NAME_FIELD),
                Double.parseDouble(row.get(HEIGHT_FIELD)),
                row.get(GRID_REFERENCE_FIELD),
                switch (row.get(CATEGORY_FIELD).toUpperCase()) {
                    case "MUN" -> MUNRO;
                    case "TOP" -> TOP;
                    default -> throw new IllegalArgumentException("Unknown category: " + row.get(CATEGORY_FIELD));
                }
        );
    }
}
