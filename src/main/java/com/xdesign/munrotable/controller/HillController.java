package com.xdesign.munrotable.controller;

import com.xdesign.munrotable.model.Hill;
import com.xdesign.munrotable.service.HillSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.xdesign.munrotable.dto.HillSearchRequestFactory.newRequest;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class HillController {

    private static final Logger log = LoggerFactory.getLogger(HillController.class);

    private final HillSearchService hillSearchService;

    public HillController(HillSearchService hillSearchService) {
        this.hillSearchService = hillSearchService;
    }

    @GetMapping(path = "/hills")
    public List<Hill> findHills(
        @RequestParam(name = "category", required = false) String category,
        @RequestParam(name = "minHeight", required = false) Double minHeight,
        @RequestParam(name = "maxHeight", required = false) Double maxHeight,
        @RequestParam(name = "sort", required = false) List<String> sortCriteria,
        @RequestParam(name = "limit", defaultValue = "1000") int limit
    ) {
        var request = newRequest(category, minHeight, maxHeight, sortCriteria, limit);
        return hillSearchService.searchHills(request);
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<String> handleExpectedErrors(Exception ex) {
        return ResponseEntity
            .badRequest()
            .body(ex.getMessage());
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<String> handleUnexpectedErrors(Throwable t) {
        log.error("Unexpected error", t);

        return ResponseEntity
            .internalServerError()
            .body("");
    }
}
