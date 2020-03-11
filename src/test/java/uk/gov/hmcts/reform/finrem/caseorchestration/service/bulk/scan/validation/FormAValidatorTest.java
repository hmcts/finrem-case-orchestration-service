package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulk.scan.validation;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;
import uk.gov.hmcts.reform.bsp.common.model.validation.out.OcrValidationResult;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation.FormAValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.bsp.common.model.validation.out.ValidationStatus.SUCCESS;
import static uk.gov.hmcts.reform.bsp.common.model.validation.out.ValidationStatus.WARNINGS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.APPLICANT_FULL_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.APPLICANT_INTENDS_TO;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.APPLICANT_REPRESENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.APPLYING_FOR_CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.CHILD_SUPPORT_AGENCY_CALCULATION_MADE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.DISCHARGE_PERIODICAL_PAYMENT_SUBSTITUTE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.DIVORCE_STAGE_REACHED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.HWF_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.NATURE_OF_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.ORDER_FOR_CHILDREN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.ORDER_FOR_CHILDREN_NO_AGREEMENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.PROVISION_MADE_FOR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.RESPONDENT_FULL_NAME;

public class FormAValidatorTest {

    private final FormAValidator formAValidator = new FormAValidator();
    private List<OcrDataField> mandatoryFieldsWithValues;
    private List<String> mandatoryFields;

    @Before
    public void setup() {
        mandatoryFieldsWithValues = new ArrayList<>(asList(
            new OcrDataField(DIVORCE_CASE_NUMBER, "1234567890"),
            new OcrDataField(APPLICANT_FULL_NAME, "Peter Griffin"),
            new OcrDataField(RESPONDENT_FULL_NAME, "Louis Griffin"),
            new OcrDataField(PROVISION_MADE_FOR, "in connection with matrimonial or civil partnership proceedings"),
            new OcrDataField(NATURE_OF_APPLICATION, "Periodical Payment Order, Pension Attachment Order"),
            new OcrDataField(APPLICANT_INTENDS_TO, "ApplyToCourtFor"),
            new OcrDataField(APPLYING_FOR_CONSENT_ORDER, "Yes"),
            new OcrDataField(DIVORCE_STAGE_REACHED, "Decree Nisi"),
            new OcrDataField(APPLICANT_REPRESENTED, "I am not represented by a solicitor in these proceedings")
        ));

        mandatoryFields = mandatoryFieldsWithValues.stream().map(OcrDataField::getName).collect(Collectors.toList());
    }

    @Test
    public void shouldPassValidationForValidMandatoryAndOptionalFields() {
        List<OcrDataField> optionalFieldsWithValues = asList(
            new OcrDataField(HWF_NUMBER, "123456"),
            new OcrDataField(DISCHARGE_PERIODICAL_PAYMENT_SUBSTITUTE, "a lump sum order, a pension compensation sharing order"),
            new OcrDataField("ApplicantSolicitorAddressLine1", "20 Solicitors Road"),
            new OcrDataField("ApplicantSolicitorAddressTown", "Soltown"),
            new OcrDataField("ApplicantSolicitorAddressCounty", "East Midlands"),
            new OcrDataField("ApplicantSolicitorAddressPostcode", "GL51 0EX"),
            new OcrDataField("AddressofProperties", "26 Westminster Avenue"),
            new OcrDataField("MortgageDetails", "We paid for the house with our mortgage which we split"),
            new OcrDataField("OrderForChildren", "there is no agreement, but the applicant is applying for payments"),
            new OcrDataField("OrderForChildrenNoAgreement",
                "in addition to child support maintenance already paid under a Child Support Agency assessment"),
            new OcrDataField("ChildSupportAgencyCalculationMade", "Yes"),
            new OcrDataField("ChildSupportAgencyCalculationReason", "Various reasons why I'm making this application")
        );
        List<OcrDataField> ocrDataFields = new ArrayList<>(mandatoryFieldsWithValues);
        ocrDataFields.addAll(optionalFieldsWithValues);
        OcrValidationResult validationResult = formAValidator.validateBulkScanForm(ocrDataFields);

        assertThat(validationResult.getStatus(), is(SUCCESS));
        assertThat(validationResult.getWarnings(), is(emptyList()));
        assertThat(validationResult.getErrors(), is(emptyList()));
    }

