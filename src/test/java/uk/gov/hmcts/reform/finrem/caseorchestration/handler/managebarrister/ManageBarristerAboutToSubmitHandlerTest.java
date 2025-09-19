package uk.gov.hmcts.reform.finrem.caseorchestration.handler.managebarrister;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.BarristerChange;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.BarristerChangeCaseAccessUpdater;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.barristers.ManageBarristerService;
import uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class ManageBarristerAboutToSubmitHandlerTest {

    @InjectMocks
    private ManageBarristerAboutToSubmitHandler manageBarristerAboutToSubmitHandler;
    @Mock
    private ManageBarristerService manageBarristerService;
    @Mock
    private BarristerChangeCaseAccessUpdater barristerChangeCaseAccessUpdater;

    @Test
    void testCanHandle() {
        Assertions.assertCanHandle(manageBarristerAboutToSubmitHandler, CallbackType.ABOUT_TO_SUBMIT,
            CaseType.CONTESTED, EventType.MANAGE_BARRISTER);
    }

    @Test
    void testHandle() {
        FinremCallbackRequest callbackRequest = FinremCallbackRequest.builder()
            .caseDetails(FinremCaseDetails.builder()
                .data(FinremCaseData.builder().build())
                .build())
            .caseDetailsBefore(FinremCaseDetails.builder()
                .data(FinremCaseData.builder().build())
                .build())
            .build();

        BarristerChange barristerChange = BarristerChange.builder().build();
        when(manageBarristerService.getBarristerChange(callbackRequest.getCaseDetails(),
            callbackRequest.getCaseDetailsBefore().getData(), AUTH_TOKEN))
            .thenReturn(barristerChange);

        var response = manageBarristerAboutToSubmitHandler.handle(callbackRequest, AUTH_TOKEN);

        assertThat(response.getData()).isNotNull();
        assertThat(response.getErrors()).isEmpty();
        assertThat(response.getWarnings()).isEmpty();
        verify(barristerChangeCaseAccessUpdater).update(callbackRequest.getCaseDetails(), AUTH_TOKEN, barristerChange);
    }
}
