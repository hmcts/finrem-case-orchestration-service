package uk.gov.hmcts.reform.finrem.caseorchestration.handler.creategeneralemail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralEmailWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDownloadService;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.CASE_ID_IN_LONG;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils.caseDocument;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.MID_EVENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandleAnyCaseType;

@ExtendWith(MockitoExtension.class)
class GeneralEmailMidHandlerTest {

    private static final int MAX_FILE_SIZE = 2 * 1024 * 1024;
    private static final byte[] PDF_BYTES = {1};
    private static final byte[] PDF_BYTES_EXCEEDS_FILE_SIZE = new byte[MAX_FILE_SIZE + 1];

    private final CaseDocument attachment = caseDocument("a.doc");

    @InjectMocks
    private GeneralEmailMidHandler handler;

    @Mock
    private BulkPrintDocumentService bulkPrintDocumentService;
    @Mock
    private EvidenceManagementDownloadService evidenceManagementDownloadService;

    @BeforeEach
    void setUp() {
        lenient().when(evidenceManagementDownloadService.getByteArray(attachment, AUTH_TOKEN)).thenReturn(PDF_BYTES);
    }

    @Test
    void shouldHandleAllCaseTypes() {
        assertCanHandleAnyCaseType(handler, MID_EVENT, EventType.CREATE_GENERAL_EMAIL);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void givenEmailWithAttachment_whenHandle_thenShouldValidateEncryptionOnAttachment(boolean withAttachment) {
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .generalEmailWrapper(GeneralEmailWrapper.builder().generalEmailUploadedDocument(withAttachment
                ? attachment : null).build())
            .build();
        FinremCallbackRequest request = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, finremCaseData);

        handler.handle(request, AUTH_TOKEN);

        verify(bulkPrintDocumentService, times(withAttachment ? 1 : 0))
            .validateEncryptionOnUploadedDocument(attachment, CASE_ID, List.of(), AUTH_TOKEN);
    }

    @Test
    void givenEncryptionDetectedOnAttachment_whenHandle_thenPopulateError() {
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .generalEmailWrapper(GeneralEmailWrapper.builder().generalEmailUploadedDocument(attachment).build())
            .build();
        FinremCallbackRequest request = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, finremCaseData);

        mockEncryptionDetected();

        var response = handler.handle(request, AUTH_TOKEN);

        assertAll(
            () -> verify(bulkPrintDocumentService).validateEncryptionOnUploadedDocument(eq(attachment), eq(CASE_ID), anyList(),
                eq(AUTH_TOKEN)),
            () -> assertThat(response.getErrors()).containsOnly("ENCRYPTION_DETECTED")
        );
    }

    @Test
    void givenFileSizeExceeds_whenHandle_thenPopulateError() {
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .generalEmailWrapper(GeneralEmailWrapper.builder().generalEmailUploadedDocument(attachment).build())
            .build();
        FinremCallbackRequest request = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, finremCaseData);

        when(evidenceManagementDownloadService.getByteArray(attachment, AUTH_TOKEN)).thenReturn(PDF_BYTES_EXCEEDS_FILE_SIZE);

        var response = handler.handle(request, AUTH_TOKEN);

        assertAll(
            () -> verify(bulkPrintDocumentService).validateEncryptionOnUploadedDocument(eq(attachment), eq(CASE_ID), anyList(),
                eq(AUTH_TOKEN)),
            () -> verify(evidenceManagementDownloadService).getByteArray(attachment, AUTH_TOKEN),
            () -> assertThat(response.getErrors()).containsOnly( "You attached a document which exceeds the size limit: 2MB")
        );
    }

    @Test
    void givenFileSizeExceedsAndEncryptionDetected_whenHandle_thenPopulateErrors() {
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .generalEmailWrapper(GeneralEmailWrapper.builder().generalEmailUploadedDocument(attachment).build())
            .build();
        FinremCallbackRequest request = FinremCallbackRequestFactory.from(CASE_ID_IN_LONG, finremCaseData);

        when(evidenceManagementDownloadService.getByteArray(attachment, AUTH_TOKEN)).thenReturn(PDF_BYTES_EXCEEDS_FILE_SIZE);
        mockEncryptionDetected();

        var response = handler.handle(request, AUTH_TOKEN);

        assertAll(
            () -> verify(bulkPrintDocumentService).validateEncryptionOnUploadedDocument(eq(attachment), eq(CASE_ID), anyList(),
                eq(AUTH_TOKEN)),
            () -> verify(evidenceManagementDownloadService).getByteArray(attachment, AUTH_TOKEN),
            () -> assertThat(response.getErrors()).containsExactly("ENCRYPTION_DETECTED",
                "You attached a document which exceeds the size limit: 2MB")
        );
    }

    private void mockEncryptionDetected() {
        List<String> errors = new ArrayList<>();
        doAnswer(invocation -> {
            ((List<String>) invocation.getArguments()[2]).add("ENCRYPTION_DETECTED");
            return null;
        }).when(bulkPrintDocumentService).validateEncryptionOnUploadedDocument(
            attachment, CASE_ID, errors, AUTH_TOKEN);
    }
}
