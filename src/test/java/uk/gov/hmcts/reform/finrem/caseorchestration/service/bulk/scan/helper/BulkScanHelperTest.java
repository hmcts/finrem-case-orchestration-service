package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulk.scan.helper;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.hmcts.reform.bsp.common.error.FormFieldValidationException;
import uk.gov.hmcts.reform.bsp.common.model.validation.in.OcrDataField;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.helper.BulkScanHelper;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.time.Month.FEBRUARY;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.rules.ExpectedException.none;
import static uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.helper.BulkScanHelper.*;

public class BulkScanHelperTest {

    private static final String DATE_FIELD_NAME = "DateFieldName";

    @Rule
    public ExpectedException expectedException = none();

    @Test
    public void shouldTransformDateWithRightLeapYearDate() {
        LocalDate date = BulkScanHelper.transformFormDateIntoLocalDate(DATE_FIELD_NAME, "29/02/2020");

        assertThat(date.getDayOfMonth(), is(29));
        assertThat(date.getMonth(), is(FEBRUARY));
        assertThat(date.getYear(), is(2020));
    }

    @Test
    public void shouldFailDateTransformationWithWrongLeapYearDate() {
        expectedException.expect(FormFieldValidationException.class);
        expectedException.expectMessage("DateFieldName must be a valid date");

        BulkScanHelper.transformFormDateIntoLocalDate(DATE_FIELD_NAME, "29/02/2019");
    }

    @Test
    public void getCorrectListOfCommaSeparatedValuesFromOcrFieldsMap() {
        List<OcrDataField> ocrDataFields = singletonList(
            new OcrDataField("MIAMExemptionsChecklist", "domesticViolence, urgency, previousMIAMattendance, other")
        );

        Optional<List<String>> convertedListOfValues =
            getCommaSeparatedValueFromOcrDataField("MIAMExemptionsChecklist", ocrDataFields);

        assertThat(convertedListOfValues.isPresent(), is(true));
        convertedListOfValues.ifPresent(result -> assertThat(result, allOf(
            hasSize(4),
            equalTo(Arrays.asList("domesticViolence", "urgency", "previousMIAMattendance", "other"))
        )));
    }

    @Test
    public void getEmptyListOfCommaSeparatedValuesFromNonExistentEntryInOcrFieldsMap() {
        List<OcrDataField> ocrDataFields = singletonList(
            new OcrDataField("MIAMExemptionsChecklist", "domesticViolence, urgency, other")
        );

        Optional<List<String>> convertedListOfValues =
            getCommaSeparatedValueFromOcrDataField("nonExistentEntry", ocrDataFields);

        assertThat(convertedListOfValues.isPresent(), is(false));
    }

    @Test
    public void checkCommaSeparatedEntryTransformerReturnsCorrectly() {
        List<OcrDataField> ocrDataFields = singletonList(
            new OcrDataField("exampleMultipleValue",
                "ArrestedRelevantDomesticViolenceOffence, RelevantCriminalProceedingsDomesticViolenceOffence, noWay, blah")
        );

        Optional<String> transformedListOfValues =
            commaSeparatedEntryTransformer("testChecklist", ocrDataFields, miamDomesticViolenceChecklistMap);

        assertThat(transformedListOfValues.isPresent(), is(true));
        transformedListOfValues.ifPresent(result -> assertThat(result, equalTo(
            "[FR_ms_MIAMDomesticViolenceChecklist_Value_1, " +
                "FR_ms_MIAMDomesticViolenceChecklist_Value_3, " +
                "placeholder1, ccd-blah-is-the-superior-blah]")
        ));
    }

    @Test
    public void commaSeparatedEntryTransformerDoesNotAddNonExistentElements() {
        List<OcrDataField> ocrDataFields = singletonList(
            new OcrDataField("testChecklist",
                "ArrestedRelevantDomesticViolenceOffence, RelevantCriminalProceedingsDomesticViolenceOffence, " +
                    "noWay, blah, I don't exist, random string here8u4120912")
        );

        Optional<String> transformedListOfValues =
            commaSeparatedEntryTransformer("testChecklist", ocrDataFields, miamDomesticViolenceChecklistMap);

        assertThat(transformedListOfValues.isPresent(), is(true));
        transformedListOfValues.ifPresent(result -> assertThat(result, equalTo(
            "[FR_ms_MIAMDomesticViolenceChecklist_Value_1, " +
                "FR_ms_MIAMDomesticViolenceChecklist_Value_3, " +
                "placeholder1, ccd-blah-is-the-superior-blah]")
        ));
    }
}