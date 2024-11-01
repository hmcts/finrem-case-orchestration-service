package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import lombok.Data;

@Data
public class CourtDetails {
    private String courtName;
    private String courtAddress;
    private String phoneNumber;
    private String email;
}
