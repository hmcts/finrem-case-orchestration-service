package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.casemetrics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class CaseMetrics {
    private LocalDate caseClosureDate;
}
