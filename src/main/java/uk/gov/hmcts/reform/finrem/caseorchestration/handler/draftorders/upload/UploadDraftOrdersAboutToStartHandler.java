package uk.gov.hmcts.reform.finrem.caseorchestration.handler.draftorders.upload;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadSuggestedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseAssignedRoleService;

import java.util.List;

@Slf4j
@Service
public class UploadDraftOrdersAboutToStartHandler extends FinremCallbackHandler {

    private final CaseAssignedRoleService caseAssignedRoleService;

    public UploadDraftOrdersAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                CaseAssignedRoleService caseAssignedRoleService) {
        super(finremCaseDetailsMapper);
        this.caseAssignedRoleService = caseAssignedRoleService;
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
        String caseId = String.valueOf(caseDetails.getId());
        log.info("Invoking contested {} about to start callback for Case ID: {}", callbackRequest.getEventType(),
            caseId);
        FinremCaseData finremCaseData = caseDetails.getData();

        UploadSuggestedDraftOrder uploadSuggestedDraftOrder = new UploadSuggestedDraftOrder();

        String applicantLName = finremCaseData.getApplicantLastName();
        String respondentLName = finremCaseData.getRespondentLastName();

        DynamicMultiSelectListElement element = DynamicMultiSelectListElement.builder()
            .label("I confirm the uploaded documents are for the " + applicantLName + " v " + respondentLName + " case")
            .code("1")
            .build();

        DynamicMultiSelectList list = DynamicMultiSelectList.builder()
            .listItems(List.of(element))
            .build();

        uploadSuggestedDraftOrder.setConfirmUploadedDocuments(list);

        finremCaseData.getDraftOrdersWrapper().setUploadSuggestedDraftOrder(uploadSuggestedDraftOrder);

        YesOrNo showUploadPartyQuestion = isShowUploadPartyQuestion(caseId, userAuthorisation)
            ? YesOrNo.YES : YesOrNo.NO;
        finremCaseData.getDraftOrdersWrapper().setShowUploadPartyQuestion(showUploadPartyQuestion);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(finremCaseData).build();
    }

    private boolean isShowUploadPartyQuestion(String caseId, String authToken) {
        CaseAssignedUserRolesResource caseAssignedUserRolesResource =
            caseAssignedRoleService.getCaseAssignedUserRole(caseId, authToken);

        return caseAssignedUserRolesResource.getCaseAssignedUserRoles().isEmpty();
    }
}
