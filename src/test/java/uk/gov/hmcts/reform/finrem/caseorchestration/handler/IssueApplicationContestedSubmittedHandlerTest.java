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
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignPartiesAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UserNotFoundInOrganisationApiException;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.issueapplication.IssueApplicationContestedEmailCorresponder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
    void givenRespondentNotRepresented_whenHandled_thenShouldNotGrantRespondentSolicitor()
        throws UserNotFoundInOrganisationApiException {
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
    void givenRespondentRepresented_whenHandled_thenGrantRespondentSolicitor()
        throws UserNotFoundInOrganisationApiException {
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

    @Test
    void givenSendCorrespondenceFailure_whenHandled_thenRetriesThreeTimesAndShowsError()
        throws UserNotFoundInOrganisationApiException {
        ContactDetailsWrapper contactDetailsWrapper = mock(ContactDetailsWrapper.class);
        when(contactDetailsWrapper.getContestedRespondentRepresented()).thenReturn(YesOrNo.YES);

        FinremCaseData mockedCaseData = mock(FinremCaseData.class);
        when(mockedCaseData.getContactDetailsWrapper()).thenReturn(contactDetailsWrapper);
        when(mockedCaseData.getRespondentSolicitorEmail()).thenReturn(TEST_SOLICITOR_EMAIL);

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(mockedCaseData);

        // always fail
        doThrow(new RuntimeException("boom"))
            .when(corresponder)
            .sendCorrespondence(any(FinremCaseDetails.class));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =  handler.handle(request, AUTH_TOKEN);

        assertThat(response)
            .extracting(
                GenericAboutToStartOrSubmitCallbackResponse::getConfirmationBody,
                GenericAboutToStartOrSubmitCallbackResponse::getConfirmationHeader)
            .contains("# Application Issued with Errors",
                "<ul><li>There was a problem sending correspondence.</li></ul>");
        verify(corresponder, times(3))
            .sendCorrespondence(request.getCaseDetails());
        verify(assignPartiesAccessService).grantRespondentSolicitor(request.getCaseDetails().getData());
        verifyNoMoreInteractions(corresponder, assignPartiesAccessService);
    }

    @Test
    void givenGrantRespondentSolicitorFailure_whenHandled_thenRetriesThreeTimesAndShowsError()
        throws UserNotFoundInOrganisationApiException {
        ContactDetailsWrapper contactDetailsWrapper = mock(ContactDetailsWrapper.class);
        when(contactDetailsWrapper.getContestedRespondentRepresented()).thenReturn(YesOrNo.YES);

        FinremCaseData mockedCaseData = mock(FinremCaseData.class);
        when(mockedCaseData.getContactDetailsWrapper()).thenReturn(contactDetailsWrapper);
        when(mockedCaseData.getRespondentSolicitorEmail()).thenReturn(TEST_SOLICITOR_EMAIL);

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(mockedCaseData);

        // always fail
        doThrow(new RuntimeException("boom"))
            .when(assignPartiesAccessService)
            .grantRespondentSolicitor(any(FinremCaseData.class));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =  handler.handle(request, AUTH_TOKEN);

        assertThat(response)
            .extracting(
                GenericAboutToStartOrSubmitCallbackResponse::getConfirmationBody,
                GenericAboutToStartOrSubmitCallbackResponse::getConfirmationHeader)
            .contains("# Application Issued with Errors",
                "<ul><li>There was a problem granting access to respondent solicitor %s</li></ul>"
                    .formatted(TEST_SOLICITOR_EMAIL));
        verify(corresponder)
            .sendCorrespondence(request.getCaseDetails());
        verify(assignPartiesAccessService, times(3))
            .grantRespondentSolicitor(request.getCaseDetails().getData());
        verifyNoMoreInteractions(corresponder, assignPartiesAccessService);
    }

    @Test
    void givenSendCorrespondenceAndGrantRespondentSolicitorFailure_whenHandled_thenRetriesThreeTimesAndShowsError()
        throws UserNotFoundInOrganisationApiException {
        ContactDetailsWrapper contactDetailsWrapper = mock(ContactDetailsWrapper.class);
        when(contactDetailsWrapper.getContestedRespondentRepresented()).thenReturn(YesOrNo.YES);

        FinremCaseData mockedCaseData = mock(FinremCaseData.class);
        when(mockedCaseData.getContactDetailsWrapper()).thenReturn(contactDetailsWrapper);
        when(mockedCaseData.getRespondentSolicitorEmail()).thenReturn(TEST_SOLICITOR_EMAIL);

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(mockedCaseData);

        // always fail
        doThrow(new RuntimeException("boom"))
            .when(assignPartiesAccessService)
            .grantRespondentSolicitor(any(FinremCaseData.class));
        doThrow(new RuntimeException("boom"))
            .when(corresponder)
            .sendCorrespondence(any(FinremCaseDetails.class));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =  handler.handle(request, AUTH_TOKEN);

        assertThat(response)
            .extracting(
                GenericAboutToStartOrSubmitCallbackResponse::getConfirmationBody,
                GenericAboutToStartOrSubmitCallbackResponse::getConfirmationHeader)
            .contains("# Application Issued with Errors",
                ("<ul>"
                + "<li>There was a problem granting access to respondent solicitor %s</li>"
                + "<li>There was a problem sending correspondence.</li>"
                + "</ul>").formatted(TEST_SOLICITOR_EMAIL));
        verify(corresponder, times(3))
            .sendCorrespondence(request.getCaseDetails());
        verify(assignPartiesAccessService, times(3))
            .grantRespondentSolicitor(request.getCaseDetails().getData());
        verifyNoMoreInteractions(corresponder, assignPartiesAccessService);
    }
}
