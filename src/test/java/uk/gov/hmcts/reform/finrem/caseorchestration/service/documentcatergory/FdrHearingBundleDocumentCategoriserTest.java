package uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FdrHearingBundleDocumentCategoriserTest {

    private FdrHearingBundleDocumentCategoriser fdrHearingBundleDocumentCategoriser;

    @Mock
    FeatureToggleService featureToggleService;

    @Before
    public void setUp() {
        fdrHearingBundleDocumentCategoriser = new FdrHearingBundleDocumentCategoriser(featureToggleService);
        when(featureToggleService.isCaseFileViewEnabled()).thenReturn(true);
    }

    @Test
    public void testCategorizeDocuments() {
        FinremCaseData finremCaseData = buildFinremCaseData();
        fdrHearingBundleDocumentCategoriser.categorise(finremCaseData);
        HearingUploadBundleHolder hearingUploadBundleHolder = finremCaseData.getFdrHearingBundleCollections().get(0).getValue();
        assertThat(hearingUploadBundleHolder.getHearingBundleDocuments().get(0).getValue()
            .getBundleDocuments().getCategoryId(), is(
            DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE.getDocumentCategoryId()
        ));
        assertThat(hearingUploadBundleHolder.getHearingBundleDocuments().get(1).getValue()
            .getBundleDocuments().getCategoryId(), is(
            DocumentCategory.FDR_DOCUMENTS_AND_FDR_BUNDLE.getDocumentCategoryId()
        ));
    }

    private FinremCaseData buildFinremCaseData() {

        List<HearingBundleDocumentCollection> hearingBundleDocumentCollections =
            Arrays.asList(getHearingBundleDocumentCollection("file2.pdf"), getHearingBundleDocumentCollection("file4.pdf"));

        HearingUploadBundleCollection hearingUploadBundleCollection = HearingUploadBundleCollection.builder()
            .value(HearingUploadBundleHolder.builder()
                .hearingBundleDate(LocalDate.of(2020, 1, 1))
                .hearingBundleDocuments(hearingBundleDocumentCollections)
                .hearingBundleFdr(YesOrNo.NO)
                .build())
            .build();

        return FinremCaseData.builder().fdrHearingBundleCollections(List.of(hearingUploadBundleCollection)).build();
    }


    private static HearingBundleDocumentCollection getHearingBundleDocumentCollection(String filename) {
        HearingBundleDocumentCollection hearingBundleDocumentCollection1 = HearingBundleDocumentCollection.builder()
            .value(HearingBundleDocument.builder()
                .bundleDocuments(CaseDocument.builder().documentFilename(filename).build())
                .build())
            .build();
        return hearingBundleDocumentCollection1;
    }
}
