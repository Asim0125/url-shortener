package com.vvwxx.portfolio.urlshortener.service;

import com.vvwxx.portfolio.urlshortener.entity.Url;
import com.vvwxx.portfolio.urlshortener.repository.UrlRepository;
import com.vvwxx.portfolio.urlshortener.util.Base62Convert;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;
    private final StringRedisTemplate redisTemplate;
    private final AnalyticsService analyticsService;

    public String shortenUrl(String originalUrl) {

        Url url = new Url();
        url.setLongUrl(originalUrl);
        Url saveUrl = urlRepository.save(url);

        return Base62Convert.encode(saveUrl.getId());
    }

    public String getOriginalUrl(String shortUrl) {

        try {
            long id = Base62Convert.decode(shortUrl);
            analyticsService.trackClick(id);

            String cachedUrl = redisTemplate.opsForValue().get(shortUrl);

            if (cachedUrl != null) {
                System.out.println("Hit from cache redis");
                return cachedUrl;
            }

            System.out.println("Hit from db");
            Url data = urlRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Url not found for code: " + shortUrl));

            String originalUrl = data.getLongUrl();
            redisTemplate.opsForValue().set(shortUrl, originalUrl, 24, TimeUnit.HOURS);

            return originalUrl;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
