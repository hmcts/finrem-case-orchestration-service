package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ApplicantShareDocumentsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseAssignedRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IntervenerShareDocumentsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.RespondentShareDocumentsService;

import java.util.List;

@Slf4j
@Service
public class ShareSelectedDocumentsAboutToStartHandler extends FinremCallbackHandler {
    private final ApplicantShareDocumentsService applicantDocumentsService;
    private final RespondentShareDocumentsService respondentShareDocumentsService;
    private final IntervenerShareDocumentsService intervenerShareDocumentsService;
    private final CaseAssignedRoleService caseAssignedRoleService;
    private final AssignCaseAccessService accessService;
    private static final String DOC_ERROR = "\nThere are no documents available to share.";
    private static final String PARTY_ERROR = "\nThere is/are no party/parties available to share documents.";

    public ShareSelectedDocumentsAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                     ApplicantShareDocumentsService applicantDocumentsService,
                                                     RespondentShareDocumentsService respondentShareDocumentsService,
                                                     IntervenerShareDocumentsService intervenerShareDocumentsService,
                                                     CaseAssignedRoleService caseAssignedRoleService,
                                                     AssignCaseAccessService accessService) {
        super(finremCaseDetailsMapper);
        this.applicantDocumentsService = applicantDocumentsService;
        this.respondentShareDocumentsService = respondentShareDocumentsService;
        this.intervenerShareDocumentsService = intervenerShareDocumentsService;
        this.caseAssignedRoleService = caseAssignedRoleService;
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

        CaseAssignedUserRolesResource caseAssignedUserRole
            = caseAssignedRoleService.getCaseAssignedUserRole(String.valueOf(caseDetails.getId()), userAuthorisation);
        List<CaseAssignedUserRole> caseAssignedUserRoleList = caseAssignedUserRole.getCaseAssignedUserRoles();
        log.info("logged-in user role {} in case {}", caseAssignedUserRoleList, caseId);
        String loggedInUserCaseRole = caseAssignedUserRoleList.get(0).getCaseRole();
        FinremCaseData caseData = caseDetails.getData();
        if (loggedInUserCaseRole == null) {
            return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData)
                .errors(List.of("Logged in user do not have sufficient role to execute this event")).build();
        }
        List<CaseAssignmentUserRole> allCaseRole = accessService.getAllCaseRole(String.valueOf(caseId));
        log.info("caseAssignedUserRoles {} caseId {}", loggedInUserCaseRole, caseId);
        if (loggedInUserCaseRole.equals(CaseRole.APP_SOLICITOR.getValue()) || loggedInUserCaseRole.equals(CaseRole.APP_BARRISTER.getValue())) {
            DynamicMultiSelectList sourceDocumentList = applicantDocumentsService.applicantSourceDocumentList(caseDetails);
            log.info("Applicant sourceDocumentList {} caseId {}", sourceDocumentList, caseId);
            if (validateSourceDocments(caseData, sourceDocumentList, "applicant", caseId)) {
                return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData)
                    .errors(List.of(DOC_ERROR)).build();
            }

            DynamicMultiSelectList roleList = applicantDocumentsService.getOtherSolicitorRoleList(caseDetails,
                allCaseRole, loggedInUserCaseRole);
            log.info("Applicant roleList {} caseId {}", roleList, caseId);
            if (validateSolicitorList(caseData, roleList, "applicant", caseId)) {
                return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData)
                    .errors(List.of(PARTY_ERROR)).build();
            }
        }
        if (loggedInUserCaseRole.equals(CaseRole.RESP_SOLICITOR.getValue()) || loggedInUserCaseRole.equals(CaseRole.RESP_BARRISTER.getValue())) {
            DynamicMultiSelectList sourceDocumentList = respondentShareDocumentsService.respondentSourceDocumentList(caseDetails);
            log.info("Respondent sourceDocumentList {} caseId {}", sourceDocumentList, caseId);
            if (validateSourceDocments(caseData, sourceDocumentList, "respondent", caseId)) {
                return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData)
                    .errors(List.of(DOC_ERROR)).build();
            }

            DynamicMultiSelectList roleList = respondentShareDocumentsService.getOtherSolicitorRoleList(caseDetails,
                allCaseRole, loggedInUserCaseRole);
            log.info("Respondent roleList {} caseId {}", roleList, caseId);
            if (validateSolicitorList(caseData, roleList, "respondent", caseId)) {
                return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData)
                    .errors(List.of(PARTY_ERROR)).build();
            }
        }
        if (applicantDocumentsService.getIntervenerRoles(loggedInUserCaseRole)) {
            DynamicMultiSelectList sourceDocumentList
                = intervenerShareDocumentsService.intervenerSourceDocumentList(caseDetails, loggedInUserCaseRole);
            log.info("Intervener sourceDocumentList {} caseId {}", sourceDocumentList, caseId);
            if (validateSourceDocments(caseData, sourceDocumentList, "intervener", caseId)) {
                return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData)
                    .errors(List.of(DOC_ERROR)).build();
            }
            DynamicMultiSelectList roleList = intervenerShareDocumentsService.getOtherSolicitorRoleList(caseDetails,
                allCaseRole, loggedInUserCaseRole);
            log.info("Intervener roleList {} caseId {}", roleList, caseId);
            if (validateSolicitorList(caseData, roleList, "intervener", caseId)) {
                return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData)
                    .errors(List.of(PARTY_ERROR)).build();
            }
        }
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }

    private boolean validateSolicitorList(FinremCaseData caseData,
                                                 DynamicMultiSelectList roleList,
                                                 String party,
                                                 Long caseId) {
        String logMessage = "party {} for caseId {}";
        if (roleList != null && ObjectUtils.isNotEmpty(roleList.getListItems())) {
            log.info("Doc If" + logMessage, party, caseId);
            caseData.setSolicitorRoleList(roleList);
        } else {
            log.info("Doc Else" + logMessage, party, caseId);
            return true;
        }
        return false;
    }

    private boolean validateSourceDocments(FinremCaseData caseData,
                                                  DynamicMultiSelectList sourceDocumentList,
                                                  String party,
                                                  Long caseId) {
        String logMessage = "party {} for caseId {}";
        if (sourceDocumentList != null && ObjectUtils.isNotEmpty(sourceDocumentList.getListItems())) {
            log.info("Role If" + logMessage, party, caseId);
            caseData.setSourceDocumentList(sourceDocumentList);
        } else {
            log.info("Role else" + logMessage, party, caseId);
            return true;
        }
        return false;
    }


}