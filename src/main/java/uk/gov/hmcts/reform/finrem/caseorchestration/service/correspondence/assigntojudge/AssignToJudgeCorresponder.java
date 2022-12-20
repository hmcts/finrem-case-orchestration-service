package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.SingleLetterOrEmailAllPartiesCorresponder;

@Component
@Slf4j
public class AssignToJudgeCorresponder extends SingleLetterOrEmailAllPartiesCorresponder {

    public AssignToJudgeCorresponder(
        AssignToJudgeApplicantCorresponder assignToJudgeApplicantCorresponder,
        AssignToJudgeRespondentCorresponder assignToJudgeRespondentCorresponder) {
        super(assignToJudgeApplicantCorresponder, assignToJudgeRespondentCorresponder);
    }
}
