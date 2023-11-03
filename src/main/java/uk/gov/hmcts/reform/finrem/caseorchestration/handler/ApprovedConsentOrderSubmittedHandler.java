package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.helper.DocumentHelper;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataConsented;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDataContested;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.consentorder.FinremConsentOrderAvailableCorresponder;

import java.util.List;

@Slf4j
@Service
public class ApprovedConsentOrderSubmittedHandler extends FinremCallbackHandler<FinremCaseDataConsented> {

    private final FinremConsentOrderAvailableCorresponder consentOrderAvailableCorresponder;
    private final DocumentHelper documentHelper;

    @Autowired
    public ApprovedConsentOrderSubmittedHandler(
        FinremConsentOrderAvailableCorresponder consentOrderAvailableCorresponder,
        FinremCaseDetailsMapper finremCaseDetailsMapper, DocumentHelper documentHelper) {
        super(finremCaseDetailsMapper);
        this.consentOrderAvailableCorresponder = consentOrderAvailableCorresponder;
        this.documentHelper = documentHelper;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.SUBMITTED.equals(callbackType)
            && CaseType.CONSENTED.equals(caseType)
            && EventType.APPROVE_ORDER.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseDataConsented> handle(
        FinremCallbackRequest<FinremCaseDataConsented> callbackRequest, String userAuthorisation) {
        FinremCaseDetails<FinremCaseDataConsented> caseDetails = callbackRequest.getCaseDetails();
        FinremCaseDataConsented caseData = caseDetails.getData();
        if (Boolean.TRUE.equals(isPensionDocumentsEmpty(caseData))) {
            consentOrderAvailableCorresponder.sendCorrespondence(caseDetails);
        }

        return GenericAboutToStartOrSubmitCallbackResponse
            .<FinremCaseDataConsented>builder()
            .data(callbackRequest.getCaseDetails().getData())
            .build();
    }

    private Boolean isPensionDocumentsEmpty(FinremCaseDataConsented caseData) {
        List<CaseDocument> pensionDocumentsData = documentHelper.getPensionDocumentsData(caseData);
        return pensionDocumentsData.isEmpty();
    }

}
