package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HasCaseDocument;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BulkPrintCoversheetWrapper implements HasCaseDocument {
    private CaseDocument bulkPrintCoverSheetApp;
    private CaseDocument bulkPrintCoverSheetRes;
    private CaseDocument bulkPrintCoverSheetIntv1;
    private CaseDocument bulkPrintCoverSheetIntv2;
    private CaseDocument bulkPrintCoverSheetIntv3;
    private CaseDocument bulkPrintCoverSheetIntv4;
    private CaseDocument bulkPrintCoverSheetAppConfidential;
    private CaseDocument bulkPrintCoverSheetResConfidential;
}
