package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseDetails;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.YesOrNo;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.wrapper.ContactDetailsWrapper;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignPartiesAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.UserNotFoundInOrganisationApiException;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.correspondence.assigntojudge.IssueApplicationConsentCorresponder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.finrem.caseorchestration.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.finrem.caseorchestration.ccd.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.ISSUE_APPLICATION;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONSENTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class IssueApplicationConsentedSubmittedHandlerTest {

    @InjectMocks
    private IssueApplicationConsentedSubmittedHandler handler;

    @Mock
    private IssueApplicationConsentCorresponder issueApplicationConsentCorresponder;
    @Mock
    private AssignPartiesAccessService assignPartiesAccessService;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, SUBMITTED, CONSENTED, ISSUE_APPLICATION);
    }

    @Test
    void givenCase_whenHandled_thenShouldSendCorrespondence() {
        FinremCallbackRequest request = FinremCallbackRequestFactory.from();

        handler.handle(request, AUTH_TOKEN);

        verify(issueApplicationConsentCorresponder).sendCorrespondence(request.getCaseDetails(), AUTH_TOKEN);
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
        when(contactDetailsWrapper.getConsentedRespondentRepresented()).thenReturn(YesOrNo.YES);

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
        when(contactDetailsWrapper.getConsentedRespondentRepresented()).thenReturn(YesOrNo.YES);

        FinremCaseData mockedCaseData = mock(FinremCaseData.class);
        when(mockedCaseData.getContactDetailsWrapper()).thenReturn(contactDetailsWrapper);
        when(mockedCaseData.getRespondentSolicitorEmail()).thenReturn(TEST_SOLICITOR_EMAIL);

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(mockedCaseData);

        // always fail
        doThrow(new RuntimeException("boom"))
            .when(issueApplicationConsentCorresponder)
            .sendCorrespondence(any(FinremCaseDetails.class), anyString());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =  handler.handle(request, AUTH_TOKEN);

        assertThat(response)
            .extracting(
                GenericAboutToStartOrSubmitCallbackResponse::getConfirmationBody,
                GenericAboutToStartOrSubmitCallbackResponse::getConfirmationHeader)
            .contains("# Application Issued with Errors",
                "<ul><li>There was a problem sending correspondence.</li></ul>");
        verify(issueApplicationConsentCorresponder, times(3))
            .sendCorrespondence(request.getCaseDetails(), AUTH_TOKEN);
        verify(assignPartiesAccessService).grantRespondentSolicitor(request.getCaseDetails().getData());
        verifyNoMoreInteractions(issueApplicationConsentCorresponder, assignPartiesAccessService);
    }

    @Test
    void givenGrantRespondentSolicitorFailure_whenHandled_thenRetriesThreeTimesAndShowsError()
        throws UserNotFoundInOrganisationApiException {
        ContactDetailsWrapper contactDetailsWrapper = mock(ContactDetailsWrapper.class);
        when(contactDetailsWrapper.getConsentedRespondentRepresented()).thenReturn(YesOrNo.YES);

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
        verify(issueApplicationConsentCorresponder)
            .sendCorrespondence(request.getCaseDetails(), AUTH_TOKEN);
        verify(assignPartiesAccessService, times(3))
            .grantRespondentSolicitor(request.getCaseDetails().getData());
        verifyNoMoreInteractions(issueApplicationConsentCorresponder, assignPartiesAccessService);
    }

    @Test
    void givenSendCorrespondenceAndGrantRespondentSolicitorFailure_whenHandled_thenRetriesThreeTimesAndShowsError()
        throws UserNotFoundInOrganisationApiException {
        ContactDetailsWrapper contactDetailsWrapper = mock(ContactDetailsWrapper.class);
        when(contactDetailsWrapper.getConsentedRespondentRepresented()).thenReturn(YesOrNo.YES);

        FinremCaseData mockedCaseData = mock(FinremCaseData.class);
        when(mockedCaseData.getContactDetailsWrapper()).thenReturn(contactDetailsWrapper);
        when(mockedCaseData.getRespondentSolicitorEmail()).thenReturn(TEST_SOLICITOR_EMAIL);

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(mockedCaseData);

        // always fail
        doThrow(new RuntimeException("boom"))
            .when(assignPartiesAccessService)
            .grantRespondentSolicitor(any(FinremCaseData.class));
        doThrow(new RuntimeException("boom"))
            .when(issueApplicationConsentCorresponder)
            .sendCorrespondence(any(FinremCaseDetails.class), eq(AUTH_TOKEN));

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
        verify(issueApplicationConsentCorresponder, times(3))
            .sendCorrespondence(request.getCaseDetails(), AUTH_TOKEN);
        verify(assignPartiesAccessService, times(3))
            .grantRespondentSolicitor(request.getCaseDetails().getData());
        verifyNoMoreInteractions(issueApplicationConsentCorresponder, assignPartiesAccessService);
    }
}
