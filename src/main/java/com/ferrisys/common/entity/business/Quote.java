package com.ferrisys.common.entity.business;

import com.ferrisys.common.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "bus_quote")
public class Quote extends Auditable implements Serializable {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column
    private String description;

    @Column(name = "quote_date")
    private LocalDate date;

    @Column
    private BigDecimal total;

    @Builder.Default
    @OneToMany(mappedBy = "quote", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuoteDetail> details = new ArrayList<>();

    @Column(nullable = false)
    private Integer status;
}
