package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AssignToJudgeCorresponderTest {

    @Mock
    AssignToJudgeRespondentCorresponder assignToJudgeRespondentCorresponder;
    @Mock
    AssignToJudgeApplicantCorresponder assignToJudgeApplicantCorresponder;

    AssignToJudgeCorresponder assignToJudgeCorrespondenceService;

    private CaseDetails caseDetails;

    @Before
    public void setUp() throws Exception {
        assignToJudgeCorrespondenceService =
            new AssignToJudgeCorresponder(assignToJudgeApplicantCorresponder, assignToJudgeRespondentCorresponder);
        caseDetails = CaseDetails.builder().build();
    }

    @Test
    public void shouldSendAssignToJudgeCorrespondence() {
        assignToJudgeCorrespondenceService.sendCorrespondence(caseDetails, "authorisationToken");
        verify(assignToJudgeApplicantCorresponder).sendCorrespondence(caseDetails, "authorisationToken");
        verify(assignToJudgeRespondentCorresponder).sendCorrespondence(caseDetails, "authorisationToken");
    }
}