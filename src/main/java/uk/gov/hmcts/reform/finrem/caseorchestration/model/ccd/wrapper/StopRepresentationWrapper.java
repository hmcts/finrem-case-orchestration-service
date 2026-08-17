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
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.access.CaseworkerDivorceFinancialremedySolicitorCrudAccess;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StopRepresentationWrapper {

    @CCD(
            label = "Does your client consent to you stopping representation?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedySolicitorCrudAccess.class}
    )
    @TemporaryField
    private YesOrNo stopRepClientConsent;

    @CCD(
            label = "Do you have judicial approval to stop representation?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedySolicitorCrudAccess.class}
    )
    @TemporaryField
    private YesOrNo stopRepJudicialApproval;

    @CCD(label = " ", searchable = false, access = {CaseworkerDivorceFinancialremedySolicitorCrudAccess.class})
    @TemporaryField
    private String clientAddressForServiceLabel;

    @CCD(
            label = "${clientAddressForServiceLabel}",
            searchable = false,
            typeOverride = FieldType.AddressUK,
            access = {CaseworkerDivorceFinancialremedySolicitorCrudAccess.class}
    )
    @TemporaryField
    private Address clientAddressForService;

    @CCD(
            label = "${clientAddressForServiceConfidentialLabel}",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedySolicitorCrudAccess.class}
    )
    @TemporaryField
    private YesOrNo clientAddressForServiceConfidential;

    @CCD(label = " ", access = {CaseworkerDivorceFinancialremedySolicitorCrudAccess.class})
    @TemporaryField
    private String clientAddressForServiceConfidentialLabel;

    // Capturing extra address (possibly applicant/respondent/intervener 1-4)

    @CCD(label = " ", access = {CaseworkerDivorceFinancialremedySolicitorCrudAccess.class})
    @TemporaryField
    private String extraClientAddr1Id;

    @CCD(label = " ", access = {CaseworkerDivorceFinancialremedySolicitorCrudAccess.class})
    @TemporaryField
    private String extraClientAddr1Label;

    @CCD(
            label = "${extraClientAddr1Label}",
            searchable = false,
            typeOverride = FieldType.AddressUK,
            access = {CaseworkerDivorceFinancialremedySolicitorCrudAccess.class}
    )
    @TemporaryField
    private Address extraClientAddr1;

    @CCD(
            label = "${extraClientAddr1ConfidentialLabel}",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedySolicitorCrudAccess.class}
    )
    @TemporaryField
    private YesOrNo extraClientAddr1Confidential;

    @CCD(label = " ", access = {CaseworkerDivorceFinancialremedySolicitorCrudAccess.class})
    @TemporaryField
    private String extraClientAddr1ConfidentialLabel;

    @CCD(label = " ", access = {CaseworkerDivorceFinancialremedySolicitorCrudAccess.class})
    @TemporaryField
    private String extraClientAddr2Id;

    @CCD(label = " ", access = {CaseworkerDivorceFinancialremedySolicitorCrudAccess.class})
    @TemporaryField
    private String extraClientAddr2Label;

    @CCD(
            label = "${extraClientAddr2Label}",
            searchable = false,
            typeOverride = FieldType.AddressUK,
            access = {CaseworkerDivorceFinancialremedySolicitorCrudAccess.class}
    )
    @TemporaryField
    private Address extraClientAddr2;

    @CCD(
            label = "${extraClientAddr2ConfidentialLabel}",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedySolicitorCrudAccess.class}
    )
    @TemporaryField
    private YesOrNo extraClientAddr2Confidential;

    @CCD(label = " ", access = {CaseworkerDivorceFinancialremedySolicitorCrudAccess.class})
    @TemporaryField
    private String extraClientAddr2ConfidentialLabel;

    @CCD(label = " ", access = {CaseworkerDivorceFinancialremedySolicitorCrudAccess.class})
    @TemporaryField
    private String extraClientAddr3Id;

    @CCD(label = " ", access = {CaseworkerDivorceFinancialremedySolicitorCrudAccess.class})
    @TemporaryField
    private String extraClientAddr3Label;

    @CCD(
            label = "${extraClientAddr3Label}",
            searchable = false,
            typeOverride = FieldType.AddressUK,
            access = {CaseworkerDivorceFinancialremedySolicitorCrudAccess.class}
    )
    @TemporaryField
    private Address extraClientAddr3;

    @CCD(
            label = "${extraClientAddr3ConfidentialLabel}",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedySolicitorCrudAccess.class}
    )
    @TemporaryField
    private YesOrNo extraClientAddr3Confidential;

    @CCD(label = " ", access = {CaseworkerDivorceFinancialremedySolicitorCrudAccess.class})
    @TemporaryField
    private String extraClientAddr3ConfidentialLabel;

    @CCD(label = " ", access = {CaseworkerDivorceFinancialremedySolicitorCrudAccess.class})
    @TemporaryField
    private String extraClientAddr4Id;

    @CCD(label = " ", access = {CaseworkerDivorceFinancialremedySolicitorCrudAccess.class})
    @TemporaryField
    private String extraClientAddr4Label;

    @CCD(
            label = "${extraClientAddr4Label}",
            searchable = false,
            typeOverride = FieldType.AddressUK,
            access = {CaseworkerDivorceFinancialremedySolicitorCrudAccess.class}
    )
    @TemporaryField
    private Address extraClientAddr4;

    @CCD(
            label = "${extraClientAddr4ConfidentialLabel}",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedySolicitorCrudAccess.class}
    )
    @TemporaryField
    private YesOrNo extraClientAddr4Confidential;

    @CCD(label = " ", access = {CaseworkerDivorceFinancialremedySolicitorCrudAccess.class})
    @TemporaryField
    private String extraClientAddr4ConfidentialLabel;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerDivorceFinancialremedySolicitorCrudAccess.class}
    )
    @TemporaryField
    private YesOrNo showClientAddressForService;
}
