package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignPartiesAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.issueapplication.IssueApplicationContestedEmailCorresponder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class IssueApplicationContestedSubmittedHandlerTest {

    @InjectMocks
    private IssueApplicationContestedSubmittedHandler handler;
    @Mock
    private IssueApplicationContestedEmailCorresponder corresponder;
    @Mock
    private AssignPartiesAccessService assignPartiesAccessService;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, CallbackType.SUBMITTED, CaseType.CONTESTED, EventType.ISSUE_APPLICATION);
    }

    @Test
    void givenCase_whenHandled_thenShouldSendCorrespondence() {
        FinremCallbackRequest request = FinremCallbackRequestFactory.from();

        handler.handle(request, AUTH_TOKEN);

        verify(corresponder).sendCorrespondence(request.getCaseDetails());
    }

    @Test
    void givenRespondentNotRepresented_whenHandled_thenShouldNotGrantRespondentSolicitor() {
        FinremCallbackRequest request = FinremCallbackRequestFactory.from();

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(request, AUTH_TOKEN);

        assertThat(response)
            .extracting(
                GenericAboutToStartOrSubmitCallbackResponse::getConfirmationBody,
                GenericAboutToStartOrSubmitCallbackResponse::getConfirmationHeader)
            .containsOnlyNulls();

        verify(assignPartiesAccessService, never()).grantRespondentSolicitor(request.getCaseDetails().getData());
    }

    @Test
    void givenRespondentRepresented_whenHandled_thenGrantRespondentSolicitor() {
        ContactDetailsWrapper contactDetailsWrapper = mock(ContactDetailsWrapper.class);
        when(contactDetailsWrapper.getContestedRespondentRepresented()).thenReturn(YesOrNo.YES);

        FinremCaseData mockedCaseData = mock(FinremCaseData.class);
        when(mockedCaseData.getContactDetailsWrapper()).thenReturn(contactDetailsWrapper);
        when(mockedCaseData.getRespondentSolicitorEmail()).thenReturn(TEST_SOLICITOR_EMAIL);

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(mockedCaseData);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(request, AUTH_TOKEN);

        assertThat(response)
            .extracting(
                GenericAboutToStartOrSubmitCallbackResponse::getConfirmationBody,
                GenericAboutToStartOrSubmitCallbackResponse::getConfirmationHeader)
            .containsOnlyNulls();

        verify(assignPartiesAccessService).grantRespondentSolicitor(request.getCaseDetails().getData());
        verifyNoMoreInteractions(assignPartiesAccessService);
    }

}
