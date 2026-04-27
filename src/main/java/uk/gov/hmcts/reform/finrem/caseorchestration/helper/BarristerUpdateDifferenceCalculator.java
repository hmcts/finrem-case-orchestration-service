package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import com.google.common.collect.Sets;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerChange;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.BarristerParty;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class BarristerUpdateDifferenceCalculator {

    /**
     * Calculates the difference between the original and updated lists of barristers
     * for a given {@link BarristerParty}. It determines which barristers have been
     * added and which have been removed based on their relevant unique information.
     *
     * <p>The comparison is performed by converting both input lists into sets of
     * {@link RelevantUniqueInformation}, and then computing the set differences.</p>
     *
     * @param barristerParty the party to which the barristers belong
     * @param original the original list of barristers
     * @param updated the updated list of barristers
     * @return a {@link BarristerChange} containing the added and removed barristers;
     *         never {@code null}
     */
    public BarristerChange calculate(BarristerParty barristerParty, List<Barrister> original, List<Barrister> updated) {
        Set<RelevantUniqueInformation> addedUniqueBarristers = Sets.difference(toRelevantSet(updated), toRelevantSet(original));
        Set<RelevantUniqueInformation> removedUniqueBarristers = Sets.difference(toRelevantSet(original), toRelevantSet(updated));

        return BarristerChange.builder()
            .barristerParty(barristerParty)
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
