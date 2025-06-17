package com.xdesign.munrotable.model;

public record Hill(
    String name,
    Double height,
    String gridReference,
    Category category
) {
    public enum Category {
        MUNRO,
        TOP
    }
}
