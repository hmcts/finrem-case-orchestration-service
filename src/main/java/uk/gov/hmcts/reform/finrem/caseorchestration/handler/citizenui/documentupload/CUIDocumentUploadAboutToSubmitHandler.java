package uk.gov.hmcts.reform.finrem.caseorchestration.handler.citizenui.documentupload;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremAboutToSubmitCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.CitizenDocumentWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.NotificationParty;
import uk.gov.hmcts.reform.finrem.caseorchestration.notifications.notifiers.SendCorrespondenceEvent;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CorrespondenceEventAuditOrchestrationService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.citizen.CitizenDocumentsUploadedCorresponder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.CUIDocumentsCategoriser;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentsorter.CitizenDocumentSorter;

@Slf4j
public abstract class CUIDocumentUploadAboutToSubmitHandler extends FinremAboutToSubmitCallbackHandler {

    private final CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService;
    private final CitizenDocumentsUploadedCorresponder citizenDocumentsUploadedCorresponder;
    protected final CUIDocumentsCategoriser documentsCategoriser;
    protected final CitizenDocumentSorter citizenDocumentSorter;

    protected CUIDocumentUploadAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                    CorrespondenceEventAuditOrchestrationService correspondenceEventAuditOrchestrationService, CUIDocumentsCategoriser documentsCategoriser,
                                                    CitizenDocumentsUploadedCorresponder citizenDocumentsUploadedCorresponder,
                                                    CitizenDocumentSorter citizenDocumentSorter) {
        super(finremCaseDetailsMapper);
        this.correspondenceEventAuditOrchestrationService = correspondenceEventAuditOrchestrationService;
        this.documentsCategoriser = documentsCategoriser;
        this.citizenDocumentsUploadedCorresponder = citizenDocumentsUploadedCorresponder;
        this.citizenDocumentSorter = citizenDocumentSorter;
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(
        FinremCallbackRequest callbackRequest,
        String userAuthorisation
    ) {
        handleLog(callbackRequest);
        FinremCaseData currentCaseData = callbackRequest.getFinremCaseData();
        categoriseDocuments(currentCaseData);
        sortDocs(currentCaseData.getCitizenDocumentWrapper());
        createNotificationAuditRows(callbackRequest, userAuthorisation);

        return response(currentCaseData);
    }

    private void createNotificationAuditRows(FinremCallbackRequest callbackRequest,
                                             String userAuthorisation) {
        correspondenceEventAuditOrchestrationService.createPendingAudits(
            buildSendCorrespondenceEvent(callbackRequest, userAuthorisation),
            handledEventType()
        );
    }

    private SendCorrespondenceEvent buildSendCorrespondenceEvent(
        FinremCallbackRequest callbackRequest,
        String userAuthorisation) {
        return citizenDocumentsUploadedCorresponder.buildCorrespondenceEvent(
            callbackRequest.getCaseDetails(),
            userAuthorisation,
            notificationParty()
        );
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && handledEventType().equals(eventType);
    }

    protected abstract EventType handledEventType();

    protected abstract NotificationParty notificationParty();

    protected abstract void handleLog(FinremCallbackRequest callbackRequest);

    private void categoriseDocuments(FinremCaseData caseData) {
        documentsCategoriser.categorise(caseData);
    }

    protected abstract void sortDocs(CitizenDocumentWrapper citizenDocumentWrapper);
}
