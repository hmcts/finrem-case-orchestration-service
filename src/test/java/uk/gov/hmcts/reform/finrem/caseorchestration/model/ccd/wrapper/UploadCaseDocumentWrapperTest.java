package uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.UploadCaseDocumentCollection;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class UploadCaseDocumentWrapperTest {

    @Test
    void givenAllCollectionsNull_whenGetAllManageableCollections_thenReturnEmptyList() {
        UploadCaseDocumentWrapper wrapper = UploadCaseDocumentWrapper.builder().build();

        List<UploadCaseDocumentCollection> result = wrapper.getAllManageableCollections();

        assertThat(result).isEmpty();
    }

    @Test
    void givenSingleNonNullCollection_whenGetAllManageableCollections_thenReturnFlattenedList() {
        UploadCaseDocumentCollection doc1 = new UploadCaseDocumentCollection();
        UploadCaseDocumentCollection doc2 = new UploadCaseDocumentCollection();

        UploadCaseDocumentWrapper wrapper = UploadCaseDocumentWrapper.builder()
            .uploadCaseDocument(List.of(doc1, doc2))
            .build();

        List<UploadCaseDocumentCollection> result = wrapper.getAllManageableCollections();

        assertThat(result)
            .hasSize(2)
            .containsExactly(doc1, doc2);
    }

    // Fields that SHOULD be included
    private static final List<String> INCLUDED_FIELDS = List.of(
        "uploadCaseDocument", "fdrCaseDocumentCollection", "appCorrespondenceCollection",
        "appHearingBundlesCollection", "appFormEExhibitsCollection", "appChronologiesCollection",
        "appQaCollection", "appStatementsExhibitsCollection", "appCaseSummariesCollection",
        "appFormsHCollection", "appExpertEvidenceCollection", "appOtherCollection",
        "respHearingBundlesCollection", "respFormEExhibitsCollection", "respChronologiesCollection",
        "respQaCollection", "respStatementsExhibitsCollection", "respCaseSummariesCollection",
        "respFormsHCollection", "respExpertEvidenceCollection", "respCorrespondenceDocsColl",
        "respOtherCollection", "intv1Summaries", "intv1Chronologies", "intv1CorrespDocs",
        "intv1ExpertEvidence", "intv1FormEsExhibits", "intv1FormHs", "intv1HearingBundles",
        "intv1Other", "intv1Qa", "intv1StmtsExhibits", "intv2Summaries", "intv2Chronologies",
        "intv2CorrespDocs", "intv2ExpertEvidence", "intv2FormEsExhibits", "intv2FormHs",
        "intv2HearingBundles", "intv2Other", "intv2Qa", "intv2StmtsExhibits", "intv3Summaries",
        "intv3Chronologies", "intv3CorrespDocs", "intv3ExpertEvidence", "intv3FormEsExhibits",
        "intv3FormHs", "intv3HearingBundles", "intv3Other", "intv3Qa", "intv3StmtsExhibits",
        "intv4Summaries", "intv4Chronologies", "intv4CorrespDocs", "intv4ExpertEvidence",
        "intv4FormEsExhibits", "intv4FormHs", "intv4HearingBundles", "intv4Other", "intv4Qa",
        "intv4StmtsExhibits", "intv1FdrCaseDocuments", "intv2FdrCaseDocuments",
        "intv3FdrCaseDocuments", "intv4FdrCaseDocuments", "confidentialDocumentCollection"
    );

    @Test
    void getAllManageableCollections_shouldNotReturnUnlistedFields() throws Exception {
        // Add a new "unmanaged" collection field for testing
        UploadCaseDocumentWrapper wrapper = UploadCaseDocumentWrapper.builder().build();

        // Collect all List fields that are NOT in INCLUDED_FIELDS
        List<Field> otherListFields = Stream.of(UploadCaseDocumentWrapper.class.getDeclaredFields())
            .filter(f -> List.class.isAssignableFrom(f.getType()))
            .filter(f -> !INCLUDED_FIELDS.contains(f.getName()))
            .toList();

        // If there are no other fields, the test passes trivially
        if (otherListFields.isEmpty()) {
            return;
        }

        // Populate each "other" field with a document
        UploadCaseDocumentCollection doc = new UploadCaseDocumentCollection();
        for (Field f : otherListFields) {
            f.setAccessible(true);
            f.set(wrapper, List.of(doc));
        }

        // Call getAllManageableCollections
        List<UploadCaseDocumentCollection> result = wrapper.getAllManageableCollections();

        // None of the "other" fields should appear
        assertThat(result)
            .isNotNull()
            .isEmpty();
    }

    @ParameterizedTest
    @MethodSource("allIncludedFields")
    void givenEachIncludedField_whenGetAllManageableCollections_thenReturned(String fieldName) throws Exception {
        UploadCaseDocumentCollection doc = new UploadCaseDocumentCollection();

        UploadCaseDocumentWrapper wrapper = UploadCaseDocumentWrapper.builder().build();

        // Populate the field with a document
        Field field = UploadCaseDocumentWrapper.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(wrapper, List.of(doc));

        // Call the method
        List<UploadCaseDocumentCollection> result = wrapper.getAllManageableCollections();

        // Assert the field's value is included
        assertThat(result).containsExactly(doc);
    }

    static Stream<String> allIncludedFields() {
        return INCLUDED_FIELDS.stream();
    }
}
