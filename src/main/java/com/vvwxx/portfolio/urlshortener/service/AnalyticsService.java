package com.vvwxx.portfolio.urlshortener.service;

import com.vvwxx.portfolio.urlshortener.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final StringRedisTemplate redisTemplate;
    private final UrlRepository urlRepository;

    private static final String CLICK_KEY_PREFIX = "click_count:";

    public void trackClick(Long id) {

        String key = CLICK_KEY_PREFIX + id;
        redisTemplate.opsForValue().increment(key);
    }

    @Scheduled(fixedRate = 60000)
    public void syncClickCountsToDb() {
        Set<String> keys = redisTemplate.keys(CLICK_KEY_PREFIX + "*");

        if (keys != null && !keys.isEmpty()) {

            for (String key : keys) {

                String countStr = redisTemplate.opsForValue().getAndDelete(key);

                if (countStr != null) {
                    try{
                        Long countToAdd = Long.parseLong(countStr);
                        Long id = Long.parseLong(key.replace(CLICK_KEY_PREFIX, ""));

                        urlRepository.addClickCount(id, countToAdd);

                        log.info("Synced {} clicks for ID: {}", countToAdd, id);
                    } catch (Exception e) {
                        log.error("Failed to update DB for id. Putting count back to Redis.", e);
                        redisTemplate.opsForValue().increment(key, Long.parseLong(countStr));
                    }
                }

            }
        }

    }
}
