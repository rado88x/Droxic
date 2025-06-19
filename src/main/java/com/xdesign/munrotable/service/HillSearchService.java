package com.xdesign.munrotable.service;

import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.exceptions.CsvValidationException;
import com.xdesign.munrotable.dto.HillSearchRequest;
import com.xdesign.munrotable.dto.Sort;
import com.xdesign.munrotable.dto.SortField;
import com.xdesign.munrotable.dto.SortOrder;
import com.xdesign.munrotable.model.Hill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.xdesign.munrotable.model.Hill.Category.MUNRO;
import static com.xdesign.munrotable.model.Hill.Category.TOP;
import static com.xdesign.munrotable.util.CsvColumnHeading.CATEGORY_FIELD;
import static com.xdesign.munrotable.util.CsvColumnHeading.GRID_REFERENCE_FIELD;
import static com.xdesign.munrotable.util.CsvColumnHeading.HEIGHT_FIELD;
import static com.xdesign.munrotable.util.CsvColumnHeading.NAME_FIELD;
import static java.nio.charset.StandardCharsets.UTF_8;

//probably would make it @Service
public final class HillSearchService {

    private static final EnumMap<SortField, Comparator<Hill>> FIELD_COMPARATORS = new EnumMap<>(SortField.class);
    private static final Logger log = LoggerFactory.getLogger(HillSearchService.class);

    static {
        FIELD_COMPARATORS.put(SortField.HEIGHT, Comparator.comparing(
                Hill::height, Comparator.naturalOrder()));
        FIELD_COMPARATORS.put(SortField.NAME, Comparator.comparing(
                Hill::name, String.CASE_INSENSITIVE_ORDER));
    }

    private final File csvFile;

    public HillSearchService(File csvFile) {
        this.csvFile = csvFile;
    }

    public List<Hill> searchHills(HillSearchRequest request) {
        return loadHillsFromCsvFile(csvFile).stream()
                .filter(hill -> matchesCriteria(hill, request))
                .sorted(buildComparator(request))
                .limit(request.limit()) ///moving limit because it cut list to shorter before comparator to sort hills
                .toList();
    }

    private boolean matchesCriteria(Hill hill, HillSearchRequest request) {
        if (request.category() != null && !hill.category().name().equalsIgnoreCase(request.category().toString())) {
            log.debug("Request category incompatible with Scottish enumerations/classification.");
            return false;
        }
        if (request.minHeight() != null && hill.height() < request.minHeight()) {
            log.debug("Hill height = {} is lower than requested minimum height = {}.", hill.height(), request.minHeight());
            return false;
        }
        if (request.maxHeight() != null && hill.height() > request.maxHeight()) {
            log.debug("Hill height = {} is higher than requested maximum height= {}.", hill.height(), request.maxHeight());
            return false;
        }
        if (request.name() != null && !hill.name().toLowerCase().contains(request.name().toLowerCase())) {
            log.debug("Hill name = {} does not match requested name = {}.", hill.name(), request.name());
            return false;
        }
        return true;
    }

    private Comparator<Hill> buildComparator(HillSearchRequest request) {
        return request.sorts().stream()
                .map(this::getComparator)
                .reduce(Comparator::thenComparing)
                .orElse((h1, h2) -> 0);
    }

    private Comparator<Hill> getComparator(Sort sort) {
        var comparator = FIELD_COMPARATORS.get(sort.field());
        return sort.order() == SortOrder.ASC ? comparator : comparator.reversed();
    }


    //Probably can be extracted in DataLoader class to separate logic from Service layer
    private static List<Hill> loadHillsFromCsvFile(File file) {
        try (var csvReader = new CSVReaderHeaderAware(new FileReader(file, UTF_8))) {

            Map<String, String> rowData;
            var summits = new ArrayList<Hill>();

            while ((rowData = csvReader.readMap()) != null) {
                if (isQualifyingHill(rowData)) {
                    summits.add(createHill(rowData));
                }
            }

            return summits;
        } catch (IOException | CsvValidationException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static boolean isQualifyingHill(Map<String, String> rowData) {
        return isHillData(rowData) && !isDeletedHill(rowData);
    }

    private static boolean isHillData(Map<String, String> rowData) {
        // Identify rows with no summit data, e.g. rows used for notes
        var hillFields = List.of(NAME_FIELD, HEIGHT_FIELD, GRID_REFERENCE_FIELD, CATEGORY_FIELD);

        return rowData.entrySet().stream()
                .filter(e1 -> hillFields.contains(e1.getKey()))
                .noneMatch(e2 -> e2.getValue().isBlank());
    }

    private static boolean isDeletedHill(Map<String, String> rowData) {
        // Identify Munros and Tops deleted from the 1997 classification
        var category = rowData.get(CATEGORY_FIELD);
        return category.isBlank();
    }

    private static Hill createHill(Map<String, String> rowData) {
        var name = rowData.get(NAME_FIELD);
        var height = Double.parseDouble(rowData.get(HEIGHT_FIELD));
        var gridReference = rowData.get(GRID_REFERENCE_FIELD);
        var category = getCategory(rowData.get(CATEGORY_FIELD));
        return new Hill(name, height, gridReference, category);
    }

    private static Hill.Category getCategory(String value) {
        return switch (value.toUpperCase()) {
            case "MUN" -> MUNRO;
            case "TOP" -> TOP;
            default -> throw new IllegalArgumentException("Unknown hill category [%s]".formatted(value));
        };
    }
}
