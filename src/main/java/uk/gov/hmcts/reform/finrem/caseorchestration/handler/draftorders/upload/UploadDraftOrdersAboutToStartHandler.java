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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadSuggestedDraftOrder;

import java.util.List;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.UPLOAD_PARTY_APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.UPLOAD_PARTY_RESPONDENT;

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

        UploadSuggestedDraftOrder uploadSuggestedDraftOrder = new UploadSuggestedDraftOrder();

        String applicantLName = finremCaseData.getApplicantLastName();
        String respondentLName = finremCaseData.getRespondentLastName();

        DynamicMultiSelectListElement elementConfirmation = DynamicMultiSelectListElement.builder()
            .label("I confirm the uploaded documents are for the " + applicantLName + " v " + respondentLName + " case")
            .code("1")
            .build();

        DynamicMultiSelectList list = DynamicMultiSelectList.builder()
            .listItems(List.of(elementConfirmation))
            .build();

        uploadSuggestedDraftOrder.setConfirmUploadedDocuments(list);

        DynamicRadioListElement elementApplicant = DynamicRadioListElement.builder()
            .code(UPLOAD_PARTY_APPLICANT)
            .label(format("The applicant, %s", finremCaseData.getFullApplicantName()))
            .build();

        DynamicRadioListElement elementRespondent = DynamicRadioListElement.builder()
            .code(UPLOAD_PARTY_RESPONDENT)
            .label(format("The respondent, %s", finremCaseData.getRespondentFullName()))
            .build();

        DynamicRadioList uploadPartyRadioList = DynamicRadioList.builder()
            .listItems(List.of(elementApplicant, elementRespondent))
            .build();

        uploadSuggestedDraftOrder.setUploadParty(uploadPartyRadioList);

        finremCaseData.getDraftOrdersWrapper().setUploadSuggestedDraftOrder(uploadSuggestedDraftOrder);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(finremCaseData).build();
    }

}
