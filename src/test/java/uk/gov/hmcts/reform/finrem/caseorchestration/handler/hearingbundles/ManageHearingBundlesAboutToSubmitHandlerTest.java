package uk.gov.hmcts.reform.finrem.caseorchestration.handler.hearingbundles;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingBundleDocument;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingBundleDocumentCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingUploadBundleCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingUploadBundleHolder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ManageHearingBundlesAboutToSubmitHandlerTest {


    ManageHearingBundlesAboutToSubmitHandler manageHearingBundlesAboutToSubmitHandler =
        new ManageHearingBundlesAboutToSubmitHandler(new FinremCaseDetailsMapper(new ObjectMapper()));

    @Test
    public void givenHandlerCanHandleCallback_whenCanHandle_thenReturnTrue() {
        assertThat(manageHearingBundlesAboutToSubmitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.MANAGE_HEARING_BUNDLES),
            is(true));
    }


    @Test
    public void givenInvalidCallbackType_whenCanHandle_thenReturnFalse() {
        assertThat(manageHearingBundlesAboutToSubmitHandler.canHandle(
                CallbackType.ABOUT_TO_SUBMIT,
                CaseType.CONTESTED,
                EventType.MANAGE_BARRISTER),
            is(false));
    }

    @Test
    public void givenHearingBundlesShouldSortByBundleDateAndUploadedDate() {

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

        manageHearingBundlesAboutToSubmitHandler.sortHearingBundlesAndValidateForErrors(new ArrayList<>(), hearingUploadBundleCollections);

        assertThat(hearingUploadBundleCollections.get(0).getValue().getHearingBundleDocuments().get(0).getValue().getBundleUploadDate(),
            is(LocalDateTime.of(2021, 1, 1, 1, 1)));
        assertThat(hearingUploadBundleCollections.get(0).getValue().getHearingBundleDocuments().get(1).getValue().getBundleUploadDate(),
            is(LocalDateTime.of(2020, 1, 1, 1, 1)));

        assertThat(hearingUploadBundleCollections.get(1).getValue().getHearingBundleDocuments().get(0).getValue().getBundleUploadDate(),
            is(LocalDateTime.of(2023, 1, 1, 1, 1)));
        assertThat(hearingUploadBundleCollections.get(1).getValue().getHearingBundleDocuments().get(1).getValue().getBundleUploadDate(),
            is(LocalDateTime.of(2019, 1, 1, 1, 1)));


    }


    @Test
    public void shouldMoveHearingBundleToFdrCollections() {
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
            manageHearingBundlesAboutToSubmitHandler.handle(FinremCallbackRequest.builder()
                .caseDetails(FinremCaseDetails.builder()
                    .data(FinremCaseData.builder()
                        .hearingUploadBundle(hearingUploadBundleCollections)
                        .build())
                    .build())
                .build(), "auth");

        assertThat(response.getData().getHearingUploadBundle().size(), is(1));
        assertThat(response.getData().getHearingUploadBundle().get(0).getValue().getHearingBundleFdr(), is(YesOrNo.NO));
        assertThat(response.getData().getFdrHearingBundleCollections().size(), is(1));
        assertThat(response.getData().getFdrHearingBundleCollections().get(0).getValue().getHearingBundleFdr(), is(YesOrNo.YES));

    }

    private static HearingBundleDocumentCollection getHearingBundleDocumentCollection(int year, String filename) {
        HearingBundleDocumentCollection hearingBundleDocumentCollection1 = HearingBundleDocumentCollection.builder()
            .value(HearingBundleDocument.builder()
                .bundleDocuments(CaseDocument.builder().documentFilename(filename).build())
                .bundleUploadDate(LocalDateTime.of(year, 1, 1, 1, 1))
                .build())
            .build();
        return hearingBundleDocumentCollection1;
    }

}
