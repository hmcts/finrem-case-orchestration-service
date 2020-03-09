package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulk.scan.transformation;

import org.junit.Test;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.ExceptionRecord;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.OcrFieldName;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformation.FormAToCaseTransformer;

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

    @Test
    public void shouldTransformFieldsAccordingly() {
        ExceptionRecord exceptionRecord = createExceptionRecord(asList(
            new OcrDataField(OcrFieldName.DIVORCE_CASE_NUMBER, "1234567890"),
            new OcrDataField(OcrFieldName.HWF_NUMBER, "123456"),
            new OcrDataField(OcrFieldName.APPLICANT_FULL_NAME, "Peter Griffin"),
            new OcrDataField(OcrFieldName.RESPONDENT_FULL_NAME, "Louis Griffin"),
            new OcrDataField(OcrFieldName.PROVISION_MADE_FOR, "in connection with matrimonial or civil partnership proceedings"),
            new OcrDataField(OcrFieldName.NATURE_OF_APPLICATION, "Periodical Payment Order, Pension Attachment Order"),
            new OcrDataField(OcrFieldName.APPLICANT_INTENDS_TO, "ApplyToCourtFor"),
            new OcrDataField(OcrFieldName.DISCHARGE_PERIODICAL_PAYMENT_SUBSTITUTE, "a lump sum order, a pension sharing order"),
            new OcrDataField(OcrFieldName.APPLYING_FOR_CONSENT_ORDER, "Yes"),
            new OcrDataField(OcrFieldName.DIVORCE_STAGE_REACHED, "Decree Nisi")
        ));

        Map<String, Object> transformedCaseData = formAToCaseTransformer.transformIntoCaseData(exceptionRecord);

        assertThat(transformedCaseData, allOf(
            hasEntry(BULK_SCAN_CASE_REFERENCE, TEST_CASE_ID),
            hasEntry(CCDConfigConstant.DIVORCE_CASE_NUMBER, "1234567890"),
            hasEntry("HWFNumber", "123456"),
            hasEntry("applicantFMName", "Peter"),
            hasEntry("applicantLName", "Griffin"),
            hasEntry("appRespondentFMname", "Louis"),
            hasEntry("appRespondentLName", "Griffin"),
            hasEntry("provisionMadeFor", "in connection with matrimonial or civil partnership proceedings"),
            hasEntry("applicantIntendsTo", "ApplyToCourtFor"),
            hasEntry("applyingForConsentOrder", "Yes"),
            hasEntry("divorceStageReached", "Decree Nisi")
        ));
    }

    private ExceptionRecord createExceptionRecord(List<OcrDataField> ocrDataFields) {
        return ExceptionRecord.builder().id(TEST_CASE_ID).ocrDataFields(ocrDataFields).build();
    }
}
