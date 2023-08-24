package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GeneralEmailService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.GenericDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

@Slf4j
@Service
public class GeneralEmailAboutToSubmitHandler extends FinremCallbackHandler {

    private final NotificationService notificationService;
    private final GeneralEmailService generalEmailService;
    private final GenericDocumentService genericDocumentService;

    @Autowired
    public GeneralEmailAboutToSubmitHandler(FinremCaseDetailsMapper mapper,
                                            NotificationService notificationService,
                                            GeneralEmailService generalEmailService,
                                            GenericDocumentService genericDocumentService) {
        super(mapper);
        this.notificationService = notificationService;
        this.generalEmailService = generalEmailService;
        this.genericDocumentService = genericDocumentService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && (CaseType.CONSENTED.equals(caseType) || CaseType.CONTESTED.equals(caseType))
            && (EventType.CREATE_GENERAL_EMAIL.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info("Received request to send general email for Case ID: {}", callbackRequest.getCaseDetails().getId());
        validateCaseData(callbackRequest);

        log.info("Sending general email notification for Case ID: {}", callbackRequest.getCaseDetails().getId());
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseDocument generalEmailUploadedDocument = caseDetails.getData().getGeneralEmailWrapper().getGeneralEmailUploadedDocument();
        if (generalEmailUploadedDocument != null) {
            CaseDocument pdfDocument = genericDocumentService.convertDocumentIfNotPdfAlready(generalEmailUploadedDocument,
                userAuthorisation, caseDetails.getId().toString());
            caseDetails.getData().getGeneralEmailWrapper().setGeneralEmailUploadedDocument(pdfDocument);
        }
        if (caseDetails.getData().isConsentedApplication()) {
            notificationService.sendConsentGeneralEmail(caseDetails, userAuthorisation);
        } else {
            notificationService.sendContestedGeneralEmail(caseDetails, userAuthorisation);
        }

        generalEmailService.storeGeneralEmail(caseDetails);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseDetails.getData()).build();
    }

}
