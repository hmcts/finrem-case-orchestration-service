package uk.gov.hmcts.reform.finrem.caseorchestration.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourtDetails {
    private String courtName;
    private String courtAddress;
    private String phoneNumber;
    private String email;
    private String emailReplyToId;
    private String centralFRCCourtAddress;
    private String centralFRCCourtEmail;

    public CourtDetails(String courtName, String courtAddress, String phoneNumber, String email, String emailReplyToId) {
        this.courtName = courtName;
        this.courtAddress = courtAddress;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.emailReplyToId = emailReplyToId;
    }
}
