package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.TemporaryField;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Address;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StopRepresentationWrapper {

    @TemporaryField
    private YesOrNo stopRepClientConsent;

    @TemporaryField
    private YesOrNo stopRepJudicialApproval;

    @TemporaryField
    private String clientAddressForServiceLabel;

    @TemporaryField
    private Address clientAddressForService;

    @TemporaryField
    private YesOrNo clientAddressForServiceConfidential;

    @TemporaryField
    private String clientAddressForServiceConfidentialLabel;

    // Capturing extra address (possibly applicant/respondent/intervener 1-4)

    @TemporaryField
    private String extraClientAddr1Id;

    @TemporaryField
    private String extraClientAddr1Label;

    @TemporaryField
    private Address extraClientAddr1;

    @TemporaryField
    private YesOrNo extraClientAddr1Confidential;

    @TemporaryField
    private String extraClientAddr1ConfidentialLabel;

    @TemporaryField
    private String extraClientAddr2Id;

    @TemporaryField
    private String extraClientAddr2Label;

    @TemporaryField
    private Address extraClientAddr2;

    @TemporaryField
    private YesOrNo extraClientAddr2Confidential;

    @TemporaryField
    private String extraClientAddr2ConfidentialLabel;

    @TemporaryField
    private String extraClientAddr3Id;

    @TemporaryField
    private String extraClientAddr3Label;

    @TemporaryField
    private Address extraClientAddr3;

    @TemporaryField
    private YesOrNo extraClientAddr3Confidential;

    @TemporaryField
    private String extraClientAddr3ConfidentialLabel;

    @TemporaryField
    private String extraClientAddr4Id;

    @TemporaryField
    private String extraClientAddr4Label;

    @TemporaryField
    private Address extraClientAddr4;

    @TemporaryField
    private YesOrNo extraClientAddr4Confidential;

    @TemporaryField
    private String extraClientAddr4ConfidentialLabel;

    @TemporaryField
    private YesOrNo showClientAddressForService;
}
