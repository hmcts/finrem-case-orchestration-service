package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.ManageHearingDocumentsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.WorkingVacatedHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.Hearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.ManageHearingsCollectionItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.VacateOrAdjournedHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.managehearings.hearings.VacatedOrAdjournedHearingsCollectionItem;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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
    void when_getVacatedOrAdjournedHearingsCollectionItemById_should_return_null(ManageHearingsWrapper hearingsWrapper) {
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
        ManageHearingsWrapper wrapper = ManageHearingsWrapper.builder()
            .workingVacatedHearing(
                WorkingVacatedHearing.builder()
                    .chooseHearings(DynamicList.builder()
                        .value(DynamicListElement.builder()
                            .code(testUuid.toString())
                            .build())
                        .build())
                    .build())
            .build();

        // Act
        UUID expectedUuid = wrapper.getWorkingVacatedHearingId();

        // Assert
        assertThat(expectedUuid).isEqualTo(testUuid);
    }

    @Test
    void getAssociatedWorkingHearingDocuments_shouldReturnAssociatedDocuments_WhenHearingIdMatches() {
        UUID hearingId = UUID.randomUUID();
        CaseDocument doc1 = CaseDocument.builder().documentUrl("url1").documentFilename("file1").build();
        CaseDocument doc2 = CaseDocument.builder().documentUrl("url2").documentFilename("file2").build();

        ManageHearingDocumentsCollectionItem item1 = ManageHearingDocumentsCollectionItem.builder()
                .value(ManageHearingDocument.builder().hearingId(hearingId).hearingDocument(doc1).build())
                .build();
        ManageHearingDocumentsCollectionItem item2 = ManageHearingDocumentsCollectionItem.builder()
                .value(ManageHearingDocument.builder().hearingId(hearingId).hearingDocument(doc2).build())
                .build();

        ManageHearingsWrapper wrapper = ManageHearingsWrapper.builder()
                .workingHearingId(hearingId)
                .hearingDocumentsCollection(List.of(item1, item2))
                .build();

        List<CaseDocument> result = wrapper.getAssociatedWorkingHearingDocuments();

        assertNotNull(result);
        assertThat(result).containsExactlyInAnyOrder(doc1, doc2);
    }

    @Test
    void getAssociatedWorkingHearingDocuments_shouldReturnEmptyList_WhenNoMatchFoundForAssociatedDocuments() {
        UUID hearingId = UUID.randomUUID();
        UUID nonMatchingId = UUID.randomUUID();
        CaseDocument doc1 = CaseDocument.builder().documentUrl("url1").documentFilename("file1").build();

        ManageHearingDocumentsCollectionItem item1 = ManageHearingDocumentsCollectionItem.builder()
                .value(ManageHearingDocument.builder().hearingId(nonMatchingId).hearingDocument(doc1).build())
                .build();

        ManageHearingsWrapper wrapper = ManageHearingsWrapper.builder()
                .workingHearingId(hearingId)
                .hearingDocumentsCollection(List.of(item1))
                .build();

        List<CaseDocument> result = wrapper.getAssociatedWorkingHearingDocuments();

        assertNotNull(result);
        assertThat(result).isEmpty();
    }

    @Test
    void getAssociatedWorkingHearingDocuments_shouldReturnEmptyList_WhenHearingDocumentsCollectionIsNull() {
        UUID hearingId = UUID.randomUUID();

        ManageHearingsWrapper wrapper = ManageHearingsWrapper.builder()
                .workingHearingId(hearingId)
                .hearingDocumentsCollection(null)
                .build();

        List<CaseDocument> result = wrapper.getAssociatedWorkingHearingDocuments();

        assertNotNull(result);
        assertThat(result).isEmpty();
    }

    @Test
    void getAssociatedWorkingHearingDocuments_shouldReturnEmptyList_WhenWorkingHearingIdIsNull() {
        CaseDocument doc1 = CaseDocument.builder().documentUrl("url1").documentFilename("file1").build();

        ManageHearingDocumentsCollectionItem item1 = ManageHearingDocumentsCollectionItem.builder()
                .value(ManageHearingDocument.builder().hearingId(UUID.randomUUID()).hearingDocument(doc1).build())
                .build();

        ManageHearingsWrapper wrapper = ManageHearingsWrapper.builder()
                .workingHearingId(null)
                .hearingDocumentsCollection(List.of(item1))
                .build();

        List<CaseDocument> result = wrapper.getAssociatedWorkingHearingDocuments();

        assertNotNull(result);
        assertThat(result).isEmpty();
    }
}
