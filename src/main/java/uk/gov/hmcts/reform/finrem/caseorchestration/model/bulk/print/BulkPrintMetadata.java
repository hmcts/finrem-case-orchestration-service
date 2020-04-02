package uk.gov.hmcts.reform.finrem.caseorchestration.model.bulk.print;

import com.google.common.collect.ImmutableMap;
import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;

import java.util.UUID;

@Builder
@Getter
public class BulkPrintMetadata {
    private final CaseDocument coverSheet;
    private final UUID letterId;

    public ImmutableMap<String, Object> toMap(String coverSheetKey, String letterIdKey) {
        return ImmutableMap.of(
                coverSheetKey, coverSheet,
                letterIdKey, letterId
        );
    }
}