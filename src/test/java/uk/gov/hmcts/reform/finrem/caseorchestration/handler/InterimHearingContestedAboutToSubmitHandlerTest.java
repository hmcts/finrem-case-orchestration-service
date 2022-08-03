package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InterimHearingService;
import uk.gov.hmcts.reform.finrem.ccd.callback.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackRequest;
import uk.gov.hmcts.reform.finrem.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.ccd.domain.CaseType;
import uk.gov.hmcts.reform.finrem.ccd.domain.EventType;
import uk.gov.hmcts.reform.finrem.ccd.domain.FinremCaseData;
import uk.gov.hmcts.reform.finrem.ccd.domain.InterimHearingCollection;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.InterimRegionWrapper;
import uk.gov.hmcts.reform.finrem.ccd.domain.wrapper.InterimWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class InterimHearingContestedAboutToSubmitHandlerTest extends BaseHandlerTest {

    @InjectMocks
    private InterimHearingContestedAboutToSubmitHandler interimHearingContestedAboutToSubmitHandler;
    @Mock
    private InterimHearingService interimHearingService;

    private static final String AUTH_TOKEN = "tokien:)";
    private static final String TEST_JSON = "/fixtures/contested/interim-hearing-two-collection.json";

    @Before
    public void setup() {

    }

    @Test
    public void canHandle() {
        assertThat(interimHearingContestedAboutToSubmitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.INTERIM_HEARING),
            is(true));
    }

    @Test
    public void canNotHandleWrongCaseType() {
        assertThat(interimHearingContestedAboutToSubmitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.INTERIM_HEARING),
            is(false));
    }

    @Test
    public void canNotHandleWrongEvent() {
        assertThat(interimHearingContestedAboutToSubmitHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.CLOSE),
            is(false));
    }

    @Test
    public void canNotHandleWrongCallbackType() {
        assertThat(interimHearingContestedAboutToSubmitHandler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.INTERIM_HEARING),
            is(false));
    }

    @Test
    public void givenContestedCase_WhenMultipleInterimHearing_ThenHearingsShouldBePresentInChronologicalOrder() {
        CallbackRequest callbackRequest = getCallbackRequestFromResource(TEST_JSON);
        AboutToStartOrSubmitCallbackResponse handle =
            interimHearingContestedAboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        FinremCaseData caseData = handle.getData();
        List<InterimHearingCollection> interimHearingList = Optional.ofNullable(caseData.getInterimWrapper().getInterimHearings())
            .orElse(new ArrayList<>());

        assertEquals("2000-10-10", interimHearingList.get(0).getValue().getInterimHearingDate());
        assertEquals("2040-10-10", interimHearingList.get(1).getValue().getInterimHearingDate());

        verify(interimHearingService).submitInterimHearing(any(), any(), any());
        verifyNonCollectionData(caseData);
    }

    private void verifyNonCollectionData(FinremCaseData data) {
        InterimWrapper interimData = data.getInterimWrapper();
        assertNull(interimData.getInterimHearingType());
        assertNull(interimData.getInterimHearingDate());
        assertNull(interimData.getInterimHearingTime());
        assertNull(interimData.getInterimTimeEstimate());
        assertNull(interimData.getInterimAdditionalInformationAboutHearing());
        assertNull(interimData.getInterimPromptForAnyDocument());
        assertNull(interimData.getInterimUploadAdditionalDocument());
        assertEquals(data.getRegionWrapper().getInterimRegionWrapper(), new InterimRegionWrapper());
    }
}
