package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CaseFlagsService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.IdamService;

import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_POLICY;

@Slf4j
@Service
public class PaperCaseCreateContestedAboutToSubmitHandler extends FinremCallbackHandler {

    private final CaseFlagsService caseFlagsService;
    private final IdamService idamService;

    public PaperCaseCreateContestedAboutToSubmitHandler(FinremCaseDetailsMapper finremCaseDetailsMapper,
                                                        CaseFlagsService caseFlagsService,
                                                        IdamService idamService) {
        super(finremCaseDetailsMapper);
        this.caseFlagsService = caseFlagsService;
        this.idamService = idamService;
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_START.equals(callbackType)
            && CaseType.CONTESTED.equals(caseType)
            && (EventType.NEW_PAPER_CASE.equals(eventType));
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {
        validateCaseData(callbackRequest);
        caseFlagsService.setCaseFlagInformation(callbackRequest.getCaseDetails());

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        FinremCaseData caseData = caseDetails.getData();
        log.info("Setting default values for contested paper case journey caseid {} event {} about to submit callback",
            caseDetails.getId(), EventType.NEW_PAPER_CASE);

        if (idamService.isUserRoleAdmin(userAuthorisation)) {
            caseData.getContactDetailsWrapper().setIsAdmin(YES_VALUE);
        } else {
            caseData.getContactDetailsWrapper().setIsAdmin(NO_VALUE);
            caseData.getContactDetailsWrapper().setApplicantRepresented(YesOrNo.YES);
        }

        caseData.setPaperApplication(YesOrNo.YES);
        if (caseData.getFastTrackDecision() == null) {
            caseData.setFastTrackDecision(YesOrNo.NO);
        }

        if (!caseData.isApplicantRepresentedByASolicitor()) {
            OrganisationPolicy applicantOrganisationPolicy = OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(APP_SOLICITOR_POLICY)
                .build();
            caseData.setApplicantOrganisationPolicy(applicantOrganisationPolicy);
        }
        if (!caseData.isRespondentRepresentedByASolicitor()) {
            OrganisationPolicy respondentOrganisationPolicy = OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(RESP_SOLICITOR_POLICY)
                .build();
            caseData.setRespondentOrganisationPolicy(respondentOrganisationPolicy);
        }

        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }
}
