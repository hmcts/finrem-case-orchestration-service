package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

@Component
@Slf4j
public abstract class SingleLetterOrEmailAllPartiesCorresponder {

    private final SingleLetterOrEmailApplicantCorresponder singleLetterOrEmailApplicantCorresponder;
    private final SingleLetterOrEmailRespondentCorresponder singleLetterOrEmailRespondentCorresponder;

    public SingleLetterOrEmailAllPartiesCorresponder(SingleLetterOrEmailApplicantCorresponder singleLetterOrEmailApplicantCorresponder,
                                                     SingleLetterOrEmailRespondentCorresponder singleLetterOrEmailRespondentCorresponder) {
        this.singleLetterOrEmailApplicantCorresponder = singleLetterOrEmailApplicantCorresponder;
        this.singleLetterOrEmailRespondentCorresponder = singleLetterOrEmailRespondentCorresponder;
    }

    public void sendCorrespondence(CaseDetails caseDetails, String authToken) {
        singleLetterOrEmailApplicantCorresponder.sendCorrespondence(caseDetails, authToken);
        singleLetterOrEmailRespondentCorresponder.sendCorrespondence(caseDetails, authToken);
    }
}
