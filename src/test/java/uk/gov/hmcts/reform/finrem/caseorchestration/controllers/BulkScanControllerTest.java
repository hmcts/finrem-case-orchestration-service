package uk.gov.hmcts.reform.finrem.caseorchestration.controllers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.bsp.common.error.InvalidDataException;
import uk.gov.hmcts.reform.bsp.common.error.UnsupportedFormTypeException;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.ExceptionRecord;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;
import uk.gov.hmcts.reform.bsp.common.model.transformation.output.CaseCreationDetails;
import uk.gov.hmcts.reform.bsp.common.model.transformation.output.SuccessfulTransformationResponse;
import uk.gov.hmcts.reform.bsp.common.model.update.output.SuccessfulUpdateResponse;
import uk.gov.hmcts.reform.bsp.common.model.validation.in.OcrDataValidationRequest;
import uk.gov.hmcts.reform.bsp.common.model.validation.out.OcrValidationResponse;
import uk.gov.hmcts.reform.bsp.common.model.validation.out.OcrValidationResult;
import uk.gov.hmcts.reform.bsp.common.service.AuthService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkScanService;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_IMPLEMENTED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static uk.gov.hmcts.reform.bsp.common.model.validation.out.ValidationStatus.ERRORS;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.CASE_TYPE_ID_FR;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_BULK_FORM_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_BULK_UNSUPPORTED_FORM_TYPE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SERVICE_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class BulkScanControllerTest {

    private static final String TEST_KEY = "testKey";
    private static final String TEST_VALUE = "testValue";

    @Mock
    private BulkScanService bulkScanService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private BulkScanController bulkScanController;

    @Test
    public void shouldReturnValidatorResults() {
        List<OcrDataField> testOcrDataFields = singletonList(new OcrDataField(TEST_KEY, TEST_VALUE));
        when(bulkScanService.validateBulkScanForm(eq(TEST_BULK_FORM_TYPE), eq(testOcrDataFields))).thenReturn(OcrValidationResult.builder()
            .addError("this is an error")
            .addWarning("this is a warning")
            .build()
        );

        ResponseEntity<OcrValidationResponse> response = bulkScanController
            .validateOcrData(TEST_SERVICE_TOKEN, TEST_BULK_FORM_TYPE, new OcrDataValidationRequest(testOcrDataFields));

        assertThat(response.getStatusCode(), is(OK));
        OcrValidationResponse responseBody = response.getBody();
        assertThat(responseBody.getErrors(), hasItem("this is an error"));
        assertThat(responseBody.getWarnings(), hasItem("this is a warning"));
        assertThat(responseBody.getStatus(), is(ERRORS));
        verify(bulkScanService).validateBulkScanForm(eq(TEST_BULK_FORM_TYPE), eq(testOcrDataFields));

        verify(authService).assertIsServiceAllowedToValidate(TEST_SERVICE_TOKEN);
    }

    @Test
    public void shouldReturnResourceNotFoundForUnsupportedFormType_ForValidation() {
        List<OcrDataField> testOcrDataFields = singletonList(new OcrDataField(TEST_KEY, TEST_VALUE));
        String unsupportedFormType = TEST_BULK_UNSUPPORTED_FORM_TYPE;
        when(bulkScanService.validateBulkScanForm(eq(unsupportedFormType), eq(testOcrDataFields)))
            .thenThrow(UnsupportedFormTypeException.class);

        ResponseEntity<OcrValidationResponse> response = bulkScanController
            .validateOcrData(TEST_SERVICE_TOKEN, unsupportedFormType, new OcrDataValidationRequest(testOcrDataFields));

        assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND));
        OcrValidationResponse responseBody = response.getBody();
        assertThat(responseBody, is(nullValue()));
        verify(bulkScanService).validateBulkScanForm(eq(unsupportedFormType), eq(testOcrDataFields));

        verify(authService).assertIsServiceAllowedToValidate(TEST_SERVICE_TOKEN);
    }

    @Test
    public void shouldReturnTransformerServiceResults() {
        ExceptionRecord exceptionRecord = ExceptionRecord.builder().formType(TEST_BULK_FORM_TYPE).build();
        when(bulkScanService.transformBulkScanForm(exceptionRecord)).thenReturn(singletonMap(TEST_KEY, TEST_VALUE));

        ResponseEntity<SuccessfulTransformationResponse> response =
            bulkScanController.transformExceptionRecordIntoCase(TEST_SERVICE_TOKEN, exceptionRecord);

        assertThat(response.getStatusCode(), is(OK));
        SuccessfulTransformationResponse transformationResponse = response.getBody();
        assertThat(transformationResponse.getWarnings(), is(emptyList()));
        CaseCreationDetails caseCreationDetails = transformationResponse.getCaseCreationDetails();
        assertThat(caseCreationDetails.getCaseTypeId(), is(CASE_TYPE_ID_FR));
        assertThat(caseCreationDetails.getEventId(), is("createCase"));
        assertThat(caseCreationDetails.getCaseData(), hasEntry(TEST_KEY, TEST_VALUE));

        verify(authService).assertIsServiceAllowedToUpdate(TEST_SERVICE_TOKEN);
    }

    @Test
    public void shouldReturnUpdateServiceResults() {
        ResponseEntity<SuccessfulUpdateResponse> response =
                bulkScanController.updateCase(TEST_SERVICE_TOKEN, new Object());

        assertThat(response.getStatusCode(), is(NOT_IMPLEMENTED));
    }

    @Test
    public void shouldReturnErrorForUnsupportedFormType_ForTransformation() {
        ExceptionRecord exceptionRecord = ExceptionRecord.builder().formType(TEST_BULK_FORM_TYPE).build();
        when(bulkScanService.transformBulkScanForm(exceptionRecord)).thenThrow(UnsupportedFormTypeException.class);

        ResponseEntity response = bulkScanController.transformExceptionRecordIntoCase(TEST_SERVICE_TOKEN, exceptionRecord);

        assertThat(response.getStatusCode(), is(UNPROCESSABLE_ENTITY));
        verify(authService).assertIsServiceAllowedToUpdate(TEST_SERVICE_TOKEN);
    }

    @Test
    public void shouldReturnErrorWhenInvalidDateException_ForTransformation() {
        ExceptionRecord exceptionRecord = ExceptionRecord.builder().formType(TEST_BULK_FORM_TYPE).build();
        when(bulkScanService.transformBulkScanForm(exceptionRecord)).thenThrow(InvalidDataException.class);

        ResponseEntity response = bulkScanController.transformExceptionRecordIntoCase(TEST_SERVICE_TOKEN, exceptionRecord);

        assertThat(response.getStatusCode(), is(UNPROCESSABLE_ENTITY));
        verify(authService).assertIsServiceAllowedToUpdate(TEST_SERVICE_TOKEN);
    }
}
