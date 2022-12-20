package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.hearing.formcandg;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.MultiLetterOrEmailAllPartiesCorresponder;

@Component
@Slf4j
public class FormCandGCorresponder extends MultiLetterOrEmailAllPartiesCorresponder {


    public FormCandGCorresponder(
        FormCandGApplicantCorresponder multiLetterOrEmailApplicantCorresponder,
        FormCandGRespondentCorresponder multiLetterOrEmailRespondentCorresponder) {
        super(multiLetterOrEmailApplicantCorresponder, multiLetterOrEmailRespondentCorresponder);
    }
}
