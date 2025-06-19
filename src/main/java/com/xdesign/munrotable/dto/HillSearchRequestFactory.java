package com.xdesign.munrotable.dto;

import com.xdesign.munrotable.model.Hill;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static org.apache.commons.lang3.StringUtils.isBlank;


//@Component and inject it to service or controller for easier testing and decoupling IoC DI or keep it simple static?
public final class HillSearchRequestFactory {

    private HillSearchRequestFactory() { /* Non-instantiable class */ }

    public static HillSearchRequest newRequest(
            String category,
            Double minHeight,
            Double maxHeight,
            List<String> sortCriteria,
            int limit,
            String name) {
        Validate.isTrue(limit > 0, "Limit must be greater than zero; found [%d]", limit);
        validateHeightBracket(minHeight, maxHeight);

        var hillCategory = parseCategory(category);
        var sorts = createSorts(sortCriteria);
        return new HillSearchRequestImpl(hillCategory, minHeight, maxHeight, sorts, limit, name);
    }

    private static Hill.Category parseCategory(String categoryStr) {
        if (isBlank(categoryStr)) {
            return null;
        }

        try {
            var normalizedCategory = categoryStr.trim().toUpperCase();
            return Hill.Category.valueOf(normalizedCategory);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown Category [%s]".formatted(categoryStr), ex);
        }
    }

    private static List<Sort> createSorts(List<String> sortCriteria) {
        if (sortCriteria == null) {
            return emptyList();
        }

        var sorts = sortCriteria.stream()
                .map(HillSearchRequestFactory::parseSort)
                .toList();

        var duplicateSortFields = findDuplicateSortFields(sorts);
        if (!duplicateSortFields.isEmpty()) {
            var duplicateFields = duplicateSortFields.stream()
                    .map(SortField::toString)
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("Duplicate sort criteria found for fields named [%s]".formatted(
                    duplicateFields));
        }

        return sorts;
    }

    private static Sort parseSort(String criterion) {
        Validate.isTrue(criterion.contains("_"), "Sort parameter format is 'fieldName_order'. Found [%s]", criterion);

        var parts = criterion.split("_", 2);
        SortField sortField;
        SortOrder sortOrder;

        try {
            sortField = SortField.valueOf(parts[0].trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("[%s] is not a sortable field".formatted(parts[0]));
        }

        try {
            sortOrder = SortOrder.valueOf(parts[1].trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown sort order [%s]".formatted(parts[1]));
        }

        return new Sort(sortField, sortOrder);
    }

    private static List<SortField> findDuplicateSortFields(List<Sort> sorts) {
        return sorts.stream()
                .collect(groupingBy(Sort::field, counting()))
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .toList();
    }

    private static void validateHeightBracket(
            Double minHeight,
            Double maxHeight
    ) {
        if (minHeight != null && minHeight < 0) {
            throw new IllegalArgumentException("minHeight must be greater than zero; found [%.1f]".formatted(minHeight));
        }
        if (maxHeight != null && maxHeight < 0) {
            throw new IllegalArgumentException("maxHeight must be greater than zero; found [%.1f]".formatted(maxHeight));
        }
        if (minHeight != null && maxHeight != null) {
            Validate.isTrue(minHeight <= maxHeight, "minHeight must be less than or equal to maxHeight");
        }
    }

    private record HillSearchRequestImpl(
            Hill.Category category,
            Double minHeight,
            Double maxHeight,
            List<Sort> sorts,
            int limit,
            String name
    ) implements HillSearchRequest {
    }

}
