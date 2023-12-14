package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseRole;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_FIRST_MIDDLE_NAME;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APPLICANT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.APP_SOLICITOR_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CHANGE_ORGANISATION_REQUEST;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESPONDENT_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.RESP_SOLICITOR_POLICY;


@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateRepresentationWorkflowService {

    @Autowired
    private final NoticeOfChangeService noticeOfChangeService;

    @Autowired
    private final AssignCaseAccessService assignCaseAccessService;

    @Autowired
    private final SystemUserService systemUserService;

    public AboutToStartOrSubmitCallbackResponse handleNoticeOfChangeWorkflow(CaseDetails caseDetails,
                                                                             String authorisationToken,
                                                                             CaseDetails originalCaseDetails) {
        log.info("Received request to update representation on case with Case ID: {}", caseDetails.getId());

        log.info("DEBUGGING NOC - handleNoticeOfChangeWorkflow entered and applicant name is still present {} with Case ID: {}",
            ObjectUtils.nullSafeConciseToString(caseDetails.getData().get(APPLICANT_FIRST_MIDDLE_NAME)), caseDetails.getId());

        assignCaseAccessService.findAndRevokeCreatorRole(caseDetails);

        log.info("DEBUGGING NOC - findAndRevokeCreatorRole entered and applicant name is still present {} with Case ID: {}",
            ObjectUtils.nullSafeConciseToString(caseDetails.getData().get(APPLICANT_FIRST_MIDDLE_NAME)), caseDetails.getId());
        log.info("findAndRevokeCreatorRole executed {}", caseDetails.getId());
        Map<String, Object> caseData = noticeOfChangeService.updateRepresentation(caseDetails, authorisationToken,
            originalCaseDetails);
        log.info("DEBUGGING NOC - noticeOfChangeService.updateRepresentation entered and applicant name is still present {} with Case ID: {}",
            ObjectUtils.nullSafeConciseToString(caseDetails.getData().get(APPLICANT_FIRST_MIDDLE_NAME)), caseDetails.getId());

        if (isNoOrganisationsToAddOrRemove(caseDetails)) {
            persistDefaultOrganisationPolicy(caseDetails);
            log.info("DEBUGGING NOC - persistDefaultOrganisationPolicy entered and applicant name is still present {} with Case ID: {}",
                ObjectUtils.nullSafeConciseToString(caseDetails.getData().get(APPLICANT_FIRST_MIDDLE_NAME)), caseDetails.getId());
            setDefaultChangeOrganisationRequest(caseDetails);
            log.info("DEBUGGING NOC - setDefaultChangeOrganisationRequest entered and applicant name is still present {} with Case ID: {}",
                ObjectUtils.nullSafeConciseToString(caseDetails.getData().get(APPLICANT_FIRST_MIDDLE_NAME)), caseDetails.getId());
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build();
        }

        caseDetails.getData().putAll(caseData);
        caseDetails = noticeOfChangeService.persistOriginalOrgPoliciesWhenRevokingAccess(caseDetails,
            originalCaseDetails);
        log.info("DEBUGGING NOC - noticeOfChangeService.persistOriginalOrgPoliciesWhenRevokingAccess "
                + "entered and applicant name is still present {} with Case ID: {}",
            ObjectUtils.nullSafeConciseToString(caseDetails.getData().get(APPLICANT_FIRST_MIDDLE_NAME)), caseDetails.getId());

        return assignCaseAccessService.applyDecision(systemUserService.getSysUserToken(), caseDetails);
    }

    private boolean isNoOrganisationsToAddOrRemove(CaseDetails caseDetails) {
        ChangeOrganisationRequest changeRequest = new ObjectMapper().registerModule(new JavaTimeModule())
            .convertValue(caseDetails.getData().get(CHANGE_ORGANISATION_REQUEST), ChangeOrganisationRequest.class);
        return isOrganisationsEmpty(changeRequest);
    }

    private void setDefaultChangeOrganisationRequest(CaseDetails caseDetails) {
        ChangeOrganisationRequest defaultRequest = ChangeOrganisationRequest.builder()
            .requestTimestamp(null)
            .organisationToAdd(null)
            .organisationToRemove(null)
            .approvalRejectionTimestamp(null)
            .approvalStatus(null)
            .caseRoleId(null)
            .reason(null)
            .build();

        caseDetails.getData().put(CHANGE_ORGANISATION_REQUEST, defaultRequest);
    }

    private boolean isOrganisationsEmpty(ChangeOrganisationRequest changeRequest) {
        boolean addedIsEmpty = Optional.ofNullable(changeRequest.getOrganisationToAdd()).isEmpty()
            || Optional.ofNullable(changeRequest.getOrganisationToAdd().getOrganisationID()).isEmpty();

        boolean removedIsEmpty = Optional.ofNullable(changeRequest.getOrganisationToRemove()).isEmpty()
            || Optional.ofNullable(changeRequest.getOrganisationToRemove().getOrganisationID()).isEmpty();

        return addedIsEmpty && removedIsEmpty;
    }

    public void persistDefaultOrganisationPolicy(CaseDetails caseDetails) {
        if (noticeOfChangeService.hasInvalidOrgPolicy(caseDetails, true)) {
            persistDefaultApplicantOrganisationPolicy(caseDetails);
        }
        if (noticeOfChangeService.hasInvalidOrgPolicy(caseDetails, false)) {
            persistDefaultRespondentOrganisationPolicy(caseDetails);
        }
    }

    public void persistDefaultOrganisationPolicy(FinremCaseData caseData) {
        String ccdCaseId = caseData.getCcdCaseId();
        OrganisationPolicy appPolicy = caseData.getApplicantOrganisationPolicy();
        log.info("Applicant existing org policy {} for Case ID: {}", appPolicy, ccdCaseId);
        if (appPolicy == null) {
            OrganisationPolicy organisationPolicy = getOrganisationPolicy(CaseRole.APP_SOLICITOR);
            caseData.setApplicantOrganisationPolicy(organisationPolicy);
        }
        OrganisationPolicy respPolicy = caseData.getRespondentOrganisationPolicy();
        log.info("Respondent existing org policy {} for Case ID: {}", respPolicy, ccdCaseId);
        if (respPolicy == null) {
            OrganisationPolicy organisationPolicy = getOrganisationPolicy(CaseRole.RESP_SOLICITOR);
            caseData.setRespondentOrganisationPolicy(organisationPolicy);
        }
    }

    private void persistDefaultApplicantOrganisationPolicy(CaseDetails caseDetails) {
        caseDetails.getData().put(APPLICANT_ORGANISATION_POLICY,
            OrganisationPolicy.builder()
                .orgPolicyReference(null)
                .orgPolicyCaseAssignedRole(APP_SOLICITOR_POLICY)
                .build());
    }

    private void persistDefaultRespondentOrganisationPolicy(CaseDetails caseDetails) {
        caseDetails.getData().put(RESPONDENT_ORGANISATION_POLICY,
            OrganisationPolicy.builder()
                .orgPolicyReference(null)
                .orgPolicyCaseAssignedRole(RESP_SOLICITOR_POLICY)
                .build());
    }

    private OrganisationPolicy getOrganisationPolicy(CaseRole role) {
        return OrganisationPolicy
            .builder()
            .organisation(Organisation.builder().organisationID(null).organisationName(null).build())
            .orgPolicyReference(null)
            .orgPolicyCaseAssignedRole(role.getCcdCode())
            .build();
    }
}
