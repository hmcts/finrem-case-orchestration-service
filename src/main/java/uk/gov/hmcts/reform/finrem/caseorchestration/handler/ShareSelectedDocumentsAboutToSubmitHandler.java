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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ApplicantShareDocumentsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseAssignedRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IntervenerShareDocumentsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.RespondentShareDocumentsService;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class ShareSelectedDocumentsAboutToSubmitHandler extends FinremCallbackHandler {
    private final CaseAssignedRoleService caseAssignedRoleService;
    private final ApplicantShareDocumentsService applicantShareDocumentsService;
    private final RespondentShareDocumentsService respondentShareDocumentsService;
    private final IntervenerShareDocumentsService intervenerShareDocumentsService;

    public ShareSelectedDocumentsAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                      CaseAssignedRoleService caseAssignedRoleService,
                                                      ApplicantShareDocumentsService applicantShareDocumentsService,
                                                      RespondentShareDocumentsService respondentShareDocumentsService,
                                                      IntervenerShareDocumentsService intervenerShareDocumentsService) {
        super(finremCaseDetailsMapper);
        this.caseAssignedRoleService =  caseAssignedRoleService;
        this.applicantShareDocumentsService = applicantShareDocumentsService;
        this.respondentShareDocumentsService = respondentShareDocumentsService;
        this.intervenerShareDocumentsService =  intervenerShareDocumentsService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.SHARE_SELECTED_DOCUMENTS.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        log.info("Invoking contested {} about to submit callback for case id: {}",
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
                if (caseRole.get().equals(CaseRole.APP_SOLICITOR.getValue()) || caseRole.get().equals(CaseRole.APP_BARRISTER.getValue())) {
                    applicantShareDocumentsService.shareSelectedDocumentWithOtherSelectedSolicitors(caseData);
                }
                if (caseRole.get().equals(CaseRole.RESP_SOLICITOR.getValue()) || caseRole.get().equals(CaseRole.RESP_BARRISTER.getValue())) {
                    respondentShareDocumentsService.shareSelectedDocumentWithOtherSelectedSolicitors(caseData);
                }
                if (caseRole.get().equals(CaseRole.INTVR_SOLICITOR_1.getValue()) || caseRole.get().equals(CaseRole.INTVR_BARRISTER_1.getValue())) {
                    intervenerShareDocumentsService.shareSelectedDocumentWithOtherSelectedSolicitors(caseData);
                }
                if (caseRole.get().equals(CaseRole.INTVR_SOLICITOR_2.getValue()) || caseRole.get().equals(CaseRole.INTVR_BARRISTER_2.getValue())) {
                    intervenerShareDocumentsService.shareSelectedDocumentWithOtherSelectedSolicitors(caseData);
                }
                if (caseRole.get().equals(CaseRole.INTVR_SOLICITOR_3.getValue()) || caseRole.get().equals(CaseRole.INTVR_BARRISTER_3.getValue())) {
                    intervenerShareDocumentsService.shareSelectedDocumentWithOtherSelectedSolicitors(caseData);
                }
                if (caseRole.get().equals(CaseRole.INTVR_SOLICITOR_4.getValue()) || caseRole.get().equals(CaseRole.INTVR_BARRISTER_4.getValue())) {
                    intervenerShareDocumentsService.shareSelectedDocumentWithOtherSelectedSolicitors(caseData);
                }
            });
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).build();
    }


}