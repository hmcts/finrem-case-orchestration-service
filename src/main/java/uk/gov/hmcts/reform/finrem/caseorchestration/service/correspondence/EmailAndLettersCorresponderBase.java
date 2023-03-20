package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

public abstract class EmailAndLettersCorresponderBase<D> extends CorresponderBase<D> {

    public abstract void sendCorrespondence(D caseDetails, String authToken);

}
