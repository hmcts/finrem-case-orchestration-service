package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.VacateOrAdjournedHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.VacatedOrAdjournedHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.WorkingVacatedHearing;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ManageHearingsWrapperTest {

    private static final UUID testUuid = UUID.fromString("c0a78b4c-5d85-4e50-9d62-219b1b8eb9bb");

    @Test
    void shouldReturnMatchingItemById() {
        UUID matchingId = UUID.randomUUID();
        ManageHearingsCollectionItem itemToMatch = ManageHearingsCollectionItem.builder()
            .id(matchingId)
            .value(new Hearing())
            .build();

        ManageHearingsWrapper wrapper = ManageHearingsWrapper.builder()
            .hearings(List.of(
                ManageHearingsCollectionItem.builder().id(UUID.randomUUID()).build(),
                itemToMatch,
                ManageHearingsCollectionItem.builder().id(UUID.randomUUID()).build()
            ))
            .build();

        ManageHearingsCollectionItem result = wrapper.getManageHearingsCollectionItemById(matchingId);

        assertNotNull(result);
        assertEquals(matchingId, result.getId());
    }

    @Test
    void shouldReturnNullWhenNoMatchFound() {
        ManageHearingsWrapper wrapper = ManageHearingsWrapper.builder()
            .hearings(List.of(
                ManageHearingsCollectionItem.builder().id(UUID.randomUUID()).build()
            ))
            .build();

        ManageHearingsCollectionItem result = wrapper.getManageHearingsCollectionItemById(UUID.randomUUID());

        assertNull(result);
    }

    @Test
    void shouldReturnNullWhenHearingsListIsNull() {
        ManageHearingsWrapper wrapper = ManageHearingsWrapper.builder()
            .hearings(null)
            .build();

        ManageHearingsCollectionItem result = wrapper.getManageHearingsCollectionItemById(UUID.randomUUID());

        assertNull(result);
    }

    // Creates three collection items and checks that we get the right one back when we look by id.
    @Test
    void when_getVacatedOrAdjournedHearingsCollectionItemById_should_match_expected() {
        UUID matchingId = UUID.randomUUID();
        VacatedOrAdjournedHearingsCollectionItem itemToMatch = VacatedOrAdjournedHearingsCollectionItem.builder()
            .id(matchingId)
            .value(new VacateOrAdjournedHearing())
            .build();

        ManageHearingsWrapper wrapper = ManageHearingsWrapper.builder()
            .vacatedOrAdjournedHearings(List.of(
                VacatedOrAdjournedHearingsCollectionItem.builder().id(UUID.randomUUID()).build(),
                itemToMatch,
                VacatedOrAdjournedHearingsCollectionItem.builder().id(UUID.randomUUID()).build()
            ))
            .build();

        VacatedOrAdjournedHearingsCollectionItem result = wrapper.getVacatedOrAdjournedHearingsCollectionItemById(matchingId);

        assertNotNull(result);
        assertEquals(matchingId, result.getId());
    }

    // assert null given for null or empty collections, when we look for a vacated hearing item by id.
    @ParameterizedTest
    @MethodSource("nullVacatedOrAdjournedHearingItems")
    void when_getVacatedOrAdjournedHearingsCollectionItemById_should_return_null (ManageHearingsWrapper hearingsWrapper) {
        assertNull(hearingsWrapper.getVacatedOrAdjournedHearingsCollectionItemById(testUuid));
    }

    private static Stream<Arguments> nullVacatedOrAdjournedHearingItems() {
        return Stream.of(
            // Collection does not exist
            Arguments.of(
                ManageHearingsWrapper.builder().hearings(null).build()
            ),
            // Collection is empty
            Arguments.of(
                ManageHearingsWrapper.builder()
                    .vacatedOrAdjournedHearings(
                        List.of(
                            VacatedOrAdjournedHearingsCollectionItem.builder()
                                .build()))
                    .build()
            )
        );
    }

    @Test
    void when_getWorkingVacatedHearingId_shouldFindWorkingVacatedHearingId() {
        // Arrange
        WorkingVacatedHearing vacatedHearing = WorkingVacatedHearing.builder()
            .chooseHearings(DynamicList.builder()
                .value(DynamicListElement.builder()
                    .code(testUuid.toString())
                    .build())
                .build())
            .build();

        // Act
        UUID expectedUuid = ManageHearingsWrapper.getWorkingVacatedHearingId(vacatedHearing);

        // Assert
        assertThat(expectedUuid).isEqualTo(testUuid);
    }

    @Test
    void when_WorkingVacatedHearing_is_null_getWorkingVacatedHearingId_should_throw() {
        // Arrange
        WorkingVacatedHearing vacatedHearing = WorkingVacatedHearing.builder().build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> ManageHearingsWrapper.getWorkingVacatedHearingId(vacatedHearing));
        assertThat(exception.getMessage()).isEqualTo("Invalid or missing working vacated hearing UUID");
    }

}
