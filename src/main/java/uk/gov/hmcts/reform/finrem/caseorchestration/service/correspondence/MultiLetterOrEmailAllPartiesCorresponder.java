package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

@Component
@Slf4j
public abstract class MultiLetterOrEmailAllPartiesCorresponder {

    private final MultiLetterOrEmailApplicantCorresponder multiLetterOrEmailApplicantCorresponder;
    private final MultiLetterOrEmailRespondentCorresponder multiLetterOrEmailRespondentCorresponder;

    @Autowired
    public MultiLetterOrEmailAllPartiesCorresponder(MultiLetterOrEmailApplicantCorresponder multiLetterOrEmailApplicantCorresponder,
                                                    MultiLetterOrEmailRespondentCorresponder multiLetterOrEmailRespondentCorresponder) {
        this.multiLetterOrEmailApplicantCorresponder = multiLetterOrEmailApplicantCorresponder;
        this.multiLetterOrEmailRespondentCorresponder = multiLetterOrEmailRespondentCorresponder;
    }

    public void sendCorrespondence(CaseDetails caseDetails, String authorisationToken) {
        multiLetterOrEmailApplicantCorresponder.sendCorrespondence(caseDetails, authorisationToken);
        multiLetterOrEmailRespondentCorresponder.sendCorrespondence(caseDetails, authorisationToken);
    }


}
