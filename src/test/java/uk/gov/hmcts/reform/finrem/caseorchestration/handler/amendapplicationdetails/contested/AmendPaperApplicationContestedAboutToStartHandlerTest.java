package uk.gov.hmcts.reform.finrem.caseorchestration.handler.amendapplicationdetails.contested;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.OnStartDefaultValueService;
import uk.gov.hmcts.reform.finrem.caseorchestration.utils.refuge.RefugeWrapperUtils;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class AmendPaperApplicationContestedAboutToStartHandlerTest {

    @InjectMocks
    private AmendPaperApplicationContestedAboutToStartHandler handler;

    @Mock
    private OnStartDefaultValueService onStartDefaultValueService;

    @Test
    void testHandlerCanHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_START, CaseType.CONTESTED, EventType.AMEND_CONTESTED_PAPER_APP_DETAILS);
    }

    @Test
    void testHandle() {
        FinremCaseData finremCaseData = mock(FinremCaseData.class);
        FinremCaseDetails caseDetails = mock(FinremCaseDetails.class);
        FinremCallbackRequest callbackRequest = mock(FinremCallbackRequest.class);
        when(callbackRequest.getCaseDetails()).thenReturn(caseDetails);
        when(callbackRequest.getFinremCaseData()).thenReturn(finremCaseData);

        try (MockedStatic<RefugeWrapperUtils> mockedStatic = Mockito.mockStatic(RefugeWrapperUtils.class)) {
            var response = handler.handle(callbackRequest, AUTH_TOKEN);

            assertAll(
                () -> verify(onStartDefaultValueService).defaultCivilPartnershipField(callbackRequest),
                () -> verify(onStartDefaultValueService).defaultTypeOfApplication(callbackRequest),
                () -> verify(onStartDefaultValueService).defaultUrgencyQuestion(callbackRequest),
                () -> verifyNoMoreInteractions(onStartDefaultValueService),
                () -> assertEquals(finremCaseData, response.getData()),
                () -> mockedStatic.verify(() -> RefugeWrapperUtils.populateApplicantInRefugeQuestion(caseDetails)),
                () -> mockedStatic.verify(() -> RefugeWrapperUtils.populateRespondentInRefugeQuestion(caseDetails)),
                mockedStatic::verifyNoMoreInteractions
            );
        }
    }
}
