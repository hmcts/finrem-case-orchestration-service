package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

@Component
@Slf4j
public abstract class SingleLetterOrEmailApplicantCorresponder<D> extends EmailAndLettersCorresponderBase<D> {

    public abstract CaseDocument getDocumentToPrint(D caseDetails, String authorisationToken);

    protected abstract void emailApplicantSolicitor(D caseDetails);

}
