package com.example.demo.service.client.impl;

import com.example.demo.service.domain.MovieDto;
import com.example.demo.service.domain.MovieResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MovieRestClientImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private MovieRestClient movieRestClient;

    private final String apiUrl = "https://example.com/movies?page=";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(movieRestClient, "apiUrl", apiUrl);
    }

    @Test
    void fetchMoviesFromApi_ShouldReturnMovieResponse_WhenApiReturns200() throws Exception {
        int page = 1;
        String jsonResponse = """
                {
                  "Title": "Inception",
                  "Year": "2010",
                  "Rated": "PG-13",
                  "Released": "2010-07-16",
                  "Runtime": "148 min",
                  "Genre": "Action, Adventure, Sci-Fi",
                  "Director": "Christopher Nolan",
                  "Writer": "Christopher Nolan",
                  "Actors": "Leonardo DiCaprio, Joseph Gordon-Levitt, Elliot Page"
                }
                """;
        MovieDto movie = MovieDto.builder()
                .title("Inception")
                .year(2010)
                .rated("PG-13")
                .released("2010-07-16")
                .runtime("148 min")
                .genre("Action, Adventure, Sci-Fi")
                .director("Christopher Nolan")
                .writer("Christopher Nolan")
                .actors("Leonardo DiCaprio, Joseph Gordon-Levitt, Elliot Page")
                .build();
        MovieResponseDto mockResponse = MovieResponseDto.builder()
                .movies(List.of(movie))
                .page(1)
                .total(1)
                .perPage(1)
                .build();

        when(restTemplate.exchange(eq(apiUrl + page), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(jsonResponse, HttpStatus.OK));

        when(objectMapper.readValue(jsonResponse, MovieResponseDto.class)).thenReturn(mockResponse);

        Optional<MovieResponseDto> result = movieRestClient.fetchMoviesFromApi(page);

        Assertions.assertTrue(result.isPresent());
        verify(restTemplate, times(1))
                .exchange(anyString(), any(), any(), eq(String.class));
    }

    @Test
    void fetchMoviesFromApi_ShouldReturnEmptyOptional_WhenApiReturns4xx() {
        int page = 1;
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);

        when(restTemplate.exchange(eq(apiUrl + page), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(exception);

        Optional<MovieResponseDto> result = movieRestClient.fetchMoviesFromApi(page);

        Assertions.assertTrue(result.isEmpty());
        verify(restTemplate, times(1)).exchange(anyString(), any(), any(), eq(String.class));
    }

    @Test
    void fetchMoviesFromApi_ShouldRetry_WhenApiReturns5xx() {
        int page = 1;
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);

        when(restTemplate.exchange(eq(apiUrl + page), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(exception);

        Assertions.assertThrows(ResponseStatusException.class, () -> movieRestClient.fetchMoviesFromApi(page));

        verify(restTemplate, atLeastOnce()).exchange(anyString(), any(), any(), eq(String.class));

    }

    @Test
    void recoverFromApiError_ShouldReturnEmptyOptional_AfterRetriesFail() {
        ResponseStatusException exception = new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Server error");

        Assertions.assertThrows(ResponseStatusException.class, () -> movieRestClient.recoverFromApiError(exception, 1));

    }
}
