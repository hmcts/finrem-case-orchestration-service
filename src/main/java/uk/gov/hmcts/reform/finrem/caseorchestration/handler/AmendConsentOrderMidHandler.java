package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.ConsentedApplicationHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AmendedConsentOrderCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.BulkPrintDocumentService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AmendConsentOrderMidHandler extends FinremCallbackHandler {

    private final BulkPrintDocumentService bulkPrintDocumentService;

    private final ConsentedApplicationHelper consentedApplicationHelper;

    public AmendConsentOrderMidHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                       BulkPrintDocumentService bulkPrintDocumentService,
                                       ConsentedApplicationHelper consentedApplicationHelper) {
        super(finremCaseDetailsMapper);
        this.bulkPrintDocumentService = bulkPrintDocumentService;
        this.consentedApplicationHelper = consentedApplicationHelper;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.MID_EVENT.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.AMEND_CONSENT_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        log.info(CallbackHandlerLogger.midEvent(callbackRequest));
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        String caseId = caseDetails.getCaseIdAsString();
        FinremCaseData finremCaseData = caseDetails.getData();
        consentedApplicationHelper.setConsentVariationOrderLabelField(callbackRequest.getCaseDetails().getData());

        List<String> errors = new ArrayList<>();

        List<AmendedConsentOrderCollection> amendedCollection = finremCaseData.getAmendedConsentOrderCollection();
        if (amendedCollection != null && !amendedCollection.isEmpty()) {
            FinremCaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
            FinremCaseData finremCaseDataBefore = caseDetailsBefore.getData();
            List<AmendedConsentOrderCollection> amendedCollectionBefore = finremCaseDataBefore.getAmendedConsentOrderCollection();
            if (amendedCollectionBefore != null && !amendedCollectionBefore.isEmpty()) {
                amendedCollection.removeAll(amendedCollectionBefore);
            }
            amendedCollection.forEach(order -> {
                CaseDocument document = order.getValue().getAmendedConsentOrder();
                bulkPrintDocumentService.validateEncryptionOnUploadedDocument(document,
                    caseId, errors, userAuthorisation);
            });
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(finremCaseData).errors(errors).build();
    }
}
