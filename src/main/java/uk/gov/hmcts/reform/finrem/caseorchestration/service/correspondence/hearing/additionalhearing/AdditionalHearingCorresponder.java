package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing.additionalhearing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.MultiLetterOrEmailAllPartiesCorresponder;

@Component
@Slf4j
public class AdditionalHearingCorresponder extends MultiLetterOrEmailAllPartiesCorresponder {

    @Autowired
    public AdditionalHearingCorresponder(AdditionalHearingApplicantCorresponder additionalHearingApplicantCorresponder,
                                         AdditionalHearingRespondentCorresponder additionalHearingRespondentCorresponder) {
        super(additionalHearingApplicantCorresponder, additionalHearingRespondentCorresponder);

    }
}
