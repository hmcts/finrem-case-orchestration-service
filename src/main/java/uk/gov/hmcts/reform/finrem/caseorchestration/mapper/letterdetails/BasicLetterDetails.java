package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.FrcCourtDetails;
import uk.gov.hmcts.reform.finrem.ccd.domain.Address;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BasicLetterDetails {
    private String applicantName;
    private String respondentName;
    private String reference;
    private Address applicantAddress;
    private Address respondentAddress;
    private String caseNumber;
    private String letterDate;
    private FrcCourtDetails courtDetails;
    private Addressee addressee;
    private CtscContactDetails ctscContactDetails;
    private String divorceCaseNumber;
    private String courtContactDetails;
}
