package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BulkPrintWrapper {
    private CaseDocument bulkPrintCoverSheetApp;
    private CaseDocument bulkPrintCoverSheetRes;
    private CaseDocument bulkPrintCoverSheetIntv1;
    private CaseDocument bulkPrintCoverSheetIntv2;
    private CaseDocument bulkPrintCoverSheetIntv3;
    private CaseDocument bulkPrintCoverSheetIntv4;
    private CaseDocument bulkPrintCoverSheetAppConfidential;
    private CaseDocument bulkPrintCoverSheetResConfidential;
    private String bulkPrintLetterIdRes;
    private String bulkPrintLetterIdApp;
}