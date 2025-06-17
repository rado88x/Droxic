package com.xdesign.munrotable;

import com.xdesign.munrotable.exception.CsvFileLoadingException;
import com.xdesign.munrotable.service.HillSearchService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;

@SpringBootApplication
public class HillSearchApplication {

    @Value("${hill-search.munro.file.name}")
    private String munroDataFileName;

    @Bean
    public HillSearchService hillSearchService() {
        return new HillSearchService(csvFile());
    }

    @Bean
    public File csvFile() {
        try {
            var munroData = new ClassPathResource(munroDataFileName);
            return munroData.getFile();
        } catch (IOException ex) {
            throw new CsvFileLoadingException(ex);
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(HillSearchApplication.class, args);
    }
}
