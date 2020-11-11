package uk.gov.hmcts.reform.finrem.caseorchestration.error;


public class CourtDetailsParseException extends RuntimeException {

    private static final String message = "Failed to parse court details.";

    public CourtDetailsParseException() {
        super(message);
    }
}

