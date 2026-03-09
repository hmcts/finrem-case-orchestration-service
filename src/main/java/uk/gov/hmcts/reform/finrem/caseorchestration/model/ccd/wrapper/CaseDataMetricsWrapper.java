package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.TemporaryField;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.casemetrics.CaseMetrics;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaseDataMetricsWrapper {

    @TemporaryField
    private LocalDate caseClosureDateField;
    @Getter(AccessLevel.NONE)
    private CaseMetrics caseMetrics;

    public CaseMetrics getCaseMetrics() {
        if (caseMetrics == null) {
            this.caseMetrics = new CaseMetrics();
        }
        return caseMetrics;
    }
}
