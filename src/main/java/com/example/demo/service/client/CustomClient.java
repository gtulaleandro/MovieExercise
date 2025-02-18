package com.example.demo.service.client;

import com.example.demo.service.domain.MovieResponseDto;

import java.util.Optional;

public interface CustomClient {

    Optional<MovieResponseDto> fetchMoviesFromApi(int page);
}
