package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseAssignedRoleService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ShareSelectedDocumentsAboutToStartHandler extends FinremCallbackHandler {
    private final CaseAssignedRoleService caseAssignedRoleService;
    public ShareSelectedDocumentsAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                     CaseAssignedRoleService caseAssignedRoleService) {
        super(finremCaseDetailsMapper);
        this.caseAssignedRoleService = caseAssignedRoleService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.SHARE_SELECTED_DOCUMENTS.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Invoking contested {} about to start callback for case id: {}",
            callbackRequest.getEventType(), caseDetails.getId());

        CaseAssignedUserRolesResource caseAssignedUserRole
            = caseAssignedRoleService.getCaseAssignedUserRole(String.valueOf(caseDetails.getId()), userAuthorisation);
        List<CaseAssignedUserRole> caseAssignedUserRoles = caseAssignedUserRole.getCaseAssignedUserRoles();

        if (caseAssignedUserRoles != null) {
            log.info("caseAssignedUserRoles {}", caseAssignedUserRoles);
            caseAssignedUserRoles.forEach(x -> {
                log.info("User Role {}", x.getCaseRole());
                log.info("User Id {}", x.getUserId());
            });
        }


        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
        List<UploadCaseDocumentCollection> appOtherCollectionShared = caseData.getUploadCaseDocumentWrapper().getAppOtherCollection();

        List<DynamicMultiSelectListElement> dynamicListElements = new ArrayList<>();


        appOtherCollectionShared.forEach(doc -> dynamicListElements.add(getDynamicMultiSelectListElement(doc.getId()
                .toString(),doc.getValue().getCaseDocuments().getDocumentFilename())));

        DynamicMultiSelectList applicantDocuments = caseData.getApplicantDocuments();
        DynamicMultiSelectList dynamicList = getDynamicMultiSelectList(dynamicListElements, applicantDocuments);


        caseData.setApplicantDocuments(dynamicList);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).build();
    }

    private DynamicMultiSelectListElement getDynamicMultiSelectListElement(String code, String label) {
        return DynamicMultiSelectListElement.builder()
                .code(code)
                .label(label)
                .build();
    }

    private DynamicMultiSelectList getDynamicMultiSelectList(List<DynamicMultiSelectListElement> dynamicMultiSelectListElement,
                                                             DynamicMultiSelectList selectedDocuments) {
        if (selectedDocuments != null) {
            return DynamicMultiSelectList.builder()
                    .value(selectedDocuments.getValue())
                    .listItems(dynamicMultiSelectListElement)
                    .build();
        } else {
            return DynamicMultiSelectList.builder()
                    .listItems(dynamicMultiSelectListElement)
                    .build();
        }
    }
}