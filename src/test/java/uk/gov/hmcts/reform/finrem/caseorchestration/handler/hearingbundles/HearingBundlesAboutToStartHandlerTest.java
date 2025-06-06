package uk.gov.hmcts.reform.finrem.caseorchestration.handler.hearingbundles;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.HearingUploadBundleCollection;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(MockitoJUnitRunner.class)
public class HearingBundlesAboutToStartHandlerTest {

    protected static final String THIS_IS_AN_ERROR = "This is an error";
    @Mock
    FinremCaseDetailsMapper finremCaseDetailsMapper;

    @InjectMocks
    ManageHearingBundlesAboutToStartHandler manageHearingBundlesAboutToStartHandler;


    @Test
    public void givenHandlerCanHandleCallback_whenCanHandle_thenReturnTrue() {
        assertThat(manageHearingBundlesAboutToStartHandler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.MANAGE_HEARING_BUNDLES),
            is(true));
    }

    @Test
    public void givenInvalidCallbackType_whenCanHandle_thenReturnFalse() {
        assertThat(manageHearingBundlesAboutToStartHandler.canHandle(
                CallbackType.ABOUT_TO_START,
                CaseType.CONTESTED,
                EventType.MANAGE_BARRISTER),
            is(false));
    }


    @Test
    public void shouldCombineHearingBundleWithFdrHearingBundle() {

        HearingUploadBundleCollection hearingUploadBundleCollection = HearingUploadBundleCollection.builder().build();
        List<HearingUploadBundleCollection> hearingUploadBundleCollections = new ArrayList<>();
        hearingUploadBundleCollections.add(hearingUploadBundleCollection);
        HearingUploadBundleCollection fdrHearingUploadBundleCollection = HearingUploadBundleCollection.builder().build();
        List<HearingUploadBundleCollection> fdrHearingUploadBundleCollections = new ArrayList<>();
        fdrHearingUploadBundleCollections.add(fdrHearingUploadBundleCollection);

        FinremCaseData finremCaseData = FinremCaseData.builder().hearingUploadBundle(hearingUploadBundleCollections)
            .fdrHearingBundleCollections(fdrHearingUploadBundleCollections).build();

        FinremCaseDetails finremCaseDetails = FinremCaseDetails.builder().id(1L).data(finremCaseData).build();
        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder().caseDetails(finremCaseDetails).build();


        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =
            manageHearingBundlesAboutToStartHandler.handle(callbackRequest, "auth");
        assertThat(response.getData().getHearingUploadBundle().contains(hearingUploadBundleCollection), is(true));
        assertThat(response.getData().getHearingUploadBundle().contains(fdrHearingUploadBundleCollection), is(true));
    }

}
