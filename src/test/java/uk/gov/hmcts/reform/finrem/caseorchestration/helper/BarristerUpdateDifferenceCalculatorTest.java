package uk.gov.hmcts.reform.finrem.caseorchestration.helper;

import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerChange;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Barrister;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Organisation;

import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;

public class BarristerUpdateDifferenceCalculatorTest {
    public static final Barrister BARRISTER = Barrister.builder()
        .email("email1")
        .phone("0454444")
        .name("Sandro")
        .organisation(Organisation.builder().organisationID("orgIdOne").build())
        .build();
    public static final Barrister ANOTHER_BARRISTER = Barrister.builder()
        .email("email2")
        .phone("343543543")
        .name("Jonny")
        .organisation(Organisation.builder().organisationID("orgIdTwo").build())
        .build();
    public static final String ANOTHER_EMAIL = "anotherEmail";


    private final BarristerUpdateDifferenceCalculator underTest = new BarristerUpdateDifferenceCalculator();

    @Test
    public void givenEmptyLists_whenCalculateDifference_thenReturnEmptySets() {
        BarristerChange actual = underTest.calculate(
            emptyList(),
            emptyList()
        );

        assertThat(actual).isEqualTo(BarristerChange.builder()
            .added(emptySet())
            .removed(emptySet())
            .build());
    }

    @Test
    public void givenAddedBarrister_whenCalculateDifference_thenReturnAddedSet() {
        BarristerChange actual = underTest.calculate(
            emptyList(),
            List.of(BARRISTER)
        );

        assertThat(actual).isEqualTo(BarristerChange.builder()
            .added(Set.of(BARRISTER))
            .removed(emptySet())
            .build());
    }

    @Test
    public void givenAddedBarristerWithPreExisting_whenCalculateDifference_thenReturnNewBarristerOnly() {
        BarristerChange actual = underTest.calculate(
            List.of(BARRISTER),
            List.of(BARRISTER, ANOTHER_BARRISTER)
        );

        assertThat(actual).isEqualTo(BarristerChange.builder()
            .added(Set.of(ANOTHER_BARRISTER))
            .removed(emptySet())
            .build());
    }

    @Test
    public void givenRemovedBarrister_whenCalculateDifference_thenReturnRemovedBarrister() {
        BarristerChange actual = underTest.calculate(
            List.of(BARRISTER),
            emptyList()
        );

        assertThat(actual).isEqualTo(BarristerChange.builder()
            .added(emptySet())
            .removed(Set.of(BARRISTER))
            .build());
    }

    @Test
    public void givenRemovedBarristerWithPreExisting_whenCalculateDifference_thenReturnBarristerToRemoveOnly()  {
        BarristerChange actual = underTest.calculate(
            List.of(BARRISTER, ANOTHER_BARRISTER),
            List.of(BARRISTER)
        );

        assertThat(actual).isEqualTo(BarristerChange.builder()
            .added(emptySet())
            .removed(Set.of(ANOTHER_BARRISTER))
            .build());
    }

    @Test
    public void givenEmailChangedOnExistingElement_whenCalculateDifference_thenTreatAsNewBarrister() {
        BarristerChange actual = underTest.calculate(
            List.of(BARRISTER),
            List.of(BARRISTER.toBuilder()
                .email(ANOTHER_EMAIL).build())
        );

        assertThat(actual).isEqualTo(BarristerChange.builder()
            .added(Set.of(BARRISTER.toBuilder().email(ANOTHER_EMAIL).build()))
            .removed(Set.of(BARRISTER.toBuilder().build()))
            .build());
    }

    @Test
    public void givenNonRelevantFieldsUpdatedForBarrister_whenCalculateDifference_thenTreatAsNoChange() {
        BarristerChange actual = underTest.calculate(
            List.of(BARRISTER),
            List.of(BARRISTER.toBuilder()
                .phone("23243")
                .organisation(Organisation.builder().organisationID("orgIdTwo").build())
                .name("Another name")
                .build())
        );

        assertThat(actual).isEqualTo(BarristerChange.builder()
            .added(emptySet())
            .removed(emptySet())
            .build());
    }
}