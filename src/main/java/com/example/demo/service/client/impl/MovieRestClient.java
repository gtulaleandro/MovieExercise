package com.example.demo.service.client.impl;

import com.example.demo.service.client.CustomClient;
import com.example.demo.service.domain.MovieResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Component
@Slf4j
public class MovieRestClient implements CustomClient {

    @Value("${api.movies.url}")
    private final String apiUrl;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public MovieRestClient(@Value("${api.movies.url}") String apiUrl,
                           RestTemplate restTemplate,
                           ObjectMapper objectMapper) {
        this.apiUrl = apiUrl;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     *
     * This method has configurable retries. We should only retry the api call against the microservice
     * if the error code is 5xx.
     * @param page required page number
     * @return An {@link Optional} with the api call result
     */
    @Retryable(
            value = { ResponseStatusException.class },
            maxAttempts = 2,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    @Override
    public Optional<MovieResponseDto> fetchMoviesFromApi(int page) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl + page, HttpMethod.GET, entity, String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return Optional.of(objectMapper.readValue(response.getBody(), MovieResponseDto.class));
            }
        }catch (HttpStatusCodeException e) {
            if (e.getStatusCode().is5xxServerError()) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Server error fetching movies", e);
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Unexpected error fetching movies", e);
        }
        return Optional.empty();
    }

    /**
     * Recovery method that is executed when all retry attempts have failed.
     * This method is automatically invoked by Spring Retry after exhausting
     * the maximum number of attempts defined in the {@code @Retryable} annotation.
     *
     * @param e    The exception that caused the failure after all retries.
     * @param page The page number of the request that failed.
     * @return An {@link Optional} empty instance, indicating that the API response could not be retrieved.
     */
    @Recover
    public Optional<MovieResponseDto> recoverFromApiError(ResponseStatusException e, int page) {
        log.error("Recover. Error fetching movies from API", e);
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Couldn't recover. Server error fetching movies", e);
    }
}
