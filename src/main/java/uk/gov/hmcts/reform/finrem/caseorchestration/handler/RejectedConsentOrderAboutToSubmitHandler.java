package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.RefusalOrderDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.RefusedConsentOrderDocumentCategoriser;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RejectedConsentOrderAboutToSubmitHandler implements CallbackHandler<Map<String, Object>> {

    private final RefusalOrderDocumentService refusalOrderDocumentService;
    private final RefusedConsentOrderDocumentCategoriser refusalConsentOrderDocumentCategoriser;
    private final FinremCaseDetailsMapper finremCaseDetailsMapper;

    @Override
    public boolean canHandle(final CallbackType callbackType, final CaseType caseType,
                             final EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && (CaseType.CONTESTED.equals(caseType) || CaseType.CONSENTED.equals(caseType))
            && (EventType.REJECT_ORDER.equals(eventType)
            || EventType.CONSENT_ORDER_NOT_APPROVED.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<Map<String, Object>> handle(CallbackRequest callbackRequest,
                                                                                   String userAuthorisation) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Received request to generate 'Consent Order Not Approved' for Case ID: {}", caseDetails.getId());

        Map<String, Object> caseData = refusalOrderDocumentService.generateConsentOrderNotApproved(userAuthorisation, caseDetails);
        caseDetails.setData(caseData);
        FinremCaseDetails finremCaseDetails = finremCaseDetailsMapper.mapToFinremCaseDetails(caseDetails);
        refusalConsentOrderDocumentCategoriser.categorise(finremCaseDetails.getData());
        Map<String, Object> mappedCaseData = finremCaseDetailsMapper.mapToCaseDetails(finremCaseDetails).getData();
        return GenericAboutToStartOrSubmitCallbackResponse.<Map<String, Object>>builder().data(mappedCaseData).build();
    }
}
