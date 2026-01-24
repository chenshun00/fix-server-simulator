package com.fixsimulator.repository;

import com.fixsimulator.entity.FixMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FixMessageRepository extends JpaRepository<FixMessageEntity, Long> {
    
    // 根据sessionKey查找消息
    List<FixMessageEntity> findBySessionKey(String sessionKey);
    
    // 根据消息方向查找
    List<FixMessageEntity> findByDirection(FixMessageEntity.MessageDirection direction);
    
    // 根据消息类型查找
    List<FixMessageEntity> findByMsgType(String msgType);
    
    // 根据接收时间范围查找
    List<FixMessageEntity> findByReceiveTimeBetween(java.time.LocalDateTime startTime, java.time.LocalDateTime endTime);
    
    // 自定义查询：按会话和消息类型查找
    @Query("SELECT f FROM FixMessageEntity f WHERE f.sessionKey = :sessionKey AND f.msgType = :msgType")
    List<FixMessageEntity> findBySessionKeyAndMsgType(@Param("sessionKey") String sessionKey, @Param("msgType") String msgType);
}