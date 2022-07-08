package uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.bulkprint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.letterdetails.BasicLetterDetails;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BulkPrintLetterDetails extends BasicLetterDetails {
    private String courtContactDetails;
}
