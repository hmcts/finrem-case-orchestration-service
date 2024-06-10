package uk.gov.hmcts.reform.finrem.caseorchestration.handler;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimHearingItem;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.InterimTypeOfHearing;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InterimHearingService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.PartyService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InterimHearingContestedAboutToStartHandlerTest extends BaseHandlerTestSetup {

    private static final String AUTH_TOKEN = "tokien:)";
    private static final String CONTESTED_INTERIM_HEARING_JSON = "/fixtures/contested/interim-hearing.json";

    @Mock
    private PartyService partyService;
    @Mock
    private InterimHearingService interimHearingService;
    @Mock
    private FinremCaseDetailsMapper finremCaseDetailsMapper;
    @InjectMocks
    private InterimHearingContestedAboutToStartHandler interimHearingContestedAboutToStartHandler;


    @Test
    public void canHandle() {
        assertThat(interimHearingContestedAboutToStartHandler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.INTERIM_HEARING),
            is(true));
    }

    @Test
    public void canNotHandle() {
        assertThat(interimHearingContestedAboutToStartHandler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONSENTED, EventType.INTERIM_HEARING),
            is(false));
    }

    @Test
    public void canNotHandleWrongEventType() {
        assertThat(interimHearingContestedAboutToStartHandler
                .canHandle(CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.CLOSE),
            is(false));
    }

    @Test
    public void canNotHandleWrongCallbackType() {
        assertThat(interimHearingContestedAboutToStartHandler
                .canHandle(CallbackType.ABOUT_TO_SUBMIT, CaseType.CONTESTED, EventType.INTERIM_HEARING),
            is(false));
    }

    @Test
    public void givenCaseWithLegacyInterimHearing_WhenAboutToStart_ThenMigrateToInterimHearingCollection() {
        FinremCallbackRequest callbackRequest =
            buildFinremCallbackRequest(CONTESTED_INTERIM_HEARING_JSON);
        when(interimHearingService.getLegacyInterimHearingAsInterimHearingCollection(any())).thenCallRealMethod();

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData>
            handle = interimHearingContestedAboutToStartHandler.handle(callbackRequest, AUTH_TOKEN);
        FinremCaseData caseData = handle.getData();

        InterimHearingItem migratedItem = caseData.getInterimWrapper().getInterimHearings().get(0).getValue();
        assertThat(migratedItem.getInterimHearingType(),
            is(InterimTypeOfHearing.MPS));
        assertThat(migratedItem.getInterimHearingTime(),
            is("12:00"));
        assertThat(migratedItem.getInterimRegionList().getValue(),
            is("southwest"));
    }
}
