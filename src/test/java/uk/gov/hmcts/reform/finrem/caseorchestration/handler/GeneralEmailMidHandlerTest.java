package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.TestSetUpUtils;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.GeneralEmailWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDownloadService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
