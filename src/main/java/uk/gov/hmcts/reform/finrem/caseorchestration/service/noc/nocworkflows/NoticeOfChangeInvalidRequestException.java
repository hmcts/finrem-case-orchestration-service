package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows;

import java.io.Serial;

/**
 * Thrown when request data to process a Notice of Change is invalid.
 */
public class NoticeOfChangeInvalidRequestException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public NoticeOfChangeInvalidRequestException(String message) {
        super(message);
    }
}
