package uk.gov.hmcts.reform.finrem.caseorchestration.handler.policy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackHandler;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;

@Slf4j
@Service
public class ApplicantChangeOrgPolicyAboutToSubmitHandler extends FinremCallbackHandler {
    @Autowired
    public ApplicantChangeOrgPolicyAboutToSubmitHandler(FinremCaseDetailsMapper mapper) {
        super(mapper);
    }

    @Override
    public boolean canHandle(CallbackType callbackType, CaseType caseType, EventType eventType) {
        return CallbackType.ABOUT_TO_SUBMIT.equals(callbackType)
            && (CaseType.CONTESTED.equals(caseType) || CaseType.CONSENTED.equals(caseType))
            && EventType.CLEAR_APPLICANT_POLICY.equals(eventType);
    }

    @Override
    public GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> handle(FinremCallbackRequest callbackRequest,
                                                                              String userAuthorisation) {

        FinremCaseDetails caseDetails = callbackRequest.getCaseDetails();
        String caseId = String.valueOf(caseDetails.getId());
        log.info("Received callback {} request for event {} for given Case ID: {}", CallbackType.ABOUT_TO_SUBMIT,
            EventType.CLEAR_APPLICANT_POLICY, caseId);

        FinremCaseData caseData = caseDetails.getData();
        OrganisationPolicy policy = caseData.getApplicantOrganisationPolicy();
        log.info("Applicant existing org policy {} for Case ID: {}", policy, caseId);

        OrganisationPolicy organisationPolicy = OrganisationPolicy
            .builder()
            .organisation(Organisation.builder().organisationID(null).organisationName(null).build())
            .orgPolicyReference(null)
            .orgPolicyCaseAssignedRole(CaseRole.APP_SOLICITOR.getCcdCode())
            .build();
        caseData.setApplicantOrganisationPolicy(organisationPolicy);
        caseData.setChangeOrganisationRequestField(null);

        log.info("cleared applicant org policy {} for Case ID: {}", organisationPolicy, caseId);
        return GenericAboutToStartOrSubmitCallbackResponse.<FinremCaseData>builder().data(caseData).build();
    }
}