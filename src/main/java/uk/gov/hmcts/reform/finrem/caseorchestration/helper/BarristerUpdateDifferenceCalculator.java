package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import com.google.common.collect.Sets;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerChange;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class BarristerUpdateDifferenceCalculator {

    public BarristerChange calculate(List<Barrister> original, List<Barrister> updated) {
        Set<RelevantUniqueInformation> addedUniqueBarristers = Sets.difference(toRelevantSet(updated), toRelevantSet(original));
        Set<RelevantUniqueInformation> removedUniqueBarristers = Sets.difference(toRelevantSet(original), toRelevantSet(updated));

        return BarristerChange.builder()
            .added(toBarristerSet(addedUniqueBarristers))
            .removed(toBarristerSet(removedUniqueBarristers))
            .build();
    }

    private Set<RelevantUniqueInformation> toRelevantSet(List<Barrister> barristers) {
        return barristers.stream()
            .map(this::toRelevantInformation)
            .collect(Collectors.toSet());
    }

    private Set<Barrister> toBarristerSet(Set<RelevantUniqueInformation> uniqueInformation) {
        return uniqueInformation.stream()
            .map(RelevantUniqueInformation::getBarrister)
            .collect(Collectors.toSet());
    }

    private RelevantUniqueInformation toRelevantInformation(Barrister barrister) {
        return new RelevantUniqueInformation(barrister.getEmail(), barrister);
    }

    @Value
    private class RelevantUniqueInformation {
        String email;
        @EqualsAndHashCode.Exclude
        Barrister barrister;
    }
}
