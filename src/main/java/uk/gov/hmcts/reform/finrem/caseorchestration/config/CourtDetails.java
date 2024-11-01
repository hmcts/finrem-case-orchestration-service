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
}
