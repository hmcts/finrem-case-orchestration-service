package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.updatefrc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.SingleLetterOrEmailAllPartiesCorresponder;

@Component
@Slf4j
public class UpdateFrcLetterOrEmailAllSolicitorsCorresponder extends SingleLetterOrEmailAllPartiesCorresponder {

    @Autowired
    public UpdateFrcLetterOrEmailAllSolicitorsCorresponder(UpdateFrcApplicantCorresponder updateFrcApplicantCorresponder,
                                                           UpdateFrcRespondentCorresponder updateFrcRespondentCorresponder) {
        super(updateFrcApplicantCorresponder, updateFrcRespondentCorresponder);
    }

}
