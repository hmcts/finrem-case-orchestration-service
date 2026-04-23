package uk.gov.hmcts.reform.finrem.caseorchestration.service;

public class UserNotFoundInOrganisationApiException extends Exception {

    public UserNotFoundInOrganisationApiException() {
        super();
    }

    public UserNotFoundInOrganisationApiException(String message) {
        super(message);
    }
}
