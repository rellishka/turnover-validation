package com.turnover.validation.adapter.out;

import com.turnover.validation.application.domain.ImportRunStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.YearMonth;

@Getter
@Entity
@Table(name = "import_run")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ImportRunEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Instant startedAt;
    private Instant finishedAt;

    @Enumerated(EnumType.STRING)
    private ImportRunStatus status;

    private YearMonth periodFetched;
    private int entriesImported;
    private String errorMessage;
}
