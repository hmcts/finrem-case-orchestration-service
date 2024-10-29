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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicRadioListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.agreed.UploadAgreedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.upload.suggested.UploadSuggestedDraftOrder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.DraftOrdersWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseAssignedRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.HearingService;

import java.util.List;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.CONFIRM_UPLOAD_DOCUMENTS_OPTION_CODE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.UPLOAD_PARTY_APPLICANT;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.draftorders.DraftOrdersConstants.UPLOAD_PARTY_RESPONDENT;

@Slf4j
@Service
public class UploadDraftOrdersAboutToStartHandler extends FinremCallbackHandler {

    private final CaseAssignedRoleService caseAssignedRoleService;

    private final HearingService hearingService;

    public UploadDraftOrdersAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper, CaseAssignedRoleService caseAssignedRoleService,
                                                HearingService hearingService) {
        super(finremCaseDetailsMapper);
        this.caseAssignedRoleService = caseAssignedRoleService;
        this.hearingService = hearingService;
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
        DraftOrdersWrapper draftOrdersWrapper = finremCaseData.getDraftOrdersWrapper();
        draftOrdersWrapper.setTypeOfDraftOrder(null);

        YesOrNo showUploadPartyQuestion = isShowUploadPartyQuestion(caseId, userAuthorisation)
            ? YesOrNo.YES : YesOrNo.NO;
        draftOrdersWrapper.setShowUploadPartyQuestion(showUploadPartyQuestion);

        DynamicMultiSelectList confirmUploadedDocuments = createConfirmUploadDocuments(finremCaseData);
        DynamicRadioList uploadPartyRadioList = null;
        if (YesOrNo.YES.equals(showUploadPartyQuestion)) {
            uploadPartyRadioList = createUploadParty(finremCaseData);
        }

        draftOrdersWrapper.setUploadSuggestedDraftOrder(UploadSuggestedDraftOrder.builder()
            .confirmUploadedDocuments(confirmUploadedDocuments)
            .uploadParty(uploadPartyRadioList)
            .build());

        draftOrdersWrapper.setUploadAgreedDraftOrder(UploadAgreedDraftOrder.builder()
            .hearingDetails(hearingService.generateSelectableHearingsAsDynamicList(caseDetails))
            .confirmUploadedDocuments(confirmUploadedDocuments)
            .uploadParty(uploadPartyRadioList)
            .build());

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(finremCaseData).build();
    }

    private DynamicMultiSelectList createConfirmUploadDocuments(FinremCaseData finremCaseData) {
        String applicantLName = finremCaseData.getApplicantLastName();
        String respondentLName = finremCaseData.getRespondentLastName();

        DynamicMultiSelectListElement elementConfirmation = DynamicMultiSelectListElement.builder()
            .label(format("I confirm the uploaded documents are for the %s v %s case", applicantLName, respondentLName))
            .code(CONFIRM_UPLOAD_DOCUMENTS_OPTION_CODE)
            .build();

        return DynamicMultiSelectList.builder()
            .listItems(List.of(elementConfirmation))
            .build();
    }

    private boolean isShowUploadPartyQuestion(String caseId, String authToken) {
        CaseAssignedUserRolesResource caseAssignedUserRolesResource =
            caseAssignedRoleService.getCaseAssignedUserRole(caseId, authToken);

        return caseAssignedUserRolesResource.getCaseAssignedUserRoles().isEmpty();
    }

    private DynamicRadioList createUploadParty(FinremCaseData finremCaseData) {
        DynamicRadioListElement elementApplicant = DynamicRadioListElement.builder()
            .code(UPLOAD_PARTY_APPLICANT)
            .label(format("The applicant, %s", finremCaseData.getFullApplicantName()))
            .build();

        DynamicRadioListElement elementRespondent = DynamicRadioListElement.builder()
            .code(UPLOAD_PARTY_RESPONDENT)
            .label(format("The respondent, %s", finremCaseData.getRespondentFullName()))
            .build();

        return DynamicRadioList.builder()
            .listItems(List.of(elementApplicant, elementRespondent))
            .build();
    }
}
