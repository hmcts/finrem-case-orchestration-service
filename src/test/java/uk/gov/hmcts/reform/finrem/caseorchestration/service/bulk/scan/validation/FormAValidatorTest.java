package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulk.scan.validation;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.bsp.common.model.validation.in.OcrDataField;
import uk.gov.hmcts.reform.bsp.common.model.validation.out.OcrValidationResult;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation.FormAValidator;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.hasItems;
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
            new OcrDataField("claimingExemptionMIAM", "No"),
            new OcrDataField("familyMediatorMIAM", "No"),
            new OcrDataField("applicantAttendedMIAM", "No")
        ));
    }

    @Test
    public void shouldPassValidationWhenMandatoryFieldsArePresent() {
        OcrValidationResult validationResult = formAValidator.validateBulkScanForm(listOfAllMandatoryFields);

        assertThat(validationResult.getStatus(), is(SUCCESS));
        assertThat(validationResult.getWarnings(), is(emptyList()));
        assertThat(validationResult.getErrors(), is(emptyList()));
    }

    @Test
    public void shouldFailValidationWhenMandatoryFieldsAreMissing() {
        OcrValidationResult validationResult = formAValidator.validateBulkScanForm(emptyList());

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getErrors(), is(emptyList()));
        assertThat(validationResult.getWarnings(), hasItems(
            "Mandatory field \"claimingExemptionMIAM\" is missing",
            "Mandatory field \"familyMediatorMIAM\" is missing",
            "Mandatory field \"applicantAttendedMIAM\" is missing"
        ));
    }

    @Test
    public void shouldFailValidationWhenMandatoryFieldIsPresentButEmpty() {
        OcrValidationResult validationResult = formAValidator.validateBulkScanForm(asList(
            new OcrDataField("claimingExemptionMIAM", ""),
            new OcrDataField("familyMediatorMIAM", ""),
            new OcrDataField("applicantAttendedMIAM", "")
        ));

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getErrors(), is(emptyList()));
        assertThat(validationResult.getWarnings(), hasItems(
            "Mandatory field \"claimingExemptionMIAM\" is missing",
            "Mandatory field \"familyMediatorMIAM\" is missing",
            "Mandatory field \"applicantAttendedMIAM\" is missing"
        ));
    }

    @Test
    public void shouldFailFieldsHavingInvalidValues() {
        OcrValidationResult validationResult = formAValidator.validateBulkScanForm(asList(
            new OcrDataField("claimingExemptionMIAM", "Maybe"),
            new OcrDataField("familyMediatorMIAM", "Not really sure"),
            new OcrDataField("applicantAttendedMIAM", "Let me get back to you on it")
        ));

        assertThat(validationResult.getStatus(), is(WARNINGS));
        assertThat(validationResult.getErrors(), is(emptyList()));
        assertThat(validationResult.getWarnings(), hasItems(
            "claimingExemptionMIAM must be \"Yes\" or \"No\"",
            "familyMediatorMIAM must be \"Yes\" or \"No\"",
            "applicantAttendedMIAM must be \"Yes\" or \"No\""
        ));
    }

    @Test
    public void shouldPassForNonMandatoryEmptyFields() {
        // placeholder for future work - in case form A has only mandatory fields remove the test
    }
}
