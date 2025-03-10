package uk.gov.hmcts.reform.finrem.caseorchestration.handler.solicitorcreatecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.mapper.FinremCaseDetailsMapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SelectedCourtService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class SolicitorCreateContestedMidHandlerTest {

    public static final String AUTH_TOKEN = "tokien:)";

    private SolicitorCreateContestedMidHandler handler;

    @Mock
    FinremCaseDetailsMapper finremCaseDetailsMapper;
    @Mock
    private InternationalPostalService postalService;

    @Mock
    private SelectedCourtService selectedCourtService;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private ExpressCaseService expressCaseService;


    @BeforeEach
    public void init() {
        handler = new SolicitorCreateContestedMidHandler(
                finremCaseDetailsMapper,
                postalService,
                selectedCourtService,
                expressCaseService,
                featureToggleService);
    }

    @Test
    void testHandlerCanHandle() {
        assertCanHandle(handler, CallbackType.MID_EVENT, CaseType.CONTESTED, EventType.SOLICITOR_CREATE);
    }

    @Test
    void testPostalServiceValidationCalled() {
        FinremCallbackRequest finremCallbackRequest = buildFinremCallbackRequest();
        handler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(postalService, times(1))
                .validate(finremCallbackRequest.getCaseDetails().getData());
    }

    @Test
    void testSetSelectedCourtDetailsIfPresentCalled() {
        FinremCallbackRequest finremCallbackRequest = buildFinremCallbackRequest();
        handler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(selectedCourtService, times(1))
                .setSelectedCourtDetailsIfPresent(finremCallbackRequest.getCaseDetails().getData());
    }

    @Test
    void testGivenExpressPilotEnabled_ThenExpressCaseServiceCalled() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest();
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
        when(featureToggleService.isExpressPilotEnabled()).thenReturn(true);

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(expressCaseService).setExpressCaseEnrollmentStatus(caseData);
    }

    @Test
    void testGivenExpressPilotDisabled_ThenExpressCaseServiceIsNotCalled() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest();
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();
        when(featureToggleService.isExpressPilotEnabled()).thenReturn(false);

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(expressCaseService, never()).setExpressCaseEnrollmentStatus(caseData);
    }

    private FinremCallbackRequest buildFinremCallbackRequest() {
        FinremCaseData caseData = FinremCaseData.builder().build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123L).data(caseData).build();
        return FinremCallbackRequest.builder().caseDetails(caseDetails).build();
    }

}
