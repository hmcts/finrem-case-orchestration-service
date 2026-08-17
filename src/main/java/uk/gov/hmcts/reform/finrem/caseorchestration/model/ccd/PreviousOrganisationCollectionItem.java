package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(generate = false)
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PreviousOrganisationCollectionItem {

    private String id;

    private PreviousOrganisation value;
}
