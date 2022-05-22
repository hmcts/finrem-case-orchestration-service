package uk.gov.hmcts.reform.finrem.caseorchestration.service.noc.nocworkflows;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignCaseAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SystemUserService;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.CHANGE_ORGANISATION_REQUEST;


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
        assignCaseAccessService.findAndRevokeCreatorRole(caseDetails);
        Map<String, Object> caseData = noticeOfChangeService.updateRepresentation(caseDetails, authorisationToken,
            originalCaseDetails);

        if (isNoOrganisationsToAddOrRemove(caseDetails)) {
            setDefaultChangeOrganisationRequest(caseDetails);
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseData).build();
        }

        caseDetails.getData().putAll(caseData);
        caseDetails = noticeOfChangeService.persistOriginalOrgPoliciesWhenRevokingAccess(caseDetails,
            originalCaseDetails);

        return assignCaseAccessService.applyDecision(systemUserService.getSysUserToken(), caseDetails);
    }

    private boolean isNoOrganisationsToAddOrRemove(CaseDetails caseDetails) {
        ChangeOrganisationRequest changeRequest =  new ObjectMapper().registerModule(new JavaTimeModule())
            .convertValue(caseDetails.getData().get(CHANGE_ORGANISATION_REQUEST), ChangeOrganisationRequest.class);

        return Optional.ofNullable(changeRequest.getOrganisationToAdd()).isEmpty()
            && Optional.ofNullable(changeRequest.getOrganisationToRemove()).isEmpty();
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
}
