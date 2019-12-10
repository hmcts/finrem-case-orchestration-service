package uk.gov.hmcts.reform.finrem.caseorchestration.service.bulk.scan.validation;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.exception.bulk.scan.UnsupportedFormTypeException;
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

@RunWith(MockitoJUnitRunner.class)
public class BulkScanServiceTest {

    @Rule
    public ExpectedException expectedException = none();

    @Mock
    private BulkScanFormTransformerFactory bulkScanFormTransformerFactory;

    @Mock
    private BulkScanFormTransformer bulkScanFormTransformer;

    @Mock
    private BulkScanFormValidator validator;

    @Mock
    private BulkScanFormValidatorFactory factory;

    @InjectMocks
    private BulkScanService bulkScanService;

    @Test
    public void shouldCallReturnedValidator() throws UnsupportedFormTypeException {
        OcrValidationResult resultFromValidator = OcrValidationResult.builder().build();
        List<OcrDataField> ocrDataFields = singletonList(new OcrDataField("myName", "myValue"));
        when(factory.getValidator("testForm")).thenReturn(validator);
        when(validator.validateBulkScanForm(ocrDataFields)).thenReturn(resultFromValidator);

        OcrValidationResult returnedResult = bulkScanService.validateBulkScanForm("testForm", ocrDataFields);

        verify(validator).validateBulkScanForm(ocrDataFields);
        assertThat(returnedResult, equalTo(resultFromValidator));
    }

    @Test
    public void shouldRethrowUnsupportedFormTypeExceptionFromFactory() {
        expectedException.expect(UnsupportedFormTypeException.class);
        List<OcrDataField> ocrDataFields = singletonList(new OcrDataField("myName", "myValue"));
        when(factory.getValidator("unsupportedFormType")).thenThrow(UnsupportedFormTypeException.class);

        bulkScanService.validateBulkScanForm("unsupportedFormType", ocrDataFields);
    }

    @Test
    public void shouldCallReturnedTransformer() throws UnsupportedFormTypeException {
        ExceptionRecord exceptionRecord = ExceptionRecord.builder()
            .formType("testFormType")
            .ocrDataFields(singletonList(new OcrDataField("myName", "myValue")))
            .build();
        when(bulkScanFormTransformerFactory.getTransformer("testFormType")).thenReturn(bulkScanFormTransformer);
        when(bulkScanFormTransformer.transformIntoCaseData(exceptionRecord)).thenReturn(singletonMap("testKey", "testValue"));

        Map<String, Object> returnedResult = bulkScanService.transformBulkScanForm(exceptionRecord);

        verify(bulkScanFormTransformerFactory).getTransformer("testFormType");
        verify(bulkScanFormTransformer).transformIntoCaseData(exceptionRecord);
        assertThat(returnedResult, hasEntry("testKey", "testValue"));
    }

    @Test
    public void shouldRethrowUnsupportedFormTypeExceptionFromFormTransformerFactory() {
        expectedException.expect(UnsupportedFormTypeException.class);

        ExceptionRecord exceptionRecord = ExceptionRecord.builder().formType("unsupportedFormType").build();
        when(bulkScanFormTransformerFactory.getTransformer("unsupportedFormType")).thenThrow(UnsupportedFormTypeException.class);

        bulkScanService.transformBulkScanForm(exceptionRecord);
    }
}