package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssignToJudgeCorrespondenceService {

    private final AssignToJudgeApplicantCorresponder assignToJudgeApplicantCorresponder;
    private final AssignToJudgeRespondentCorresponder assignToJudgeRespondentCorresponder;

    public void sendCorrespondence(CaseDetails caseDetails, String authorisationToken) {
        log.info("Sending Assign to Judge correspondence for case {}", caseDetails.getId());
        assignToJudgeApplicantCorresponder.sendCorrespondence(caseDetails, authorisationToken);
        assignToJudgeRespondentCorresponder.sendCorrespondence(caseDetails, authorisationToken);

    }

}
