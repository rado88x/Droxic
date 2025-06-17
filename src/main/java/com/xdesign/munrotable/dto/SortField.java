package com.xdesign.munrotable.dto;

public enum SortField {
    HEIGHT("height"),
    NAME("name");

    private final String fieldName;

    SortField(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public String toString() {
        return fieldName;
    }
}
