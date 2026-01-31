package com.fixsimulator.domain.message;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParsedMessageRepository extends JpaRepository<ParsedMessage, Long> {

    @Query("SELECT m FROM ParsedMessage m WHERE " +
           "(:symbol IS NULL OR m.symbol = :symbol) AND " +
           "(:clOrdId IS NULL OR m.clOrdId = :clOrdId)")
    Page<ParsedMessage> searchMessages(
        @Param("symbol") String symbol,
        @Param("clOrdId") String clOrdId,
        Pageable pageable
    );
}
