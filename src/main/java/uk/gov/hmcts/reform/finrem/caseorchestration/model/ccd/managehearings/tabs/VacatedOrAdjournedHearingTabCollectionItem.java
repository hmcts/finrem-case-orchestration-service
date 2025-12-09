package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.tabs;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VacatedOrAdjournedHearingTabCollectionItem {
    private UUID id;
    private VacatedOrAdjournedHearingTabItem value;
}
