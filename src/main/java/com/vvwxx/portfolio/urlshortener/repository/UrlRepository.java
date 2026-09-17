package com.vvwxx.portfolio.urlshortener.repository;

import com.vvwxx.portfolio.urlshortener.entity.Url;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {

    @Modifying
    @Query("UPDATE Url u SET u.clickCount = u.clickCount + 1 WHERE u.id = :id")
    void incrementClickCount(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Url u SET u.clickCount = u.clickCount + :count WHERE u.id = :id")
    void addClickCount(@Param("id") Long id, @Param("count") Long count);
}
