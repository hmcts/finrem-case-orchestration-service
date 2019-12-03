package uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.update.in;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.bulkscan.transformation.in.ExceptionRecord;

import java.util.Map;

@Getter
public class BulkScanCaseUpdateRequest {

    private final ExceptionRecord exceptionRecord;
    private final Map<String, Object> caseData;

    public BulkScanCaseUpdateRequest(
        @JsonProperty("exception_record") ExceptionRecord exceptionRecord,
        @JsonProperty("case_details") Map<String, Object> caseData
    ) {
        this.exceptionRecord = exceptionRecord;
        this.caseData = caseData;
    }
}