package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulk.scan.transformation;

import org.junit.Test;
import uk.gov.hmcts.reform.bsp.common.model.transformation.in.ExceptionRecord;
import uk.gov.hmcts.reform.bsp.common.model.validation.in.OcrDataField;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformations.FormAToCaseTransformer;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.collection.IsMapWithSize.aMapWithSize;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.BULK_SCAN_CASE_REFERENCE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_CASE_ID;

public class FormAToCaseTransformerTest {

    private final FormAToCaseTransformer formAToCaseTransformer = new FormAToCaseTransformer();

    @Test
    public void shouldTransformFieldsAccordingly() {
        ExceptionRecord exceptionRecord = createExceptionRecord(asList(
            new OcrDataField("claimingExemptionMIAM", "Yes"),
            new OcrDataField("familyMediatorMIAM", "No"),
            new OcrDataField("applicantAttendedMIAM", "No")
        ));

        Map<String, Object> transformedCaseData = formAToCaseTransformer.transformIntoCaseData(exceptionRecord);

        assertThat(transformedCaseData, allOf(
            hasEntry(BULK_SCAN_CASE_REFERENCE, TEST_CASE_ID),
            hasEntry("claimingExemptionMIAM", "Yes"),
            hasEntry("familyMediatorMIAM", "No"),
            hasEntry("applicantAttendedMIAM", "No")
        ));
    }

    @Test
    public void shouldNotReturnUnexpectedField() {
        ExceptionRecord incomingExceptionRecord = createExceptionRecord(singletonList(
            new OcrDataField("UnexpectedName", "UnexpectedValue")
        ));

        Map<String, Object> transformedCaseData = formAToCaseTransformer.transformIntoCaseData(incomingExceptionRecord);

        assertThat(transformedCaseData, allOf(
            aMapWithSize(1),
            hasEntry(BULK_SCAN_CASE_REFERENCE, TEST_CASE_ID)
        ));
    }

    private ExceptionRecord createExceptionRecord(List<OcrDataField> ocrDataFields) {
        return ExceptionRecord.builder().id(TEST_CASE_ID).ocrDataFields(ocrDataFields).build();
    }
}
