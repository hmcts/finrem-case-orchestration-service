package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocumentType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicList;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.DynamicListElement;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        CaseDocument doc1 = caseDocument("file1", "url1");
        CaseDocument doc2 = caseDocument("file2", "url2");

        ManageHearingDocumentsCollectionItem item1 = hearingDocumentItem(hearingId, doc1);
        ManageHearingDocumentsCollectionItem item2 = hearingDocumentItem(hearingId, doc2);

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
        CaseDocument doc1 = caseDocument("file1", "url1");

        ManageHearingDocumentsCollectionItem item1 = hearingDocumentItem(nonMatchingId, doc1);

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
        CaseDocument doc1 = caseDocument("file1", "url1");

        ManageHearingDocumentsCollectionItem item1 = hearingDocumentItem(UUID.randomUUID(), doc1);

        ManageHearingsWrapper wrapper = ManageHearingsWrapper.builder()
                .workingHearingId(null)
                .hearingDocumentsCollection(List.of(item1))
                .build();

        List<CaseDocument> result = wrapper.getAssociatedWorkingHearingDocuments();

        assertNotNull(result);
        assertThat(result).isEmpty();
    }

    @Test
    void getAssociatedWorkingHearingDocuments_shouldExcludeCitizenFormCDocuments() {
        UUID hearingId = UUID.randomUUID();
        CaseDocument standardFormC = caseDocument("FormC.pdf");
        CaseDocument applicantCitizenFormC = caseDocument("FormCApplicantCitizen.pdf");
        CaseDocument respondentCitizenFormC = caseDocument("FormCRespondentCitizen.pdf");

        ManageHearingDocumentsCollectionItem standardItem = hearingDocumentItem(hearingId, CaseDocumentType.FORM_C, standardFormC);
        ManageHearingDocumentsCollectionItem applicantCitizenItem = hearingDocumentItem(
            hearingId,
            CaseDocumentType.FORM_C_FAST_TRACK_APPLICANT_CITIZEN,
            applicantCitizenFormC
        );
        ManageHearingDocumentsCollectionItem respondentCitizenItem = hearingDocumentItem(
            hearingId,
            CaseDocumentType.FORM_C_EXPRESS_RESPONDENT_CITIZEN,
            respondentCitizenFormC
        );

        ManageHearingsWrapper wrapper = ManageHearingsWrapper.builder()
            .workingHearingId(hearingId)
            .hearingDocumentsCollection(List.of(standardItem, applicantCitizenItem, respondentCitizenItem))
            .build();

        assertThat(wrapper.getAssociatedWorkingHearingDocuments()).containsExactly(standardFormC);
    }

    @Test
    void whenGetAssociatedHearingDocuments_givenNullHearingId_thenReturnsEmpty() {
        var wrapper = ManageHearingsWrapper.builder()
            .hearingDocumentsCollection(List.of())
            .build();

        assertThat(wrapper.getAssociatedHearingDocuments(null)).isEmpty();
    }

    @Test
    void whenGetAssociatedHearingDocuments_givenNullCollection_thenReturnsEmpty() {
        var wrapper = ManageHearingsWrapper.builder()
            .hearingDocumentsCollection(null)
            .build();

        assertThat(wrapper.getAssociatedHearingDocuments(UUID.randomUUID())).isEmpty();
    }

    @Test
    void whenGetAssociatedHearingDocuments_givenValidCollection_thenReturnsDocsInOrder() {
        UUID target = UUID.randomUUID();
        UUID other = UUID.randomUUID();

        CaseDocument docA = caseDocument("A");
        CaseDocument docB = caseDocument("B");

        var itemB = hearingDocumentItem(target, docB);
        var itemA = hearingDocumentItem(target, docA);
        var itemOther = hearingDocumentItem(other, docA);

        var wrapper = ManageHearingsWrapper.builder()
            .hearingDocumentsCollection(List.of(itemB, itemOther, itemA))
            .build();

        assertThat(wrapper.getAssociatedHearingDocuments(target)).containsExactly(docB, docA);
    }

    @Test
    void getAssociatedHearingDocuments_excludesCitizenFormCDocuments() {
        UUID target = UUID.randomUUID();

        CaseDocument standardFormC = caseDocument("FormC.pdf");
        CaseDocument applicantCitizenFormC = caseDocument("FormCApplicantCitizen.pdf");

        var standardItem = hearingDocumentItem(target, CaseDocumentType.FORM_C, standardFormC);
        var applicantCitizenItem = hearingDocumentItem(
            target,
            CaseDocumentType.FORM_C_FAST_TRACK_APPLICANT_CITIZEN,
            applicantCitizenFormC
        );

        var wrapper = ManageHearingsWrapper.builder()
            .hearingDocumentsCollection(List.of(standardItem, applicantCitizenItem))
            .build();

        assertThat(wrapper.getAssociatedHearingDocuments(target)).containsExactly(standardFormC);
    }

    @Test
    void givenNullHearings_when_hasNoHearings_shouldReturnTrue() {
        FinremCaseData caseData = FinremCaseData.builder().build();
        assertTrue(caseData.getManageHearingsWrapper().hasNoHearings());
    }

    @Test
    void givenEmptyHearings_when_hasNoHearings_shouldReturnTrue() {
        FinremCaseData caseData = FinremCaseData.builder()
            .manageHearingsWrapper(ManageHearingsWrapper.builder()
                .hearings(List.of()).build())
            .build();

        assertTrue(caseData.getManageHearingsWrapper().hasNoHearings());
    }

    @Test
    void givenHearings_when_hasNoHearings_shouldReturnFalse() {
        FinremCaseData caseData = FinremCaseData.builder()
            .manageHearingsWrapper(ManageHearingsWrapper.builder()
                .hearings(List.of(ManageHearingsCollectionItem.builder().build()))
                .build())
            .build();

        assertFalse(caseData.getManageHearingsWrapper().hasNoHearings());
    }

    private CaseDocument caseDocument(String filename) {
        return CaseDocument.builder().documentFilename(filename).build();
    }

    private CaseDocument caseDocument(String filename, String url) {
        return CaseDocument.builder().documentFilename(filename).documentUrl(url).build();
    }

    private ManageHearingDocumentsCollectionItem hearingDocumentItem(UUID hearingId, CaseDocument caseDocument) {
        return hearingDocumentItem(hearingId, null, caseDocument);
    }

    private ManageHearingDocumentsCollectionItem hearingDocumentItem(UUID hearingId,
                                                                      CaseDocumentType documentType,
                                                                      CaseDocument caseDocument) {
        return ManageHearingDocumentsCollectionItem.builder()
            .value(ManageHearingDocument.builder()
                .hearingId(hearingId)
                .hearingCaseDocumentType(documentType)
                .hearingDocument(caseDocument)
                .build())
            .build();
    }
}
