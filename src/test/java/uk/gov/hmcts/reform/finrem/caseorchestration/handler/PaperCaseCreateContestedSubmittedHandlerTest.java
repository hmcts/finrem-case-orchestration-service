package uk.gov.hmcts.reform.finrem.caseorchestration.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.finrem.caseorchestration.FinremCallbackRequestFactory;
import uk.gov.hmcts.reform.finrem.caseorchestration.controllers.GenericAboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.FinremCaseData;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.AssignPartiesAccessService;
import uk.gov.hmcts.reform.finrem.caseorchestration.service.CreateCaseService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.EventType.NEW_PAPER_CASE;
import static uk.gov.hmcts.reform.finrem.caseorchestration.model.ccd.CaseType.CONTESTED;
import static uk.gov.hmcts.reform.finrem.caseorchestration.test.Assertions.assertCanHandle;

@ExtendWith(MockitoExtension.class)
class PaperCaseCreateContestedSubmittedHandlerTest {

    @InjectMocks
    private PaperCaseCreateContestedSubmittedHandler handler;

    @Mock
    private CreateCaseService createCaseService;

    @Mock
    private AssignPartiesAccessService assignPartiesAccessService;

    @Test
    void testCanHandle() {
        assertCanHandle(handler, SUBMITTED, CONTESTED, NEW_PAPER_CASE);
    }

    @Test
    void givenCase_whenHandled_thenSetSupplementaryData() {
        FinremCallbackRequest request = FinremCallbackRequestFactory.from();

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(request, AUTH_TOKEN);

        assertThat(response)
            .extracting(GenericAboutToStartOrSubmitCallbackResponse::getConfirmationBody,
                GenericAboutToStartOrSubmitCallbackResponse::getConfirmationHeader)
            .containsOnlyNulls();

        verify(createCaseService).setSupplementaryData(request, AUTH_TOKEN);
        verifyNoMoreInteractions(createCaseService);
    }

    @Test
    void givenApplicantNotRepresented_whenHandled_thenGrantApplicantSolicitor() {
        FinremCallbackRequest request = FinremCallbackRequestFactory.from();

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(request, AUTH_TOKEN);

        assertThat(response)
            .extracting(
                GenericAboutToStartOrSubmitCallbackResponse::getConfirmationBody,
                GenericAboutToStartOrSubmitCallbackResponse::getConfirmationHeader)
            .containsOnlyNulls();

        verify(assignPartiesAccessService, never()).grantApplicantSolicitor(request.getCaseDetails().getData());
    }

    @Test
    void givenApplicantRepresented_whenHandled_thenGrantApplicantSolicitor() {
        FinremCaseData mockedCaseData = mock(FinremCaseData.class);
        when(mockedCaseData.getAppSolicitorEmailIfRepresented()).thenReturn(TEST_SOLICITOR_EMAIL);

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(mockedCaseData);

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response = handler.handle(request, AUTH_TOKEN);

        assertThat(response)
            .extracting(
                GenericAboutToStartOrSubmitCallbackResponse::getConfirmationBody,
                GenericAboutToStartOrSubmitCallbackResponse::getConfirmationHeader)
            .containsOnlyNulls();

        verify(assignPartiesAccessService).grantApplicantSolicitor(request.getCaseDetails().getData());
        verifyNoMoreInteractions(assignPartiesAccessService);
    }

    @Test
    void givenSupplementaryDataFailure_whenHandled_thenRetriesThreeTimesAndShowsError() {
        FinremCaseData mockedCaseData = mock(FinremCaseData.class);
        when(mockedCaseData.getAppSolicitorEmailIfRepresented()).thenReturn(TEST_SOLICITOR_EMAIL);

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(mockedCaseData);

        // always fail
        doThrow(new RuntimeException("boom"))
            .when(createCaseService)
            .setSupplementaryData(any(FinremCallbackRequest.class), eq(AUTH_TOKEN));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =  handler.handle(request, AUTH_TOKEN);

        assertThat(response)
            .extracting(
                GenericAboutToStartOrSubmitCallbackResponse::getConfirmationBody,
                GenericAboutToStartOrSubmitCallbackResponse::getConfirmationHeader)
            .contains("# Paper Case Created with Errors",
                "<ul><li>There was a problem setting supplementary data.</li></ul>");
        verify(createCaseService, times(3))
            .setSupplementaryData(any(FinremCallbackRequest.class), eq(AUTH_TOKEN));
        verify(assignPartiesAccessService).grantApplicantSolicitor(request.getCaseDetails().getData());
        verifyNoMoreInteractions(createCaseService, assignPartiesAccessService);
    }

    @Test
    void givenAssignPartyAccessFailure_whenHandled_thenRetriesThreeTimesAndShowsError() {
        FinremCaseData mockedCaseData = mock(FinremCaseData.class);
        when(mockedCaseData.getAppSolicitorEmailIfRepresented()).thenReturn(TEST_SOLICITOR_EMAIL);

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(mockedCaseData);

        // always fail
        doThrow(new RuntimeException("boom"))
            .when(assignPartiesAccessService)
            .grantApplicantSolicitor(request.getCaseDetails().getData());

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =  handler.handle(request, AUTH_TOKEN);

        assertThat(response)
            .extracting(
                GenericAboutToStartOrSubmitCallbackResponse::getConfirmationBody,
                GenericAboutToStartOrSubmitCallbackResponse::getConfirmationHeader)
            .contains("# Paper Case Created with Errors",
                "<ul><li>There was a problem granting access to application solicitor %s</li></ul>"
                    .formatted(TEST_SOLICITOR_EMAIL));
        verify(createCaseService)
            .setSupplementaryData(request, AUTH_TOKEN);
        verify(assignPartiesAccessService, times(3)).grantApplicantSolicitor(request.getCaseDetails().getData());
        verifyNoMoreInteractions(createCaseService, assignPartiesAccessService);
    }

    @Test
    void givenSetSupplementaryDataAndAssignPartyAccessFailure_whenHandled_thenRetriesThreeTimesAndShowsError() {
        FinremCaseData mockedCaseData = mock(FinremCaseData.class);
        when(mockedCaseData.getAppSolicitorEmailIfRepresented()).thenReturn(TEST_SOLICITOR_EMAIL);

        FinremCallbackRequest request = FinremCallbackRequestFactory.from(mockedCaseData);

        // always fail
        doThrow(new RuntimeException("boom"))
            .when(assignPartiesAccessService)
            .grantApplicantSolicitor(request.getCaseDetails().getData());
        doThrow(new RuntimeException("boom"))
            .when(createCaseService)
            .setSupplementaryData(any(FinremCallbackRequest.class), eq(AUTH_TOKEN));

        GenericAboutToStartOrSubmitCallbackResponse<FinremCaseData> response =  handler.handle(request, AUTH_TOKEN);

        assertThat(response)
            .extracting(
                GenericAboutToStartOrSubmitCallbackResponse::getConfirmationBody,
                GenericAboutToStartOrSubmitCallbackResponse::getConfirmationHeader)
            .contains("# Paper Case Created with Errors",
                ("<ul>"
                    + "<li>There was a problem setting supplementary data.</li>"
                    + "<li>There was a problem granting access to application solicitor %s</li>"
                    + "</ul>")
                    .formatted(TEST_SOLICITOR_EMAIL));
        verify(createCaseService, times(3))
            .setSupplementaryData(request, AUTH_TOKEN);
        verify(assignPartiesAccessService, times(3)).grantApplicantSolicitor(request.getCaseDetails().getData());
        verifyNoMoreInteractions(createCaseService, assignPartiesAccessService);
    }
}
