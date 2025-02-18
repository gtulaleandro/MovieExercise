package com.example.demo.controller;

import com.example.demo.service.MovieService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(MovieController.class)
public class DirectorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MovieService movieService;

    @InjectMocks
    private MovieController directorController;

    @Test
    public void getDirectors_ShouldReturnDirectorsList() throws Exception {
        when(movieService.getDirectors(5)).thenReturn(Arrays.asList("Director1", "Director2"));

        mockMvc.perform(get("/api/directors").param("threshold", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.directors[0]").value("Director1"))
                .andExpect(jsonPath("$.directors[1]").value("Director2"));
    }

    @Test
    public void getDirectors_ShouldReturnBadRequest_WhenThresholdIsMissing() throws Exception {
        mockMvc.perform(get("/api/directors").param("threshold", ""))
                .andExpect(status().isInternalServerError());
    }

}