    @Test
    public void shouldFailValidationWhenMandatoryFieldsAreMissing() {
        OcrValidationResult validationResult = formAValidator.validateBulkScanForm(emptyList());

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getErrors(), is(emptyList()));
        assertThat(validationResult.getWarnings(), warningMessagesForMissingOrEmptyFields());
    }

    @Test
    public void shouldFailValidationWhenMandatoryFieldsArePresentButEmpty() {
        OcrValidationResult validationResult = formAValidator.validateBulkScanForm(
            mandatoryFields.stream()
                .map(emptyValueOcrDataField)
                .collect(Collectors.toList()));

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getErrors(), is(emptyList()));
        assertThat(validationResult.getWarnings(), warningMessagesForMissingOrEmptyFields());
    }

    @Test
    public void shouldPassForNonMandatoryEmptyFields() {
        List<OcrDataField> nonMandatoryFieldsWithEmptyValues = asList(
            new OcrDataField(HWF_NUMBER, ""),
            new OcrDataField("ApplicantSolicitorAddressLine1", ""),
            new OcrDataField("ApplicantSolicitorAddressTown", ""),
            new OcrDataField("ApplicantSolicitorAddressCounty", ""),
            new OcrDataField("ApplicantSolicitorAddressPostcode", ""),
            new OcrDataField("AddressofProperties", ""),
            new OcrDataField("MortgageDetails", ""),
            new OcrDataField("OrderForChildren", ""),
            new OcrDataField("OrderForChildrenNoAgreement", ""),
            new OcrDataField("ChildSupportAgencyCalculationMade", ""),
            new OcrDataField("ChildSupportAgencyCalculationReason", "")
        );

        List<OcrDataField> ocrDataFields = new ArrayList<>(mandatoryFieldsWithValues);
        ocrDataFields.addAll(nonMandatoryFieldsWithEmptyValues);
        OcrValidationResult validationResult = formAValidator.validateBulkScanForm(ocrDataFields);
        assertThat(validationResult.getStatus(), is(SUCCESS));
        assertThat(validationResult.getWarnings(), is(emptyList()));
        assertThat(validationResult.getErrors(), is(emptyList()));
    }

    @Test
    public void shouldFailFieldsHavingInvalidValues() {
        OcrValidationResult validationResult = formAValidator.validateBulkScanForm(asList(
            new OcrDataField(HWF_NUMBER, "12345"),
            new OcrDataField(APPLICANT_FULL_NAME, "Peter"),
            new OcrDataField(RESPONDENT_FULL_NAME, "Louis"),
            new OcrDataField(PROVISION_MADE_FOR, "Onions"),
            new OcrDataField(NATURE_OF_APPLICATION, "Mountains, Forest"),
            new OcrDataField(APPLICANT_INTENDS_TO, "have breakfast"),
            new OcrDataField(DISCHARGE_PERIODICAL_PAYMENT_SUBSTITUTE, "house with pool"),
            new OcrDataField(APPLYING_FOR_CONSENT_ORDER, "No"),
            new OcrDataField(DIVORCE_STAGE_REACHED, "The cree"),
            new OcrDataField(APPLICANT_REPRESENTED, "It's wrong!"),
            new OcrDataField(ORDER_FOR_CHILDREN, "Not a valid order for children"),
            new OcrDataField(ORDER_FOR_CHILDREN_NO_AGREEMENT, "Not a valid reason for no agreement"),
            new OcrDataField(CHILD_SUPPORT_AGENCY_CALCULATION_MADE, "Decision not yet made")
        ));

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getErrors(), is(emptyList()));
        assertThat(validationResult.getWarnings(), hasItems(
            mandatoryFieldIsMissing.apply(DIVORCE_CASE_NUMBER),
            "HWFNumber is usually 6 digits",
            mustHaveAtLeastTwoNames(APPLICANT_FULL_NAME),
            mustHaveAtLeastTwoNames(RESPONDENT_FULL_NAME),
            mustBeOneOf(PROVISION_MADE_FOR,
                "in connection with matrimonial or civil partnership proceedings",
                "under paragraphs 1 or 2 of Schedule 1 to the Children Act 1989"),

            containsValueThatIsNotAccepted(NATURE_OF_APPLICATION),
            mustBeOneOf(APPLICANT_INTENDS_TO,
                "ApplyToCourtFor",
                "ProceedWithApplication",
                "ApplyToVary",
                "ApplyToDischargePeriodicalPaymentOrder"),
            containsValueThatIsNotAccepted(DISCHARGE_PERIODICAL_PAYMENT_SUBSTITUTE),
            APPLYING_FOR_CONSENT_ORDER + " only accepts value of \"Yes\"",
            mustBeOneOf(DIVORCE_STAGE_REACHED, "Decree Nisi", "Decree Absolute"),
            mustBeOneOf(APPLICANT_REPRESENTED,
                "I am not represented by a solicitor in these proceedings",
                "I am not represented by a solicitor in these proceedings but am receiving advice from a solicitor",
                "I am represented by a solicitor in these proceedings, who has signed Section 5"
                    + " and all documents for my attention should be sent to my solicitor whose details are as follows"),
            mustBeOneOf(ORDER_FOR_CHILDREN,
                "there is a written agreement made before 5 April 1993 about maintenance for the benefit of children",
                "there is a written agreement made on or after 5 April 1993 about maintenance for the benefit of children",
                "there is no agreement, but the applicant is applying for payments"),
            mustBeOneOf(ORDER_FOR_CHILDREN_NO_AGREEMENT,
                "for a stepchild or stepchildren",
                "in addition to child support maintenance already paid under a Child Support Agency assessment",
                "to meet expenses arising from a child’s disability",
                "to meet expenses incurred by a child in being educated or training for work",
                "when either the child or the person with care of the child "
                    + "or the absent parent of the child is not habitually resident in the United Kingdom"),
            CHILD_SUPPORT_AGENCY_CALCULATION_MADE + " must be \"Yes\", \"No\" or left blank"
        ));
    }

    @Test
    public void shouldPassValidateForValuesWeDontSupportYet() {
        String domesticViolenceValue = "ArrestedRelevantDomesticViolenceOffence, "
            + "invalid, insert random here,"
            + "UndertakingSection46Or63EFamilyLawActOrScotlandNorthernIrelandProtectiveInjunction";

        String urgencyValue = "RiskLifeLibertyPhysicalSafety";
        String previousAttendanceValue = "AnotherDisputeeResolutionn";

        mandatoryFieldsWithValues.addAll(asList(
            new OcrDataField("MIAMDomesticViolenceChecklist", domesticViolenceValue),
            new OcrDataField("MIAMUrgencyChecklist", urgencyValue),
            new OcrDataField("MIAMPreviousAttendanceChecklist", previousAttendanceValue)
        ));

        OcrValidationResult validationResult = formAValidator.validateBulkScanForm(mandatoryFieldsWithValues);

        assertThat(validationResult.getStatus(), is(SUCCESS));
        assertThat(validationResult.getWarnings(), is(emptyList()));
    }

    private String containsValueThatIsNotAccepted(String fieldName) {
        return String.format("%s contains a value that is not accepted", fieldName);
    }

    private String mustHaveAtLeastTwoNames(String fieldName) {
        return String.format("%s must contain a firstname and a lastname", fieldName);
    }

    private String mustBeOneOf(String fieldName, String... values) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("%s must be \"%s\"", fieldName, values[0]));
        for (int i = 1; i < values.length - 1; i++) {
            stringBuilder.append(String.format(", \"%s\"", values[i]));
        }
        if (values.length > 1) {
            stringBuilder.append(String.format(" or \"%s\"", values[values.length - 1]));
        }
        return stringBuilder.toString();
    }

    private Matcher<List<String>> warningMessagesForMissingOrEmptyFields() {
        return allOf(
            hasItems(mandatoryFields.stream()
                .map(mandatoryFieldIsMissing)
                .toArray(String[]::new))
        );
    }

    private Function<String, String> mandatoryFieldIsMissing = fieldName -> String.format("Mandatory field \"%s\" is missing", fieldName);
    private Function<String, OcrDataField> emptyValueOcrDataField = fieldName -> new OcrDataField(fieldName, "");
}
