package com.example.demo.service.impl;

import com.example.demo.service.MovieService;
import com.example.demo.service.client.CustomClient;
import com.example.demo.service.domain.MovieDto;
import com.example.demo.service.domain.MovieResponseDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class MovieServiceImpl implements MovieService {

    private final CustomClient customClient;

    @Override
    public List<String> getDirectors(int threshold) {
        Map<String, Integer> directorCount = new HashMap<>();
        int page = 1;
        int totalPages;

        do {
            Optional<MovieResponseDto> moviesFromApi = customClient.fetchMoviesFromApi(page);
            if (moviesFromApi.isEmpty()) {
                return List.of();
            }
            MovieResponseDto response = moviesFromApi.get();

            totalPages = response.getTotalPages();
            List<MovieDto> movies = response.getMovies();

            for (MovieDto movie : movies) {
                String director = movie.getDirector();
                directorCount.put(director, directorCount.getOrDefault(director, 0) + 1);
            }

            page++;
        } while (page <= totalPages);

        return directorCount.entrySet().stream()
                .filter(entry -> entry.getValue() > threshold)
                .map(Map.Entry::getKey)
                .sorted()
                .collect(Collectors.toList());
    }
}
