package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ApplicantShareDocumentsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseAssignedRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.RespondentShareDocumentsService;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class ShareSelectedDocumentsAboutToStartHandler extends FinremCallbackHandler {
    private final CaseAssignedRoleService caseAssignedRoleService;
    private final ApplicantShareDocumentsService selectedDocumentsService;
    private final RespondentShareDocumentsService respondentShareDocumentsService;

    public ShareSelectedDocumentsAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                     CaseAssignedRoleService caseAssignedRoleService,
                                                     ApplicantShareDocumentsService selectedDocumentsService,
                                                     RespondentShareDocumentsService respondentShareDocumentsService) {
        super(finremCaseDetailsMapper);
        this.caseAssignedRoleService = caseAssignedRoleService;
        this.selectedDocumentsService = selectedDocumentsService;
        this.respondentShareDocumentsService = respondentShareDocumentsService;
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

        FinremCaseData caseData = caseDetails.getData();
        AtomicReference<String> caseRole = new AtomicReference<>();
        if (caseAssignedUserRoles != null) {
            log.info("caseAssignedUserRoles {}", caseAssignedUserRoles);
            caseAssignedUserRoles.forEach(role -> {
                caseRole.set(role.getCaseRole());
                log.info("User Role {}", caseRole);
                if (caseRole.get().equals(CaseRole.APP_SOLICITOR.getValue())) {
                    DynamicMultiSelectList sourceDocumentList = selectedDocumentsService.applicantSourceDocumentList(caseDetails);
                    caseData.setSourceDocumentList(sourceDocumentList);
                    DynamicMultiSelectList roleList = selectedDocumentsService.getApplicantToOtherSolicitorRoleList(caseDetails);
                    caseData.setSolicitorRoleList(roleList);
                }
                if (caseRole.get().equals(CaseRole.RESP_SOLICITOR.getValue())) {
                    DynamicMultiSelectList sourceDocumentList = respondentShareDocumentsService.respondentSourceDocumentList(caseDetails);
                    caseData.setSourceDocumentList(sourceDocumentList);
                    DynamicMultiSelectList roleList = respondentShareDocumentsService.getRespondentToOtherSolicitorRoleList(caseDetails);
                    caseData.setSolicitorRoleList(roleList);
                }
            });


        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).build();
    }


}