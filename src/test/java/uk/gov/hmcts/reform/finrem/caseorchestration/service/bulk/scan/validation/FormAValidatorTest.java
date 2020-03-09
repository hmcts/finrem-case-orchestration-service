package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulk.scan.validation;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;
import uk.gov.hmcts.reform.bsp.common.model.validation.out.OcrValidationResult;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation.FormAValidator;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.bsp.common.model.validation.out.ValidationStatus.SUCCESS;
import static uk.gov.hmcts.reform.bsp.common.model.validation.out.ValidationStatus.WARNINGS;

public class FormAValidatorTest {

    private final FormAValidator formAValidator = new FormAValidator();
    private List<OcrDataField> listOfAllMandatoryFields;

    @Before
    public void setup() {
        listOfAllMandatoryFields = new ArrayList<>(asList(
            new OcrDataField("ApplicantRepresented", "I am not represented by a solicitor in these proceedings")
        ));
    }

    @Test
    public void shouldPassValidation() {
        OcrValidationResult validationResult = formAValidator.validateBulkScanForm(listOfAllMandatoryFields);

        assertThat(validationResult.getStatus(), is(SUCCESS));
        assertThat(validationResult.getWarnings(), is(emptyList()));
        assertThat(validationResult.getErrors(), is(emptyList()));
    }

    @Test
    public void shouldFailWhenInvalidValueOfMandatoryField() {
        OcrValidationResult validationResult = formAValidator
            .validateBulkScanForm(asList(new OcrDataField("ApplicantRepresented", "It's wrong!")));

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getWarnings().size(), is(1));
        assertThat(validationResult.getErrors(), is(emptyList()));
    }

    @Test
    public void shouldPassForNonMandatoryEmptyFields() {
        List<OcrDataField> nonMandatoryFieldsWithEmptyValues = asList(
            new OcrDataField("ApplicantSolicitorAddressLine1", ""),
            new OcrDataField("ApplicantSolicitorAddressTown", ""),
            new OcrDataField("ApplicantSolicitorAddressCounty", ""),
            new OcrDataField("ApplicantSolicitorAddressPostcode", "")
        );

        listOfAllMandatoryFields.addAll(nonMandatoryFieldsWithEmptyValues);
        OcrValidationResult validationResult = formAValidator.validateBulkScanForm(listOfAllMandatoryFields);
        assertThat(validationResult.getStatus(), is(SUCCESS));
        assertThat(validationResult.getWarnings(), is(emptyList()));
        assertThat(validationResult.getErrors(), is(emptyList()));
    }

    @Test
    public void shouldPassValidateForValuesWeDontSupportYet() {
        String domesticViolenceValue = "ArrestedRelevantDomesticViolenceOffence, "
            + "invalid, insert random here,"
            + "UndertakingSection46Or63EFamilyLawActOrScotlandNorthernIrelandProtectiveInjunction";

        String urgencyValue = "RiskLifeLibertyPhysicalSafety";
        String previousAttendanceValue = "AnotherDisputeeResolutionn";

        listOfAllMandatoryFields.addAll(asList(
            new OcrDataField("MIAMDomesticViolenceChecklist", domesticViolenceValue),
            new OcrDataField("MIAMUrgencyChecklist", urgencyValue),
            new OcrDataField("MIAMPreviousAttendanceChecklist", previousAttendanceValue)
        ));

        OcrValidationResult validationResult = formAValidator.validateBulkScanForm(listOfAllMandatoryFields);

        assertThat(validationResult.getStatus(), is(SUCCESS));
        assertThat(validationResult.getWarnings(), is(emptyList()));
    }
}
