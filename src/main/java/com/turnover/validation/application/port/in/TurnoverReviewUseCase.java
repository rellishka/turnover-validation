package com.turnover.validation.application.port.in;

import com.turnover.validation.application.domain.Turnover;
import com.turnover.validation.exception.TurnoverNotFoundException;

import java.math.BigDecimal;
import java.util.List;

/**
 * Inbound use case for the Asset Manager review workflow.
 *
 * <p>When import flags a turnover for significant deviation, an Asset Manager
 * contacts the tenant and then either corrects the figure or accepts it as
 * reported. Resolving a turnover also closes its open validation issue, which is
 * what makes the resulting figures trustworthy for reporting.
 */
public interface TurnoverReviewUseCase {

    /**
     * List turnover currently flagged for review (status {@code FLAGGED}).
     *
     * @return the flagged turnover awaiting an Asset Manager decision
     */
    List<Turnover> getFlaggedTurnovers();

    /**
     * Correct a flagged turnover's amount after confirming the right figure with
     * the tenant. Sets the turnover to {@code CORRECTED} and resolves its open
     * validation issue.
     *
     * @param turnoverId      the turnover to correct
     * @param correctedAmount the agreed correct amount
     * @param reason          why the figure was corrected (recorded on the resolved issue)
     * @param resolvedBy      the Asset Manager performing the correction
     * @return the corrected turnover
     * @throws TurnoverNotFoundException if no turnover exists for {@code turnoverId}
     */
    Turnover correctTurnover(Long turnoverId, BigDecimal correctedAmount, String reason, String resolvedBy);

    /**
     * Accept a flagged turnover as correct-as-reported after confirming with the
     * tenant. Sets the turnover to {@code ACCEPTED} and resolves its open
     * validation issue.
     *
     * @param turnoverId the turnover to accept
     * @param reason     why the figure was accepted (recorded on the resolved issue)
     * @param resolvedBy the Asset Manager accepting the figure
     * @return the accepted turnover
     * @throws TurnoverNotFoundException if no turnover exists for {@code turnoverId}
     */
    Turnover acceptTurnover(Long turnoverId, String reason, String resolvedBy);
}
