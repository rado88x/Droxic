package com.xdesign.munrotable.dto;

import com.xdesign.munrotable.model.Hill;

import java.util.List;

public interface HillSearchRequest {

    Hill.Category category();
    Double minHeight();
    Double maxHeight();
    List<Sort> sorts();
    int limit();
    String name();
}
