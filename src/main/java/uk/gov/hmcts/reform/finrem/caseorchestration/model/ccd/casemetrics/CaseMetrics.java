package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.casemetrics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class CaseMetrics {
    private LocalDate caseClosureDate;
    private LocalDate caseReopenDate;
}
