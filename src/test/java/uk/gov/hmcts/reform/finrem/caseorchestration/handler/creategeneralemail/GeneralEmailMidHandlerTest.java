package uk.gov.hmcts.reform.finrem.caseorchestration.handler.creategeneralemail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralEmailWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDownloadService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.MID_EVENT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.CREATE_GENERAL_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class GeneralEmailMidHandlerTest {

    private GeneralEmailMidHandler handler;

    @Mock
    private BulkPrintDocumentService bulkPrintDocumentService;
    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;
    @Mock
    private EvidenceManagementDownloadService evidenceManagementDownloadService;

    @BeforeEach
    void setup() {
        handler =  new GeneralEmailMidHandler(finremCaseDetailsMapper, evidenceManagementDownloadService, bulkPrintDocumentService);
    }

    @Test
    void testCanHandle() {
        assertCanHandle(handler,
            Arguments.of(MID_EVENT, CONTESTED, CREATE_GENERAL_EMAIL),
            Arguments.of(MID_EVENT, CONSENTED, CREATE_GENERAL_EMAIL)
        );
    }

    @Test
    void givenACcdCallbackCallbackCreateGeneralEmailMidHandler() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest();

        when(evidenceManagementDownloadService.getByteArray(any(CaseDocument.class), eq(AUTH_TOKEN))).thenReturn(new byte[1]);

        handler.handle(callbackRequest, AUTH_TOKEN);
        verify(bulkPrintDocumentService).validateEncryptionOnUploadedDocument(any(), any(), any(), any());
    }

    @Test
    void shouldReturnErrorWhenFileUploadedExceeds2MB() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest();

        when(evidenceManagementDownloadService.getByteArray(any(CaseDocument.class), eq(AUTH_TOKEN))).thenReturn(new byte[2 * 1024 * 1024 + 1]);
        doNothing().when(bulkPrintDocumentService).validateEncryptionOnUploadedDocument(any(), any(), any(), any());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);
        verify(bulkPrintDocumentService).validateEncryptionOnUploadedDocument(any(), any(), any(), any());
        assertThat(response.getErrors()).containsExactly("You attached a document which exceeds the size limit: 2MB");
    }

    @Test
    void shouldReturnMultipleErrorsWhenFileUploadedExceeds2mbAndWithEncryptionValidationError() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest();

        String expectedErrorMessage = "Encryption validation failed";

        when(evidenceManagementDownloadService.getByteArray(any(CaseDocument.class), eq(AUTH_TOKEN))).thenReturn(new byte[2 * 1024 * 1024 + 1]);
        doAnswer(invocation -> {
            List<String> errorList = invocation.getArgument(2);
            errorList.add(expectedErrorMessage);
            return null;
        }).when(bulkPrintDocumentService).validateEncryptionOnUploadedDocument(
            any(CaseDocument.class), anyString(), anyList(), anyString()
        );
        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(callbackRequest, AUTH_TOKEN);
        verify(bulkPrintDocumentService).validateEncryptionOnUploadedDocument(any(), any(), any(), any());
        assertThat(response.getErrors()).containsExactly(expectedErrorMessage, "You attached a document which exceeds the size limit: 2MB");
    }

    private FinremCallbackRequest buildFinremCallbackRequest() {
        FinremCaseData caseData = FinremCaseData.builder()
            .generalEmailWrapper(GeneralEmailWrapper.builder()
                .generalEmailCreatedBy("Test")
                .generalEmailBody("Email body")
                .generalEmailRecipient("Applicant@tedt.com")
                .generalEmailUploadedDocument(TestSetUpUtils.caseDocument())
                .build())
            .build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123L).caseType(CaseType.CONSENTED).data(caseData).build();
        return FinremCallbackRequest.builder().eventType(CREATE_GENERAL_EMAIL).caseDetails(caseDetails).build();
    }
}
