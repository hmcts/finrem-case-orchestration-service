package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DocumentCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ConsentOrderPrintService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.NotificationService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.CONSENT_SEND_ORDER_FOR_APPROVED_ORDER;

/**
 * Prepares the letters for each party and stores them on the case.
 * Nothing is sent here: the submitted callback sends them once the event has been saved.
 */
@Slf4j
@Service
public class SendOrderConsentForApprovedOrderAboutToSubmitHandler extends FinremCallbackHandler {

    private final ConsentOrderPrintService consentOrderPrintService;
    private final NotificationService notificationService;

    public SendOrderConsentForApprovedOrderAboutToSubmitHandler(FinremCaseDetailsMapper mapper,
                                                                ConsentOrderPrintService consentOrderPrintService,
                                                                NotificationService notificationService) {
        super(mapper);
        this.consentOrderPrintService = consentOrderPrintService;
        this.notificationService = notificationService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && CONSENT_SEND_ORDER_FOR_APPROVED_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToSubmit(callbackRequest));
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        FinremCaseData caseData = caseDetails.getData();

        // Only parties without a digital solicitor get a letter. Always overwrite, so an old pack is never re-sent.
        caseData.setConsentOrderApplicantLetterPack(
            notificationService.isApplicantSolicitorDigitalAndEmailPopulated(caseDetails)
                ? null
                : toDocumentCollection(consentOrderPrintService.getApplicantLetterPack(
                caseDetails, caseDetailsBefore, CONSENT_SEND_ORDER_FOR_APPROVED_ORDER, userAuthorisation)));

        caseData.setConsentOrderRespondentLetterPack(
            notificationService.isRespondentSolicitorDigitalAndEmailPopulated(caseDetails)
                ? null
                : toDocumentCollection(consentOrderPrintService.getRespondentLetterPack(
                caseDetails, caseDetailsBefore, CONSENT_SEND_ORDER_FOR_APPROVED_ORDER, userAuthorisation)));

        return response(caseData);
    }

    private List<DocumentCollectionItem> toDocumentCollection(List<CaseDocument> documents) {
        return documents.stream()
            .map(DocumentCollectionItem::fromCaseDocument)
            .collect(Collectors.toCollection(ArrayList::new));
    }
}