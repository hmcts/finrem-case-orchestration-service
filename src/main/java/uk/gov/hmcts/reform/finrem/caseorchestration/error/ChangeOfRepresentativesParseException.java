package uk.gov.hmcts.reform.finrem.caseorchestration.error;

public class ChangeOfRepresentativesParseException extends RuntimeException{

    private static final String message = "Failed to parse Change of representatives";

    public ChangeOfRepresentativesParseException() {
        super(message);
    }
}
