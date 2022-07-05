package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BulkPrintLetterDetails extends BasicLetterDetails {
    private String courtContactDetails;
}
