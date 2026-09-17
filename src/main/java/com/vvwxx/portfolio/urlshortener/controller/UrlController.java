package com.vvwxx.portfolio.urlshortener.controller;

import com.vvwxx.portfolio.urlshortener.dto.ShortenRequest;
import com.vvwxx.portfolio.urlshortener.service.UrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/url")
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    @PostMapping("/shorten")
    public String shortenUrl(@RequestBody ShortenRequest request) {

        return urlService.shortenUrl(request.longUrl);
    }

    @GetMapping("/{shortUrl}")
    public ResponseEntity<Void> redirect(@PathVariable String shortUrl) {

        String originalUrl = urlService.getOriginalUrl(shortUrl);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }
}
