package uk.gov.hmcts.reform.finrem.caseorchestration.utils.csv;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Builder;
import lombok.EqualsAndHashCode;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@EqualsAndHashCode
@JsonPropertyOrder(value = {"caseReference"})
@JsonRootName("idamUser")
public class CaseReference {

    @JsonProperty
    private String caseReference;

    public CaseReference() {
    }

    public CaseReference(String caseReference) {
        this.caseReference = caseReference;
    }

    public String getCaseReference() {
        return caseReference;
    }

    public void setCaseReference(String caseReference) {
        this.caseReference = caseReference;
    }

}
