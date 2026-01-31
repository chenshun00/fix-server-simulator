package com.fixsimulator.domain.message;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "parsed_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParsedMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "msg_type", nullable = false)
    private MsgType msgType;

    @Column(name = "symbol", nullable = false)
    private String symbol;

    @Column(name = "cl_ord_id")
    private String clOrdId;

    @Column(name = "orig_cl_ord_id")
    private String origClOrdId;

    @Column(name = "price", precision = 18, scale = 4)
    private BigDecimal price;

    @Column(name = "order_qty", precision = 18, scale = 4)
    private BigDecimal orderQty;

    @Column(name = "side")
    private String side;

    @Column(name = "ord_type")
    private String ordType;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Column(name = "message", length = 2000)
    private String message;

    @PrePersist
    protected void onCreate() {
        receivedAt = LocalDateTime.now();
    }
}
