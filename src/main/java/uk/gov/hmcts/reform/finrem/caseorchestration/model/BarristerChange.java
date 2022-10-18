package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;

import java.util.Set;

@Builder
@Value
public class BarristerChange {
    Set<Barrister> added;
    Set<Barrister> removed;
}
