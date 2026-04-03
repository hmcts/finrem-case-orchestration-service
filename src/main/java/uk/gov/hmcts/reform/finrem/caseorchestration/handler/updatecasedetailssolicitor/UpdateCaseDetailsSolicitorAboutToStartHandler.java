package uk.gov.hmcts.reform.finrem.caseorchestration.handler.updatecasedetailssolicitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.CallbackHandlerLogger;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseAssignedRoleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseDataService;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_POLICY;

@Slf4j
@Service
public class UpdateCaseDetailsSolicitorAboutToStartHandler extends FinremCallbackHandler {

    private final CaseAssignedRoleService caseAssignedRoleService;
    private final CaseDataService caseDataService;

    public UpdateCaseDetailsSolicitorAboutToStartHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                         CaseAssignedRoleService caseAssignedRoleService,
                                                         CaseDataService caseDataService) {
        super(finremCaseDetailsMapper);
        this.caseAssignedRoleService = caseAssignedRoleService;
        this.caseDataService = caseDataService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && (CaseType.CONTESTED.equals(caseType) || CaseType.CONSENTED.equals(caseType))
            && EventType.UPDATE_CASE_DETAILS_SOLICITOR.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest, String userAuthorisation) {
        log.info(CallbackHandlerLogger.aboutToStart(callbackRequest));

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();
        String solicitorCaseRole = getCaseAssignedUserRole(caseDetails, userAuthorisation);

        updateRepresentationFields(caseDetails, solicitorCaseRole);
        setFieldShowConditions(caseData, solicitorCaseRole);

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder()
            .data(caseData)
            .build();
    }

    private void updateRepresentationFields(FinremCaseDetails finremCaseDetails,
                                                  String solicitorCaseRole) {
        if (APP_SOLICITOR_POLICY.equals(solicitorCaseRole)) {
            finremCaseDetails.getData().getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        } else if (RESP_SOLICITOR_POLICY.equals(solicitorCaseRole)) {
            boolean isConsented = caseDataService.isConsentedApplication(finremCaseDetails);
            if (isConsented) {
                finremCaseDetails.getData().getContactDetailsWrapper().setConsentedRespondentRepresented(YesOrNo.YES);
            } else {
                finremCaseDetails.getData().getContactDetailsWrapper().setContestedRespondentRepresented(YesOrNo.YES);
            }
        }
    }

    /*
     * The EXUI journey for Update Contact Details uses page 1 for Applicant Solicitor and page 2 for Respondent Solicitor.
     * CCD requires a unique event/field ID for every field. So this sets two field show conditions, one for each page.
     */
    private void setFieldShowConditions(FinremCaseData finremCaseData, String solicitorCaseRole) {
        if (CaseRole.APP_SOLICITOR.getCcdCode().equals(solicitorCaseRole)) {
            finremCaseData.getContactDetailsWrapper().setCurrentUserIsApplicantSolicitor(YesOrNo.YES);
        } else if (CaseRole.RESP_SOLICITOR.getCcdCode().equals(solicitorCaseRole)) {
            finremCaseData.getContactDetailsWrapper().setCurrentUserIsRespondentSolicitor(YesOrNo.YES);
        }
    }

    private String getCaseAssignedUserRole(FinremCaseDetails finremCaseDetails, String authToken) {
        CaseAssignedUserRolesResource resource = caseAssignedRoleService.getCaseAssignedUserRole(finremCaseDetails.getCaseIdAsString(), authToken);
        return resource.getCaseAssignedUserRoles().getFirst().getCaseRole();
    }
}
