package uk.gov.hmcts.reform.finrem.caseorchestration.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.AssignCaseAccessRequest;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssignCaseAccessRequestMapper {
    public AssignCaseAccessRequest mapToAssignCaseAccessRequest(CaseDetails caseDetails, String userId) {
        return AssignCaseAccessRequest
            .builder()
            .caseId(caseDetails.getId().toString())
            .assigneeId(userId)
            .caseTypeId(caseDetails.getCaseTypeId())
            .build();
    }
}
