package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.OcrDataField;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.transformation.in.ExceptionRecord;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformer.FRFormToCaseTransformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class FRFormToCaseTransformerTest {

    private ExceptionRecord testExceptionRecord;

    @Before
    public void setup() {
        List<OcrDataField> testOcrDataFields = new ArrayList<>();

        testOcrDataFields.add(new OcrDataField("firstName", "John"));
        testOcrDataFields.add(new OcrDataField("lastName", "Smith"));

        testExceptionRecord = new ExceptionRecord(
                "test",
                "LV684287",
                "poBoxTest",
                testOcrDataFields
        );
    }

    @Test
    public void givenValidExceptionRecord_whenTransformed_returnValidCcdData() {
        FRFormToCaseTransformer frFormToCaseTransformer = new FRFormToCaseTransformer();

        Map<String, Object> transformedCcdData = frFormToCaseTransformer.transformIntoCaseData(testExceptionRecord);
        Map<String, Object> expectedCcdData = new HashMap<String, Object>() {
            {
                put("D8FirstName", "John");
                put("D8LastName", "Smith");
            }
        };

        assertThat(transformedCcdData, is(expectedCcdData));
    }
}