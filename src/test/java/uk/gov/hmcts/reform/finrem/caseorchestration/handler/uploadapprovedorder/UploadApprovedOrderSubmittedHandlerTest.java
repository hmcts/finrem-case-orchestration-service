package uk.gov.hmcts.reform.finrem.caseorchestration.handler.uploadapprovedorder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.ApprovedOrderNoticeOfHearingService;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.ccd.domain.EventType;
import uk.gov.hmcts.reform.finrem.ccd.domain.HearingDirectionDetail;
import uk.gov.hmcts.reform.finrem.ccd.domain.HearingDirectionDetailsCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.YesOrNo;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

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
        setHearingDirectionDetailsCollection(YesOrNo.YES);
        uploadApprovedOrderSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(approvedOrderNoticeOfHearingService, times(1))
            .printHearingNoticePackAndSendToApplicantAndRespondent(callbackRequest.getCaseDetails(), AUTH_TOKEN);
    }

    @Test
    public void givenContestedCase_whenSubmittedUploadApprovedOrderAndNotFinalHearing_thenHandle() {
        setHearingDirectionDetailsCollection(YesOrNo.NO);
        uploadApprovedOrderSubmittedHandler.handle(callbackRequest, AUTH_TOKEN);

        verify(approvedOrderNoticeOfHearingService, never())
            .printHearingNoticePackAndSendToApplicantAndRespondent(callbackRequest.getCaseDetails(), AUTH_TOKEN);
    }

    private void setHearingDirectionDetailsCollection(YesOrNo value) {
        callbackRequest.getCaseDetails().getCaseData()
            .setHearingDirectionDetailsCollection(buildAdditionalHearingDetailsCollection(value));
    }

    private List<HearingDirectionDetailsCollection> buildAdditionalHearingDetailsCollection(YesOrNo value) {
        return List.of(HearingDirectionDetailsCollection.builder()
                .value(HearingDirectionDetail.builder()
                    .isAnotherHearingYN(value)
                    .build())
            .build());
    }
}
