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
import uk.gov.hmcts.reform.finrem.caseorchestration.service.express.ExpressCaseService;


import java.util.Arrays;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
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
    private ExpressCaseService expressCaseService;


    @BeforeEach
    public void init() {
        handler = new SolicitorCreateContestedMidHandler(
                finremCaseDetailsMapper,
                postalService,
                selectedCourtService,
                expressCaseService);
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
    void testExpressCaseServiceCalled() {
        FinremCallbackRequest callbackRequest = buildFinremCallbackRequest();
        FinremCaseData caseData = callbackRequest.getCaseDetails().getData();

        handler.handle(callbackRequest, AUTH_TOKEN);

        verify(expressCaseService).setExpressCaseEnrollmentStatus(caseData);
    }

    @Test
    void testRoyalCourtOrHighCourtChosenCalled() {
        FinremCallbackRequest finremCallbackRequest = buildFinremCallbackRequest();
        handler.handle(finremCallbackRequest, AUTH_TOKEN);
        verify(selectedCourtService, times(1))
                .royalCourtOrHighCourtChosen(finremCallbackRequest.getCaseDetails().getData());
    }

    /*
     * handler can add errors that stem from either postalService.validate or
     * selectedCourtService.royalCourtOrHighCourtChosen.
     * This checks that any errors from each service are present in the callback response.
     */
    @Test
    void testThatErrorsReturned() {
        FinremCallbackRequest finremCallbackRequest = buildFinremCallbackRequest();
        when(selectedCourtService.royalCourtOrHighCourtChosen(any())).thenReturn(true);
        when(postalService.validate((FinremCaseData) any())).thenReturn(Arrays.asList("post err 1", "post err 2"));
        assertThat(handler.handle(finremCallbackRequest, AUTH_TOKEN).getErrors()).contains(
                "You cannot select High Court or Royal Court of Justice. Please select another court.",
                "post err 1",
                "post err 2"
        );
    }

    private FinremCallbackRequest buildFinremCallbackRequest() {
        FinremCaseData caseData = FinremCaseData.builder().build();
        FinremCaseDetails caseDetails = FinremCaseDetails.builder().id(123L).data(caseData).build();
        return FinremCallbackRequest.builder().caseDetails(caseDetails).build();
    }

}
