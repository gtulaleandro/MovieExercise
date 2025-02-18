package com.example.demo.service.impl;

import com.example.demo.service.client.CustomClient;
import com.example.demo.service.domain.MovieDto;
import com.example.demo.service.domain.MovieResponseDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MovieServiceImplTest {

    @Mock
    private CustomClient customClient;

    @InjectMocks
    private MovieServiceImpl movieService;

    @Test
    public void fetchAllMovies_ifNotExists_shouldReturnEmptyList() {

        when(customClient.fetchMoviesFromApi(1)).thenReturn(Optional.empty());
        List<String> result = movieService.getDirectors(1);
        Assertions.assertTrue(result.isEmpty());

    }

    @Test
    public void fetchAllMovies_ifExists_shouldReturnMovies() {

        MovieDto movieDataOne = MovieDto.builder().director("woody allen").build();
        MovieDto movieDataTwo = MovieDto.builder().director("woody allen").build();
        MovieDto movieDataThree = MovieDto.builder().director("vincent").build();
        MovieResponseDto movie = MovieResponseDto.builder()
                .movies(List.of(movieDataOne, movieDataTwo, movieDataThree))
                .build();
        when(customClient.fetchMoviesFromApi(1)).thenReturn(Optional.of(movie));
        List<String> result = movieService.getDirectors(1);
        Assertions.assertEquals(1, result.size());
        Assertions.assertTrue(result.contains("woody allen"));

    }
}
