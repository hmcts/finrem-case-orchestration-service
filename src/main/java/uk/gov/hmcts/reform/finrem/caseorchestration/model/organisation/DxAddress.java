package uk.gov.hmcts.reform.finrem.caseorchestration.model.organisation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DxAddress {
    private String dxExchange;
    private String dxNumber;
}
