package com.example.demo.controller;

import com.example.demo.controller.domain.DirectorResponse;
import com.example.demo.exceptions.InvalidParamException;
import com.example.demo.service.MovieService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @GetMapping("/directors")
    public DirectorResponse getDirectors(@RequestParam(required = true) Integer threshold) {
        if (threshold == null) {
            throw new InvalidParamException("threshold must not be null");
        }
        return new DirectorResponse(movieService.getDirectors(threshold));
    }


}
