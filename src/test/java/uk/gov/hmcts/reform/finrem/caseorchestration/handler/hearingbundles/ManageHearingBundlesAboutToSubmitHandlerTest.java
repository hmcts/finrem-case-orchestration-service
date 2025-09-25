package uk.gov.hmcts.reform.finrem.caseorchestration.handler.hearingbundles;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingBundleDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingBundleDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingUploadBundleCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingUploadBundleHolder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.HearingBundleDocumentCategoriser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class ManageHearingBundlesAboutToSubmitHandlerTest {

    @Mock
    private HearingBundleDocumentCategoriser hearingBundleDocumentCategoriser;

    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;

    @InjectMocks
    private ManageHearingBundlesAboutToSubmitHandler handler;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.MANAGE_HEARING_BUNDLES);
    }

    @Test
    void givenHearingBundlesShouldSortByBundleDateAndUploadedDate() {
        List<HearingBundleDocumentCollection> hearingBundleDocumentCollections1 =
            Arrays.asList(getHearingBundleDocumentCollection(2019, "file3.pdf"), getHearingBundleDocumentCollection(2023, "file4.pdf"));
        HearingUploadBundleCollection hearingUploadBundleCollectionEarliest = HearingUploadBundleCollection.builder()
            .value(HearingUploadBundleHolder.builder()
                .hearingBundleDate(LocalDate.of(2019, 1, 1))
                .hearingBundleDocuments(hearingBundleDocumentCollections1)
                .build())
            .build();

        List<HearingBundleDocumentCollection> hearingBundleDocumentCollections2 =
            Arrays.asList(getHearingBundleDocumentCollection(2021, "file5.pdf"), getHearingBundleDocumentCollection(2020, "file6.pdf"));
        HearingUploadBundleCollection hearingUploadBundleCollectionLatest = HearingUploadBundleCollection.builder()
            .value(HearingUploadBundleHolder.builder()
                .hearingBundleDate(LocalDate.of(2022, 1, 1))
                .hearingBundleDocuments(hearingBundleDocumentCollections2)
                .build())
            .build();

        List<HearingUploadBundleCollection> hearingUploadBundleCollections =
            Arrays.asList(hearingUploadBundleCollectionEarliest, hearingUploadBundleCollectionLatest);

        handler.sortHearingBundlesAndValidateForErrors(new ArrayList<>(), hearingUploadBundleCollections);

        assertThat(hearingUploadBundleCollections.getFirst().getValue().getHearingBundleDocuments().getFirst().getValue().getBundleUploadDate())
            .isEqualTo(LocalDateTime.of(2021, 1, 1, 1, 1));
        assertThat(hearingUploadBundleCollections.getFirst().getValue().getHearingBundleDocuments().get(1).getValue().getBundleUploadDate())
            .isEqualTo(LocalDateTime.of(2020, 1, 1, 1, 1));

        assertThat(hearingUploadBundleCollections.get(1).getValue().getHearingBundleDocuments().getFirst().getValue().getBundleUploadDate())
            .isEqualTo(LocalDateTime.of(2023, 1, 1, 1, 1));
        assertThat(hearingUploadBundleCollections.get(1).getValue().getHearingBundleDocuments().get(1).getValue().getBundleUploadDate())
            .isEqualTo(LocalDateTime.of(2019, 1, 1, 1, 1));
    }

    @Test
    void shouldMoveHearingBundleToFdrCollections() {
        List<HearingBundleDocumentCollection> hearingBundleDocumentCollections1 =
            Arrays.asList(getHearingBundleDocumentCollection(2019, "file1.pdf"), getHearingBundleDocumentCollection(2023, "file4.pdf"));

        HearingUploadBundleCollection hearingUploadBundleCollection = HearingUploadBundleCollection.builder()
            .value(HearingUploadBundleHolder.builder()
                .hearingBundleDate(LocalDate.of(2019, 1, 1))
                .hearingBundleDocuments(hearingBundleDocumentCollections1)
                .hearingBundleFdr(YesOrNo.YES)
                .build())
            .build();

        List<HearingBundleDocumentCollection> hearingBundleDocumentCollections2 =
            Arrays.asList(getHearingBundleDocumentCollection(2020, "file2.pdf"), getHearingBundleDocumentCollection(2022, "file4.pdf"));
        HearingUploadBundleCollection hearingUploadBundleCollection2 = HearingUploadBundleCollection.builder()
            .value(HearingUploadBundleHolder.builder()
                .hearingBundleDate(LocalDate.of(2020, 1, 1))
                .hearingBundleDocuments(hearingBundleDocumentCollections2)
                .hearingBundleFdr(YesOrNo.NO)
                .build())
            .build();

        List<HearingUploadBundleCollection> hearingUploadBundleCollections = new ArrayList<>();
        hearingUploadBundleCollections.add(hearingUploadBundleCollection);
        hearingUploadBundleCollections.add(hearingUploadBundleCollection2);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(FinremCallbackRequestFactory.from(
                FinremCaseData.builder()
                    .hearingUploadBundle(hearingUploadBundleCollections)
                    .build()), AUTH_TOKEN);

        assertThat(response.getData().getHearingUploadBundle()).hasSize(1);
        assertThat(response.getData().getHearingUploadBundle().getFirst().getValue().getHearingBundleFdr()).isEqualTo(YesOrNo.NO);
        assertThat(response.getData().getFdrHearingBundleCollections()).hasSize(1);
        assertThat(response.getData().getFdrHearingBundleCollections().getFirst().getValue().getHearingBundleFdr()).isEqualTo(YesOrNo.YES);
    }

    @Test
    void givenNonPdfFileUploaded_whenHandle_thenReturnErrors() {
        List<HearingBundleDocumentCollection> hearingBundleDocumentCollections1 =
            Arrays.asList(getHearingBundleDocumentCollection(2019, "file1.doc"), getHearingBundleDocumentCollectionWitDeletedBundleDocuments(2023));

        HearingUploadBundleCollection hearingUploadBundleCollection = HearingUploadBundleCollection.builder()
            .value(HearingUploadBundleHolder.builder()
                .hearingBundleDate(LocalDate.of(2019, 1, 1))
                .hearingBundleDocuments(hearingBundleDocumentCollections1)
                .hearingBundleFdr(YesOrNo.YES)
                .build())
            .build();

        List<HearingBundleDocumentCollection> hearingBundleDocumentCollections2 =
            Arrays.asList(getHearingBundleDocumentCollection(2020, "file2.pdf"), getHearingBundleDocumentCollection(2022, "file4.pdf"));
        HearingUploadBundleCollection hearingUploadBundleCollection2 = HearingUploadBundleCollection.builder()
            .value(HearingUploadBundleHolder.builder()
                .hearingBundleDate(LocalDate.of(2020, 1, 1))
                .hearingBundleDocuments(hearingBundleDocumentCollections2)
                .hearingBundleFdr(YesOrNo.NO)
                .build())
            .build();

        List<HearingUploadBundleCollection> hearingUploadBundleCollections = new ArrayList<>();
        hearingUploadBundleCollections.add(hearingUploadBundleCollection);
        hearingUploadBundleCollections.add(hearingUploadBundleCollection2);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            handler.handle(FinremCallbackRequestFactory.from(
                FinremCaseData.builder()
                    .hearingUploadBundle(hearingUploadBundleCollections)
                    .build()), AUTH_TOKEN);

        assertThat(response.getErrors()).containsExactly(
            "Uploaded bundle file1.doc is not in expected format. Please upload bundle in pdf format."
        );
    }

    private static HearingBundleDocumentCollection getHearingBundleDocumentCollection(int year, String filename) {
        return HearingBundleDocumentCollection.builder()
            .value(HearingBundleDocument.builder()
                .bundleDocuments(CaseDocument.builder().documentFilename(filename).build())
                .bundleUploadDate(LocalDateTime.of(year, 1, 1, 1, 1))
                .build())
            .build();
    }

    private static HearingBundleDocumentCollection getHearingBundleDocumentCollectionWitDeletedBundleDocuments(int year) {
        return HearingBundleDocumentCollection.builder()
            .value(HearingBundleDocument.builder()
                .bundleUploadDate(LocalDateTime.of(year, 1, 1, 1, 1))
                .build())
            .build();
    }
}
