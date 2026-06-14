package com.turnover.validation.adapter.out;

import com.turnover.validation.application.domain.TurnoverStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;

@Getter
@Entity
@Table(name = "turnover")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TurnoverEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private LeaseEntity lease;

    @ManyToOne(optional = false)
    private ImportRunEntity importRun;

    private YearMonth period;

    @Column(precision = 19, scale = 2)
    private BigDecimal amount;

    private String currency;

    @Enumerated(EnumType.STRING)
    private TurnoverStatus status;

    private Instant submittedAt;
}
