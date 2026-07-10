package uk.gov.hmcts.reform.finrem.caseorchestration.model;

// Jackson serialises to "V2" or "V3" in case_date JSON, and deserializes from the same values.
public enum EstimatedAssetsChecklistVersion {
    V2,
    V3;
}
