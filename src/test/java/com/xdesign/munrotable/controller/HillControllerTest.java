package com.xdesign.munrotable.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.nio.file.Files;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class HillControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setup(WebApplicationContext wac) {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    void should_return_all_munros_and_tops() throws Exception {
        mockMvc.perform(get("/hills")
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(content().json(readJsonFile("all-munros-and-tops.json"), true));
    }

    @Nested
//    @Disabled("Until filtering is implemented")
    class FilteringTest {

        @Test
        void should_return_only_category_top() throws Exception {
            mockMvc.perform(get("/hills?category=Top")
                    .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json(readJsonFile("tops-only.json"), true));
        }

        @Test
        void should_return_category_top_1200_metres_high_or_more() throws Exception {
            mockMvc.perform(get("/hills?category=Top&minHeight=1200")
                    .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json(readJsonFile("tops-greater-than-or-equal-to-1200-metres.json"), true));
        }

        @Test
        void should_return_category_munro_920_metres_or_less() throws Exception {
            mockMvc.perform(get("/hills?category=Munro&maxHeight=920")
                    .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json(readJsonFile("munros-920-metres-high-or-less.json"), true));
        }

        @Test
        void should_return_category_none_exactly_1000_metres_high() throws Exception {
            mockMvc.perform(get("/hills?minHeight=1000&maxHeight=1000")
                    .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json(readJsonFile("all-summits-exactly-1000-metres-high.json"), true));
        }
    }

    @Nested
//    @Disabled("Until all steps implemented")
    class AllParamsTest {

        @Test
        void should_return_five_highest_munros_ordered_height_desc() throws Exception {
            mockMvc.perform(get("/hills?category=Munro&sort=height_desc&limit=5")
                    .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json(readJsonFile("five-highest-munros-ordered-height-desc.json"), true));
        }

        @Test
        void should_return_first_five_munros_in_alphabetical_order_with_identically_named_munros_ordered_height_desc() throws Exception {
            mockMvc.perform(get("/hills?category=Munro&sort=name_asc&sort=height_desc&limit=5")
                    .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json(readJsonFile("first-five-munros-ordered-by-name-asc-and-height-desc.json"), true));
        }

        @Test
        void should_return_category_top_1200_metres_high_or_more_ordered_height_desc() throws Exception {
            mockMvc.perform(get("/hills?category=Top&minHeight=1200&sort=height_desc")
                    .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json(readJsonFile("tops-1200-metres-high-or-more-ordered-height-desc.json"), true));
        }

        @Test
        void should_return_category_munro_920_metres_high_or_less_ordered_height_asc() throws Exception {
            mockMvc.perform(get("/hills?category=Munro&maxHeight=920&sort=height_asc")
                    .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json(readJsonFile("munro-920-metres-high-or-less-ordered-height-asc.json"), true));
        }
    }

    private String readJsonFile(String path) throws IOException {
        var resource = new ClassPathResource(path);
        return Files.readString(resource.getFile().toPath(), UTF_8);
    }

}
