package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulk.scan.validation;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.bsp.common.model.validation.in.OcrDataField;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.APPLYING_FOR_CONSENT_ORDER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.DISCHARGE_PERIODICAL_PAYMENT_SUBSTITUTE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.DIVORCE_CASE_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.DIVORCE_STAGE_REACHED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.HWF_NUMBER;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName.NATURE_OF_APPLICATION;
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
                new OcrDataField(DIVORCE_STAGE_REACHED, "Decree Nisi")
        ));

        mandatoryFields = mandatoryFieldsWithValues.stream().map(OcrDataField::getName).collect(Collectors.toList());
    }

    @Test
    public void shouldPassValidation() {
        List<OcrDataField> ocrDataFields = new ArrayList<>(mandatoryFieldsWithValues);
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
    public void shouldFailValidationWhenMandatoryFieldIsPresentButEmpty() {
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
            new OcrDataField(HWF_NUMBER, "")
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
            new OcrDataField(DIVORCE_STAGE_REACHED, "The cree")
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
            mustBeOneOf(DIVORCE_STAGE_REACHED, "Decree Nisi", "Decree Absolute")
        ));
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
        for (int i=1; i<values.length-1; i++) {
            stringBuilder.append(String.format(", \"%s\"", values[i]));
        }
        if (values.length > 1) {
            stringBuilder.append(String.format(" or \"%s\"", values[values.length-1]));
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
