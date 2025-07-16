package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicMultiSelectList;

@SuperBuilder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class HearingWithDynamicList extends Hearing {
    private DynamicList hearingTypeDynamicList;
    private DynamicMultiSelectList partiesOnCaseMultiSelectList;
}