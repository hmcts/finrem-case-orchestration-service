package uk.gov.hmcts.reform.finrem.caseorchestration.handler.uploadapprovedorder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.AdditionalHearingDirectionsCollection;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.Element;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ApprovedOrderNoticeOfHearingService;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CCDConfigConstant.HEARING_DIRECTION_DETAILS_COLLECTION;

@RunWith(MockitoJUnitRunner.class)
public class UploadApprovedOrderSubmittedHandlerTest extends UploadApprovedOrderBaseHandlerTest {

    @InjectMocks
    UploadApprovedOrderSubmittedHandler uploadApprovedOrderSubmittedHandler;

    @Mock
    ApprovedOrderNoticeOfHearingService approvedOrderNoticeOfHearingService;

    @Test
    public void givenContestedCase_whenAboutToSubmitUploadApprovedOrder_thenCanHandle() {
        assertThat(uploadApprovedOrderSubmittedHandler
                .canHandle(CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.UPLOAD_APPROVED_ORDER),
            is(true));
    }

    @Test
    public void givenContestedCase_whenSubmittedUploadApprovedOrder_thenCannotHandle() {
        assertThat(uploadApprovedOrderSubmittedHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.UPLOAD_APPROVED_ORDER),
            is(false));
    }

    @Test
    public void givenContestedCase_whenSubmittedUploadApprovedOrder_thenHandle() {
        setHearingDirectionDetailsCollection(YES_VALUE);
        uploadApprovedOrderSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(approvedOrderNoticeOfHearingService, times(1))
            .printHearingNoticePackAndSendToApplicantAndRespondent(callbackRequest.getCaseDetails(), AUTH_TOKEN);
    }

    @Test
    public void givenContestedCase_whenSubmittedUploadApprovedOrderAndNotFinalHearing_thenHandle() {
        setHearingDirectionDetailsCollection(NO_VALUE);
        uploadApprovedOrderSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(approvedOrderNoticeOfHearingService, never())
            .printHearingNoticePackAndSendToApplicantAndRespondent(callbackRequest.getCaseDetails(), AUTH_TOKEN);
    }

    private void setHearingDirectionDetailsCollection(String value) {
        callbackRequest.getCaseDetails().getData().put(HEARING_DIRECTION_DETAILS_COLLECTION,
            buildAdditionalHearingDetailsCollection(value));
    }

    private List<Element<AdditionalHearingDirectionsCollection>> buildAdditionalHearingDetailsCollection(String value) {
        return List.of(Element.element(UUID.randomUUID(), AdditionalHearingDirectionsCollection.builder()
            .isAnotherHearingYN(value).build()));
    }
}
