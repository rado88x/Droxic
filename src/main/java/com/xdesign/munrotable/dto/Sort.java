package com.xdesign.munrotable.dto;

public record Sort(
    SortField field,
    SortOrder order
) { }
