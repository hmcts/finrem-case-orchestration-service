package uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence;

public abstract class EmailOnlyCorresponderBase<D> extends CorresponderBase<D> {

    public abstract void sendCorrespondence(D caseDetails);

}
