package uk.gov.hmcts.reform.finrem.caseorchestration.model;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;

import java.util.HashSet;
import java.util.Set;

@Builder
@Value
public class BarristerChange {
    Set<Barrister> added;
    Set<Barrister> removed;

    public Set<Barrister> getRemoved() {
        return this.removed == null ? new HashSet<>() : this.removed;
    }

    public Set<Barrister> getAdded() {
        return this.added == null ? new HashSet<>() : this.added;
    }
}
