package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcategory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingBundleDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingBundleDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingUploadBundleCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingUploadBundleHolder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.document.DocumentCategory;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HearingBundleDocumentCategoriserTest {

    @InjectMocks
    private HearingBundleDocumentCategoriser hearingBundleDocumentCategoriser;

    @Mock
    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setUp() {
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
    }

    @Test
    void testCategoriseDocuments() {
        FinremCaseData finremCaseData = buildFinremCaseDataWithBundles();
        hearingBundleDocumentCategoriser.categorise(finremCaseData);

        // FDR bundle
        HearingUploadBundleHolder fdrBundle =
            finremCaseData.getFdrHearingBundleCollections().getFirst().getValue();

        assertThat(fdrBundle.getHearingBundleDocuments()).hasSize(3);
        fdrBundle.getHearingBundleDocuments().subList(0, 1).forEach(doc ->
            assertThat(doc.getValue().getBundleDocuments().getCategoryId())
                .isEqualTo(DocumentCategory.FDR_BUNDLE.getDocumentCategoryId())
        );
        // Check third document has null bundleDocuments
        assertThat(fdrBundle.getHearingBundleDocuments().get(2).getValue().getBundleDocuments())
            .isNull();

        // Hearing bundle
        HearingUploadBundleHolder hearingBundle =
            finremCaseData.getHearingUploadBundle().getFirst().getValue();

        assertThat(hearingBundle.getHearingBundleDocuments()).hasSize(3);
        hearingBundle.getHearingBundleDocuments().subList(0, 1).forEach(doc ->
            assertThat(doc.getValue().getBundleDocuments().getCategoryId())
                .isEqualTo(DocumentCategory.HEARING_BUNDLE.getDocumentCategoryId())
        );
        // Check third document has null bundleDocuments
        assertThat(hearingBundle.getHearingBundleDocuments().get(2).getValue().getBundleDocuments())
            .isNull();
    }

    private FinremCaseData buildFinremCaseDataWithBundles() {
        HearingUploadBundleCollection fdrHearingUploadBundleCollection =
            getHearingUploadBundleCollection();

        HearingUploadBundleCollection hearingUploadBundleCollection =
            getHearingUploadBundleCollection();

        return FinremCaseData.builder()
            .fdrHearingBundleCollections(List.of(fdrHearingUploadBundleCollection))
            .hearingUploadBundle(List.of(hearingUploadBundleCollection))
            .build();
    }

    private static HearingUploadBundleCollection getHearingUploadBundleCollection() {
        List<HearingBundleDocumentCollection> hearingBundleDocumentCollections =
            Arrays.asList(getHearingBundleDocumentCollection("file2.pdf"),
                getHearingBundleDocumentCollection("file4.pdf"),
                getHearingBundleDocumentCollectionWithDeletedBundleDocuments());

        return HearingUploadBundleCollection.builder()
            .value(HearingUploadBundleHolder.builder()
                .hearingBundleDate(LocalDate.of(2020, 1, 1))
                .hearingBundleDocuments(hearingBundleDocumentCollections)
                .hearingBundleFdr(YesOrNo.NO)
                .build())
            .build();
    }

    private static HearingBundleDocumentCollection getHearingBundleDocumentCollection(String filename) {
        return HearingBundleDocumentCollection.builder()
            .value(HearingBundleDocument.builder()
                .bundleDocuments(CaseDocument.builder().documentFilename(filename).build())
                .build())
            .build();
    }

    private static HearingBundleDocumentCollection getHearingBundleDocumentCollectionWithDeletedBundleDocuments() {
        return HearingBundleDocumentCollection.builder()
            .value(HearingBundleDocument.builder()
                .bundleDocuments(null)
                .build())
            .build();
    }
}
