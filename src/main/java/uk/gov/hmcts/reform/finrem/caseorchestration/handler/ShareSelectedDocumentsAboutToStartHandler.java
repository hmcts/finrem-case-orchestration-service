package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ApplicantShareDocumentsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IntervenerShareDocumentsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.RespondentShareDocumentsService;

import java.util.Collections;

@Slf4j
@Service
public class ShareSelectedDocumentsAboutToStartHandler extends FinremCallbackHandler {
    private final ApplicantShareDocumentsService selectedDocumentsService;
    private final RespondentShareDocumentsService respondentShareDocumentsService;
    private final IntervenerShareDocumentsService oneShareDocumentsService;
    private final AssignCaseAccessService accessService;

    public ShareSelectedDocumentsAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                     ApplicantShareDocumentsService selectedDocumentsService,
                                                     RespondentShareDocumentsService respondentShareDocumentsService,
                                                     IntervenerShareDocumentsService oneShareDocumentsService,
                                                     AssignCaseAccessService accessService) {
        super(finremCaseDetailsMapper);
        this.selectedDocumentsService = selectedDocumentsService;
        this.respondentShareDocumentsService = respondentShareDocumentsService;
        this.oneShareDocumentsService = oneShareDocumentsService;
        this.accessService = accessService;
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
        Long caseId = caseDetails.getId();
        log.info("Invoking contested {} about to start callback for case id: {}",
            callbackRequest.getEventType(), caseId);

        String loggedInUserCaseRole = accessService.getLoggedInUserCaseRole(String.valueOf(caseId), userAuthorisation);
        String error = "";
        if (loggedInUserCaseRole == null) {
            error = "Logged in user do not have sufficient role to execute this event";
        }
        FinremCaseData caseData = caseDetails.getData();
        if (loggedInUserCaseRole != null) {
            log.info("caseAssignedUserRoles {} caseId {}", loggedInUserCaseRole, caseId);

            if (loggedInUserCaseRole.equals(CaseRole.APP_SOLICITOR.getValue()) || loggedInUserCaseRole.equals(CaseRole.APP_BARRISTER.getValue())) {
                DynamicMultiSelectList sourceDocumentList = selectedDocumentsService.applicantSourceDocumentList(caseDetails);
                caseData.setSourceDocumentList(sourceDocumentList);
                DynamicMultiSelectList roleList = selectedDocumentsService.getApplicantToOtherSolicitorRoleList(caseDetails);
                caseData.setSolicitorRoleList(roleList);
            }
            if (loggedInUserCaseRole.equals(CaseRole.RESP_SOLICITOR.getValue()) || loggedInUserCaseRole.equals(CaseRole.RESP_BARRISTER.getValue())) {
                DynamicMultiSelectList sourceDocumentList = respondentShareDocumentsService.respondentSourceDocumentList(caseDetails);
                caseData.setSourceDocumentList(sourceDocumentList);
                DynamicMultiSelectList roleList = respondentShareDocumentsService.getRespondentToOtherSolicitorRoleList(caseDetails);
                caseData.setSolicitorRoleList(roleList);
            }
            if (loggedInUserCaseRole.equals(CaseRole.INTVR_SOLICITOR_1.getValue())
                || loggedInUserCaseRole.equals(CaseRole.INTVR_SOLICITOR_2.getValue())
                || loggedInUserCaseRole.equals(CaseRole.INTVR_SOLICITOR_3.getValue())
                || loggedInUserCaseRole.equals(CaseRole.INTVR_SOLICITOR_4.getValue())
                || loggedInUserCaseRole.equals(CaseRole.INTVR_BARRISTER_1.getValue())
                || loggedInUserCaseRole.equals(CaseRole.INTVR_BARRISTER_2.getValue())
                || loggedInUserCaseRole.equals(CaseRole.INTVR_BARRISTER_3.getValue())
                || loggedInUserCaseRole.equals(CaseRole.INTVR_BARRISTER_4.getValue())) {

                DynamicMultiSelectList sourceDocumentList = oneShareDocumentsService.intervenerSourceDocumentList(caseDetails, loggedInUserCaseRole);
                log.info("sourceDocumentList {} caseId {}", sourceDocumentList, caseId);
                caseData.setSourceDocumentList(sourceDocumentList);
                DynamicMultiSelectList roleList = oneShareDocumentsService.getOtherSolicitorRoleList(caseDetails, loggedInUserCaseRole);
                log.info("roleList {} caseId {}", roleList, caseId);
                caseData.setSolicitorRoleList(roleList);
            }
        }
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData).errors(Collections.singletonList(error)).build();
    }


}