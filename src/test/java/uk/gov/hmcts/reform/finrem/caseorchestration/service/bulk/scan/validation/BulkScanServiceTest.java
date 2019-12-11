package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulk.scan.validation;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.bsp.common.error.UnsupportedFormTypeException;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.transformation.in.ExceptionRecord;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.validation.in.OcrDataField;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.validation.out.OcrValidationResult;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkScanService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformations.BulkScanFormTransformer;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformations.BulkScanFormTransformerFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation.BulkScanFormValidator;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation.BulkScanFormValidatorFactory;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.UNSUPPORTED_FORM_TYPE;

@RunWith(MockitoJUnitRunner.class)
public class BulkScanServiceTest {

    private static final String TEST_FORM = "testForm";
    private static final String TEST_FORM_TYPE = "testFormType";
    private static final String TEST_KEY = "testKey";
    private static final String TEST_VALUE = "testValue";

    @Rule
    public ExpectedException expectedException = none();

    @Mock
    private BulkScanFormTransformerFactory bulkScanFormTransformerFactory;

    @Mock
    private BulkScanFormTransformer bulkScanFormTransformer;

    @Mock
    private BulkScanFormValidator bulkScanFormValidator;

    @Mock
    private BulkScanFormValidatorFactory bulkScanFormValidatorFactory;

    @InjectMocks
    private BulkScanService bulkScanService;

    @Test
    public void shouldCallReturnedValidator() throws UnsupportedFormTypeException {
        OcrValidationResult resultFromValidator = OcrValidationResult.builder().build();
        List<OcrDataField> ocrDataFields = singletonList(new OcrDataField(TEST_KEY, TEST_VALUE));
        when(bulkScanFormValidatorFactory.getValidator(TEST_FORM)).thenReturn(bulkScanFormValidator);
        when(bulkScanFormValidator.validateBulkScanForm(ocrDataFields)).thenReturn(resultFromValidator);

        OcrValidationResult returnedResult = bulkScanService.validateBulkScanForm(TEST_FORM, ocrDataFields);

        verify(bulkScanFormValidator).validateBulkScanForm(ocrDataFields);
        assertThat(returnedResult, equalTo(resultFromValidator));
    }

    @Test
    public void shouldRethrowUnsupportedFormTypeExceptionFromFactory() {
        expectedException.expect(UnsupportedFormTypeException.class);
        List<OcrDataField> ocrDataFields = singletonList(new OcrDataField(TEST_KEY, TEST_VALUE));
        when(bulkScanFormValidatorFactory.getValidator(UNSUPPORTED_FORM_TYPE)).thenThrow(UnsupportedFormTypeException.class);

        bulkScanService.validateBulkScanForm(UNSUPPORTED_FORM_TYPE, ocrDataFields);
    }

    @Test
    public void shouldCallReturnedTransformer() throws UnsupportedFormTypeException {
        ExceptionRecord exceptionRecord = ExceptionRecord.builder()
            .formType(TEST_FORM_TYPE)
            .ocrDataFields(singletonList(new OcrDataField(TEST_KEY, TEST_VALUE)))
            .build();
        when(bulkScanFormTransformerFactory.getTransformer(TEST_FORM_TYPE)).thenReturn(bulkScanFormTransformer);
        when(bulkScanFormTransformer.transformIntoCaseData(exceptionRecord)).thenReturn(singletonMap(TEST_KEY, TEST_VALUE));

        Map<String, Object> returnedResult = bulkScanService.transformBulkScanForm(exceptionRecord);

        verify(bulkScanFormTransformerFactory).getTransformer(TEST_FORM_TYPE);
        verify(bulkScanFormTransformer).transformIntoCaseData(exceptionRecord);
        assertThat(returnedResult, hasEntry(TEST_KEY, TEST_VALUE));
    }

    @Test
    public void shouldRethrowUnsupportedFormTypeExceptionFromFormTransformerFactory() {
        expectedException.expect(UnsupportedFormTypeException.class);

        ExceptionRecord exceptionRecord = ExceptionRecord.builder().formType(UNSUPPORTED_FORM_TYPE).build();
        when(bulkScanFormTransformerFactory.getTransformer(UNSUPPORTED_FORM_TYPE)).thenThrow(UnsupportedFormTypeException.class);

        bulkScanService.transformBulkScanForm(exceptionRecord);
    }
}