package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourtRefData {
    private String epimmsId;
    private String regionId;
    private String courtName;

}
