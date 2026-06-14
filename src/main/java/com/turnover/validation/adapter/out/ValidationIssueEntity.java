package com.turnover.validation.adapter.out;

import com.turnover.validation.application.domain.ValidationIssueStatus;
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

import java.time.Instant;

@Getter
@Entity
@Table(name = "validation_issue")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ValidationIssueEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private TurnoverEntity turnover;

    private String rule;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    private ValidationIssueStatus status;

    private String resolution;
    private String resolvedBy;
    private Instant resolvedAt;
}
