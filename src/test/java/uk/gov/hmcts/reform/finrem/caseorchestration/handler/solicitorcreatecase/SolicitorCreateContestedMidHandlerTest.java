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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.InternationalPostalService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.SelectedCourtService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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

    @BeforeEach
    public void init() {
        handler = new SolicitorCreateContestedMidHandler(
                finremCaseDetailsMapper,
                postalService,
                selectedCourtService);
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

    private FinremCallbackRequest buildFinremCallbackRequest() {
        FinremCaseData caseData = FinremCaseData.builder().build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123L).data(caseData).build();
        return FinremCallbackRequest.builder().caseDetails(caseDetails).build();
    }

}
