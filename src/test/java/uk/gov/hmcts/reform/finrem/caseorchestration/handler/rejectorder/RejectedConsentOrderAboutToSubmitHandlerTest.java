package uk.gov.hmcts.reform.finrem.caseorchestration.handler.rejectorder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.handler.FinremCallbackRequest;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderRefusalHolder;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.OrderRefusalOption;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.RefusalOrderDocumentService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.documentcatergory.RefusedConsentOrderDocumentCategoriser;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class RejectedConsentOrderAboutToSubmitHandlerTest {

    @InjectMocks
    private RejectedConsentOrderAboutToSubmitHandler handler;
    @Mock
    private RefusalOrderDocumentService refusalOrderDocumentService;
    @Mock
    private RefusedConsentOrderDocumentCategoriser categoriser;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.ABOUT_TO_SUBMIT, CaseType.CONSENTED, EventType.REJECT_ORDER);
    }

    @Test
    void givenCase_whenHandled_thenRefusalOrderProcessedAndCategorised() {
        FinremCaseData finremCaseData = FinremCaseData.builder()
            .orderRefusalOnScreen(populatedOrderRefusalOrderHolder())
            .build();

        FinremCallbackRequest callbackRequest = FinremCallbackRequestFactory.from(finremCaseData);
        when(refusalOrderDocumentService.processConsentOrderNotApproved(callbackRequest.getCaseDetails(), AUTH_TOKEN))
            .thenReturn(finremCaseData);

        var response = handler.handle(callbackRequest, AUTH_TOKEN);

        assertAll(
            () -> verify(refusalOrderDocumentService).processConsentOrderNotApproved(callbackRequest.getCaseDetails(), AUTH_TOKEN),
            () -> verify(categoriser).categorise(finremCaseData),
            () -> assertThat(response.getData()).isEqualTo(finremCaseData),
            () -> verifyNoMoreInteractions(refusalOrderDocumentService, categoriser)
        );
    }

    private OrderRefusalHolder populatedOrderRefusalOrderHolder() {
        List<OrderRefusalOption> refusalOptionList = new ArrayList<>();
        refusalOptionList.add(OrderRefusalOption.TRANSFERRED_TO_APPLICANTS_HOME_COURT_OLD);
        return OrderRefusalHolder.builder()
            .orderRefusal(refusalOptionList)
            .orderRefusalOther("Some other reason")
            .build();
    }

    // pt todo - identify and complete missing tests.  Ideally test refusedAsHearingNeeded and refusedForOtherReasons directly.  Confirm that the desired responses are given for the appropriate refusal choices.

}
