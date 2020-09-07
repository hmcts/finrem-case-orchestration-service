package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.bsp.common.error.InvalidDataException;
import uk.gov.hmcts.reform.bsp.common.error.UnsupportedFormTypeException;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.ExceptionRecord;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;
import uk.gov.hmcts.reform.bsp.common.model.validation.out.OcrValidationResult;
import uk.gov.hmcts.reform.bsp.common.service.BulkScanFormValidator;
import uk.gov.hmcts.reform.bsp.common.service.transformation.BulkScanFormTransformer;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkScanService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.transformation.FinRemBulkScanFormTransformerFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation.FinRemBulkScanFormValidatorFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.bulkscan.validation.FormAValidator;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_BULK_UNSUPPORTED_FORM_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_FORM;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_FORM_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_KEY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class BulkScanServiceTest {

    @Rule
    public ExpectedException expectedException = none();

    @Mock
    private FinRemBulkScanFormTransformerFactory finRemBulkScanFormTransformerFactory;

    @Mock
    private BulkScanFormTransformer bulkScanFormTransformer;

    @Mock
    private BulkScanFormValidator bulkScanFormValidator;

    @Mock
    private FormAValidator formAValidator;

    @Mock
    private FinRemBulkScanFormValidatorFactory finRemBulkScanFormValidatorFactory;

    @InjectMocks
    private BulkScanService bulkScanService;

    @Test
    public void shouldCallReturnedValidator() throws UnsupportedFormTypeException {
        OcrValidationResult resultFromValidator = OcrValidationResult.builder().build();
        List<OcrDataField> ocrDataFields = singletonList(new OcrDataField(TEST_KEY, TEST_VALUE));
        when(finRemBulkScanFormValidatorFactory.getValidator(TEST_FORM)).thenReturn(bulkScanFormValidator);
        when(bulkScanFormValidator.validateBulkScanForm(ocrDataFields)).thenReturn(resultFromValidator);

        OcrValidationResult returnedResult = bulkScanService.validateBulkScanForm(TEST_FORM, ocrDataFields);

        verify(bulkScanFormValidator).validateBulkScanForm(ocrDataFields);
        assertThat(returnedResult, equalTo(resultFromValidator));
    }

    @Test
    public void shouldRethrowUnsupportedFormTypeExceptionFromFactory() {
        expectedException.expect(UnsupportedFormTypeException.class);
        List<OcrDataField> ocrDataFields = singletonList(new OcrDataField(TEST_KEY, TEST_VALUE));
        when(finRemBulkScanFormValidatorFactory.getValidator(TEST_BULK_UNSUPPORTED_FORM_TYPE)).thenThrow(UnsupportedFormTypeException.class);

        bulkScanService.validateBulkScanForm(TEST_BULK_UNSUPPORTED_FORM_TYPE, ocrDataFields);
    }

    @Test
    public void shouldCallReturnedTransformer() throws UnsupportedFormTypeException {
        ExceptionRecord exceptionRecord = ExceptionRecord.builder()
            .formType(TEST_FORM_TYPE)
            .ocrDataFields(singletonList(new OcrDataField(TEST_KEY, TEST_VALUE)))
            .build();

        OcrValidationResult resultFromValidator = OcrValidationResult.builder().build();
        when(finRemBulkScanFormValidatorFactory.getValidator(TEST_FORM_TYPE)).thenReturn(bulkScanFormValidator);
        when(bulkScanFormValidator.validateBulkScanForm(any())).thenReturn(resultFromValidator);
        when(formAValidator.validateFormAScannedDocuments(any())).thenReturn(resultFromValidator);
        when(finRemBulkScanFormTransformerFactory.getTransformer(TEST_FORM_TYPE)).thenReturn(bulkScanFormTransformer);
        when(bulkScanFormTransformer.transformIntoCaseData(exceptionRecord)).thenReturn(singletonMap(TEST_KEY, TEST_VALUE));

        Map<String, Object> returnedResult = bulkScanService.transformBulkScanForm(exceptionRecord);

        verify(finRemBulkScanFormTransformerFactory).getTransformer(TEST_FORM_TYPE);
        verify(bulkScanFormTransformer).transformIntoCaseData(exceptionRecord);
        assertThat(returnedResult, hasEntry(TEST_KEY, TEST_VALUE));
    }

    @Test
    public void shouldRethrowUnsupportedFormTypeExceptionFromFormTransformerFactory() {
        expectedException.expect(UnsupportedFormTypeException.class);

        ExceptionRecord exceptionRecord = ExceptionRecord.builder().formType(TEST_BULK_UNSUPPORTED_FORM_TYPE).build();
        when(finRemBulkScanFormValidatorFactory.getValidator(TEST_BULK_UNSUPPORTED_FORM_TYPE)).thenThrow(UnsupportedFormTypeException.class);

        bulkScanService.transformBulkScanForm(exceptionRecord);
    }

    @Test(expected = InvalidDataException.class)
    public void shouldThrowInvalidDataExceptionForTransformer() {
        ExceptionRecord exceptionRecord = ExceptionRecord.builder()
                .formType(TEST_FORM_TYPE)
                .ocrDataFields(singletonList(new OcrDataField(TEST_KEY, TEST_VALUE)))
                .build();

        OcrValidationResult validatedWithWarn = OcrValidationResult.builder().addWarning("warning").build();
        when(finRemBulkScanFormValidatorFactory.getValidator(TEST_FORM_TYPE)).thenReturn(bulkScanFormValidator);
        when(bulkScanFormValidator.validateBulkScanForm(any())).thenReturn(validatedWithWarn);

        bulkScanService.transformBulkScanForm(exceptionRecord);

        verify(finRemBulkScanFormTransformerFactory, never()).getTransformer(TEST_FORM_TYPE);
        verify(bulkScanFormTransformer, never()).transformIntoCaseData(exceptionRecord);
    }
}
