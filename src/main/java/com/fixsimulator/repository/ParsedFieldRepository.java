package com.fixsimulator.repository;

import com.fixsimulator.entity.ParsedFieldEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParsedFieldRepository extends JpaRepository<ParsedFieldEntity, Long> {
    
    // 根据消息ID查找
    List<ParsedFieldEntity> findByMessageId(Long messageId);
    
    // 根据ClOrdId查找
    List<ParsedFieldEntity> findByClOrdId(String clOrdId);
    
    // 根据Symbol查找
    List<ParsedFieldEntity> findBySymbol(String symbol);
    
    // 根据消息类型查找
    List<ParsedFieldEntity> findByMsgType(String msgType);
    
    // 根据创建时间范围查找
    List<ParsedFieldEntity> findByCreatedAtBetween(java.time.LocalDateTime startTime, java.time.LocalDateTime endTime);

    // 根据创建时间之后的数据查找
    List<ParsedFieldEntity> findByCreatedAtAfter(java.time.LocalDateTime dateTime);
    
    // 自定义查询：按ClOrdId和Symbol查找
    @Query("SELECT p FROM ParsedFieldEntity p WHERE p.clOrdId = :clOrdId OR p.symbol = :symbol")
    List<ParsedFieldEntity> findByClOrdIdOrSymbol(@Param("clOrdId") String clOrdId, @Param("symbol") String symbol);
}