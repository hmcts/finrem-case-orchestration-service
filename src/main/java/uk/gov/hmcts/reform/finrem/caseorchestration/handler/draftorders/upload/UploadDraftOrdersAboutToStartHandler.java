package uk.gov.hmcts.reform.finrem.caseorchestration.handler.draftorders.upload;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;

import java.util.List;

@Slf4j
@Service
public class UploadDraftOrdersAboutToStartHandler extends FinremCallbackHandler {

    public UploadDraftOrdersAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper) {
        super(finremCaseDetailsMapper);
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && EventType.DRAFT_ORDERS.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Invoking contested {} about to start callback for Case ID: {}",
            callbackRequest.getEventType(), caseDetails.getId());
        FinremCaseData finremCaseData = caseDetails.getData();

        String applicantLName = finremCaseData.getApplicantLastName();
        String respondentLName = finremCaseData.getRespondentLastName();

        DynamicMultiSelectListElement element = DynamicMultiSelectListElement.builder()
            .label("I confirm the uploaded documents are for the " + applicantLName + " v " + respondentLName)
            .code("1")
            .build();

        DynamicMultiSelectList list = DynamicMultiSelectList.builder()
            .listItems(List.of(element))
            .build();
        finremCaseData.getDraftOrdersWrapper().getUploadSuggestedDraftOrder().setConfirmUploadedDocuments(list);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(finremCaseData).build();
    }

}
