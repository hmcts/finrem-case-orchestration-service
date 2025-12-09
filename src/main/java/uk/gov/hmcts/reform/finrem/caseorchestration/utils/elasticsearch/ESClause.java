package uk.gov.hmcts.reform.finrem.caseorchestration.utils.elasticsearch;

public interface ESClause<T> {
    T toMap();
}
