package com.vvwxx.portfolio.urlshortener;

import com.vvwxx.portfolio.urlshortener.entity.Url;
import com.vvwxx.portfolio.urlshortener.repository.UrlRepository;
import com.vvwxx.portfolio.urlshortener.service.AnalyticsService;
import com.vvwxx.portfolio.urlshortener.service.UrlService;
import com.vvwxx.portfolio.urlshortener.util.Base62Convert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    @Mock
    private UrlRepository urlRepository;

    @Mock
    StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private AnalyticsService analyticsService;

    @InjectMocks
    private UrlService urlService;

    // Helper data
    private final String LONG_URL = "https://www.google.com/";
    private final String SHORT_CODE = "ab1";
    private final Long ID = Base62Convert.decode(SHORT_CODE);

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("Shorten URL: Should save to DB and return Base62 code")
    void testShortenUrl_Success() {

        // setup mock
        Url saveUrl = new Url();
        saveUrl.setId(ID);
        saveUrl.setLongUrl(LONG_URL);

        when(urlRepository.save(any(Url.class))).thenReturn(saveUrl);

        // action
        String result = urlService.shortenUrl(LONG_URL);

        // assertion
        Assertions.assertEquals(SHORT_CODE, result);

        verify(urlRepository, times(1)).save(any(Url.class));
    }

    @Test
    @DisplayName("Get Original URL: Should return from Redis if available (Cache Hit)")
    void testGetOriginalUrl_CacheHit() {

        // setup mock redis
        when(valueOperations.get(SHORT_CODE)).thenReturn(LONG_URL);

        // action
        String result = urlService.getOriginalUrl(SHORT_CODE);

        // assertion
        Assertions.assertEquals(LONG_URL, result);

        verify(analyticsService, times(1)).trackClick(ID);
        verify(urlRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Get Original URL: Should return from DB if Redis empty (Cache Miss)")
    void testGerOriginalUrl_CacheMiss_DbHit() {

        // setup mock redis null
        when(valueOperations.get(SHORT_CODE)).thenReturn(null);

        // setup mock from db
        Url urlFromDb = new Url();
        urlFromDb.setId(ID);
        urlFromDb.setLongUrl(LONG_URL);
        when(urlRepository.findById(ID)).thenReturn(Optional.of(urlFromDb));

        // action
        String result = urlService.getOriginalUrl(SHORT_CODE);

        // assertion
        Assertions.assertEquals(LONG_URL, result);

        verify(analyticsService, times(1)).trackClick(ID);
        verify(urlRepository, times(1)).findById(ID);

        // verify after get data from db and save data to redis
        verify(valueOperations, times(1)).set(eq(SHORT_CODE), eq(LONG_URL), anyLong(), any(TimeUnit.class));
    }

    @Test
    @DisplayName("Get Original URL: Should throw Exception if Short Code Invalid")
    void testGetOriginalUrl_InvalidCode() {

        String invalidCode = "&%abw1";

        // assertion
        Assertions.assertThrows(RuntimeException.class, () -> urlService.getOriginalUrl(invalidCode));

        verify(analyticsService, never()).trackClick(anyLong());
        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    @DisplayName("Get Original URL: Should throw Exception if Not Found in DB")
    void testGetOriginalUrl_NotFoundInDb() {

        // setup mock
        when(valueOperations.get(SHORT_CODE)).thenReturn(null);
        when(urlRepository.findById(ID)).thenReturn(Optional.empty());

        // assertion
        Assertions.assertThrows(RuntimeException.class, () -> urlService.getOriginalUrl(SHORT_CODE));
    }
}
