package uk.gov.hmcts.reform.finrem.caseorchestration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;


@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateRepresentationWorkflowService {

    @Autowired
    private final NoticeOfChangeService noticeOfChangeService;

    @Autowired private final AssignCaseAccessService assignCaseAccessService;

    @Autowired private final SystemUserService systemUserService;

    public AboutToStartOrSubmitCallbackResponse handleNoticeOfChangeWorkflow(CaseDetails caseDetails,
                                                                                             String authorisationToken,
                                                                                             CaseDetails originalCaseDetails) {
        Map<String, Object> caseData = caseDetails.getData();
        log.info("Received request to update representation on case with Case ID: {}", caseDetails.getId());
        assignCaseAccessService.findAndRevokeCreatorRole(caseDetails);
        caseData = noticeOfChangeService.updateRepresentation(caseDetails, authorisationToken, originalCaseDetails);
        caseDetails.getData().putAll(caseData);
        caseDetails = noticeOfChangeService.persistOriginalOrgPoliciesWhenRevokingAccess(caseDetails, originalCaseDetails);

        AboutToStartOrSubmitCallbackResponse response = assignCaseAccessService.applyDecision(
            systemUserService.getSysUserToken(),
            caseDetails);

        log.info("Response from Manage case service for caseID {}: {}", caseDetails.getId(), response);
        return response;
    }
}
