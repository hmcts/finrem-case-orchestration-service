package uk.gov.hmcts.reform.finrem.caseorchestration.handler.citizenui.documentupload;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CorrespondenceEventAuditOrchestrationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.citizen.CitizenDocumentsUploadedCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.evidencemanagement.EvidenceManagementDeleteService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.retry.RetryExecutor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class CUIDocumentUploadSubmittedHandlerTest {

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Mock
    private EvidenceManagementDeleteService evidenceManagementDeleteService;

    @Mock
    private RetryExecutor retryExecutor;

    @Mock
    private CitizenDocumentsUploadedCorresponder citizenDocumentsUploadedCorresponder;

    @Mock
    private CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService;

    private TestHandler handler;

    @BeforeEach
    void setUp() {
        handler = new TestHandler(
            finremCaseDetailsMapper,
            evidenceManagementDeleteService,
            retryExecutor,
            citizenDocumentsUploadedCorresponder,
            correspondenceEventAuditOrchestrationService
        );
    }

    @Test
    void givenNotificationParty_whenBuildingDocumentUploadCorrespondenceEvent_thenUsesExpectedParty() {
        FinremCaseDetails caseDetails = FinremCaseDetails.builder()
            .id(12345L)
            .data(new FinremCaseData())
            .build();

        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder()
            .caseDetails(caseDetails)
            .eventType(EventType.CUI_APPLICANT_DOCUMENT_UPLOAD)
            .build();

        SendCorrespondenceEvent expectedEvent = SendCorrespondenceEvent.builder()
            .caseDetails(caseDetails)
            .build();

        when(citizenDocumentsUploadedCorresponder.buildCorrespondenceEvent(
            caseDetails,
            AUTH_TOKEN,
            NotificationParty.CITIZEN_APPLICANT
        )).thenReturn(expectedEvent);

        SendCorrespondenceEvent actualEvent =
            handler.buildCorrespondenceEventForTest(callbackRequest, AUTH_TOKEN);

        assertThat(actualEvent).isSameAs(expectedEvent);

        verify(citizenDocumentsUploadedCorresponder)
            .buildCorrespondenceEvent(
                caseDetails,
                AUTH_TOKEN,
                NotificationParty.CITIZEN_APPLICANT
            );
    }

    @Test
    void shouldReturnCitizenDocumentsUploadedTaskDescription() {
        assertThat(handler.correspondenceTaskDescriptionForTest())
            .isEqualTo("Send citizen documents uploaded email: [APPLICANT]");
    }

    private static final class TestHandler extends CUIDocumentUploadSubmittedHandler {

        private static final Logger LOGGER = LoggerFactory.getLogger(CUIDocumentUploadSubmittedHandlerTest.TestHandler.class);

        private TestHandler(
            FinremCaseDetailsMapper finremCaseDetailsMapper,
            EvidenceManagementDeleteService evidenceManagementDeleteService,
            RetryExecutor retryExecutor,
            CitizenDocumentsUploadedCorresponder citizenDocumentsUploadedCorresponder,
            CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService) {
            super(
                finremCaseDetailsMapper,
                evidenceManagementDeleteService,
                retryExecutor,
                citizenDocumentsUploadedCorresponder,
                correspondenceEventAuditOrchestrationService
            );
        }

        SendCorrespondenceEvent buildCorrespondenceEventForTest(
            FinremCallbackRequest callbackRequest,
            String userAuthorisation) {
            return buildSendCorrespondenceEvent(
                callbackRequest,
                userAuthorisation
            );
        }

        String correspondenceTaskDescriptionForTest() {
            return correspondenceTaskDescription();
        }

        @Override
        protected NotificationParty notificationParty() {
            return NotificationParty.CITIZEN_APPLICANT;
        }

        @Override
        public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
            return true;
        }

        @Override
        protected Logger getLogger() {
            return LOGGER;
        }
    }
}
